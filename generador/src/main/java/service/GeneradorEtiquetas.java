package service;

import model.Etiqueta;
import model.LoteEtiquetas;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Genera documentos Word (.docx) con las etiquetas de un lote.
 *
 * Modo principal: usa la plantilla FORMATO_ETIQUETAS.dotx desde el classpath.
 *   - La plantilla tiene una tabla 3 col × 10 filas = 30 celdas por página.
 *   - Celdas de etiqueta en columnas 0, 2, 4 (columnas 1 y 3 son separadores).
 *   - Si el lote supera 30 etiquetas se generan archivos adicionales (_p2, _p3…).
 *
 * Modo fallback: genera el documento desde cero si no hay plantilla en classpath.
 */
public class GeneradorEtiquetas {

    private static final String TEMPLATE = "/FORMATO_ETIQUETAS.dotx";
    private static final int POR_PAGINA = 30;
    private static final int FILAS      = 10;
    private static final int COLS       = 3;

    // ── Punto de entrada ──────────────────────────────────────────────────────

    /**
     * Genera uno o más archivos Word para el lote dado.
     * @return lista de archivos creados (uno por página de 30 etiquetas)
     */
    public List<File> generar(LoteEtiquetas lote, File destino) throws Exception {
        if (templateDisponible()) {
            return generarConPlantilla(lote, destino);
        }
        return List.of(generarSinPlantilla(lote, destino));
    }

    private boolean templateDisponible() {
        try (InputStream s = getClass().getResourceAsStream(TEMPLATE)) {
            return s != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Modo plantilla ────────────────────────────────────────────────────────

    private List<File> generarConPlantilla(LoteEtiquetas lote, File destino) throws Exception {
        List<File> archivos = new ArrayList<>();
        List<Etiqueta> etiquetas = lote.getEtiquetas();
        String empresa = noVacio(lote.getEmpresa()) ? lote.getEmpresa() : "";

        int numPaginas = Math.max(1, (etiquetas.size() + POR_PAGINA - 1) / POR_PAGINA);

        for (int pag = 0; pag < numPaginas; pag++) {
            int desde = pag * POR_PAGINA;
            int hasta = Math.min(desde + POR_PAGINA, etiquetas.size());
            List<Etiqueta> grupo = etiquetas.subList(desde, hasta);

            File archivo = (numPaginas == 1) ? destino : paginado(destino, pag + 1);

            try (InputStream raw  = getClass().getResourceAsStream(TEMPLATE);
                 InputStream tmpl = dotxADocx(raw);
                 XWPFDocument doc  = new XWPFDocument(tmpl)) {

                llenarTabla(doc.getTables().get(0), grupo, empresa);

                try (FileOutputStream out = new FileOutputStream(archivo)) {
                    doc.write(out);
                }
            }
            archivos.add(archivo);
        }

        return archivos;
    }

    private void llenarTabla(XWPFTable tabla, List<Etiqueta> etiquetas, String empresa) {
        int idx = 0;
        for (int fila = 0; fila < FILAS; fila++) {
            XWPFTableRow row = tabla.getRow(fila);
            for (int col = 0; col < COLS; col++) {
                // Columnas de etiqueta: 0, 2, 4 (separadores en 1 y 3)
                XWPFTableCell celda = row.getCell(col * 2);
                if (idx < etiquetas.size()) {
                    llenarCelda(celda, etiquetas.get(idx), empresa);
                } else {
                    vaciarCelda(celda);
                }
                idx++;
            }
        }
    }

    private void llenarCelda(XWPFTableCell celda, Etiqueta e, String empresa) {
        // Dejar solo el primer párrafo (el resto los agrega el método)
        while (celda.getParagraphs().size() > 1) {
            celda.removeParagraph(celda.getParagraphs().size() - 1);
        }

        // Línea 1 — EMPRESA (negrita)
        parrafo(celda.getParagraphArray(0), empresa.toUpperCase(), true, 11);

        // Línea 2 — ID EMPLEADO   NOMBRE FORMATEADO
        String linea2 = (noVacio(e.getIdEmpleado()) ? e.getIdEmpleado() + "  " : "")
                      + e.getNombreFormateado();
        parrafo(celda.addParagraph(), linea2, false, 9);

        // Línea 3 — PRENDA   T:TALLA
        String prenda = e.getTipoPrenda() != null ? e.getTipoPrenda().getEtiqueta().toUpperCase() : "";
        String linea3 = prenda + (noVacio(e.getTalla()) ? "  T:" + e.getTalla().toUpperCase() : "");
        parrafo(celda.addParagraph(), linea3, false, 9);
    }

    private void vaciarCelda(XWPFTableCell celda) {
        while (celda.getParagraphs().size() > 1) {
            celda.removeParagraph(celda.getParagraphs().size() - 1);
        }
        XWPFParagraph p = celda.getParagraphArray(0);
        while (!p.getRuns().isEmpty()) p.removeRun(0);
    }

    private void parrafo(XWPFParagraph p, String texto, boolean bold, int fontSize) {
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingAfter(0);
        p.setSpacingBefore(0);
        while (!p.getRuns().isEmpty()) p.removeRun(0);
        XWPFRun run = p.createRun();
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setText(texto);
    }

    private File paginado(File base, int pagina) {
        String nombre = base.getName().replace(".docx", "") + "_p" + pagina + ".docx";
        return new File(base.getParent(), nombre);
    }

    // ── Conversión dotx → docx ────────────────────────────────────────────────

    /**
     * Un archivo .dotx es un ZIP igual que .docx, pero su [Content_Types].xml
     * declara el tipo "template". Word rechaza el resultado si ese tipo no coincide
     * con la extensión .docx. Este método reemplaza la cadena en memoria antes de
     * que POI abra el archivo, evitando el error "archivo dañado".
     */
    private InputStream dotxADocx(InputStream dotx) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (ZipInputStream  zin  = new ZipInputStream(dotx);
             ZipOutputStream  zout = new ZipOutputStream(buf)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                zout.putNextEntry(new ZipEntry(entry.getName()));
                if ("[Content_Types].xml".equals(entry.getName())) {
                    String xml = new String(zin.readAllBytes(), StandardCharsets.UTF_8);
                    xml = xml.replace(
                        "wordprocessingml.template.main+xml",
                        "wordprocessingml.document.main+xml");
                    zout.write(xml.getBytes(StandardCharsets.UTF_8));
                } else {
                    zin.transferTo(zout);
                }
                zout.closeEntry();
            }
        }
        return new ByteArrayInputStream(buf.toByteArray());
    }

    // ── Modo sin plantilla (fallback) ─────────────────────────────────────────

    private File generarSinPlantilla(LoteEtiquetas lote, File destino) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            configurarMargenes(doc);
            agregarTitulo(doc, lote);

            String empresa = noVacio(lote.getEmpresa()) ? lote.getEmpresa() : "";
            List<Etiqueta> etiquetas = lote.getEtiquetas();

            for (int i = 0; i < etiquetas.size(); i += 2) {
                boolean haySegunda = (i + 1) < etiquetas.size();
                XWPFTable tabla = doc.createTable(1, haySegunda ? 2 : 1);
                ajustarAnchoTabla(tabla, haySegunda ? 2 : 1);

                llenarCeldaScratch(tabla.getRow(0).getCell(0), etiquetas.get(i), empresa);
                if (haySegunda) {
                    llenarCeldaScratch(tabla.getRow(0).getCell(1), etiquetas.get(i + 1), empresa);
                }

                XWPFParagraph sep = doc.createParagraph();
                sep.setSpacingAfter(40);
            }

            try (FileOutputStream out = new FileOutputStream(destino)) {
                doc.write(out);
            }
        }
        return destino;
    }

    private void llenarCeldaScratch(XWPFTableCell celda, Etiqueta e, String empresa) {
        setPaddingCelda(celda);
        parrafo(celda.getParagraphArray(0), empresa.toUpperCase(), true, 12);

        String linea2 = (noVacio(e.getIdEmpleado()) ? e.getIdEmpleado() + "  " : "")
                      + e.getNombreFormateado();
        parrafo(celda.addParagraph(), linea2, false, 10);

        String prenda = e.getTipoPrenda() != null ? e.getTipoPrenda().getEtiqueta().toUpperCase() : "";
        String linea3 = prenda + (noVacio(e.getTalla()) ? "  T:" + e.getTalla().toUpperCase() : "");
        parrafo(celda.addParagraph(), linea3, false, 10);
    }

    private void agregarTitulo(XWPFDocument doc, LoteEtiquetas lote) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        p.setSpacingAfter(140);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(13);
        r.setText("ETIQUETAS — " + lote.getNombre().toUpperCase());
        if (noVacio(lote.getEmpresa())) {
            r.addBreak();
            XWPFRun r2 = p.createRun();
            r2.setFontSize(10);
            r2.setText(lote.getEmpresa().toUpperCase() + "   ·   " + lote.getFechaCreacion());
        }
    }

    private void setPaddingCelda(XWPFTableCell celda) {
        CTTc ctTc = celda.getCTTc();
        CTTcPr tcPr = ctTc.getTcPr();
        if (tcPr == null) tcPr = ctTc.addNewTcPr();
        CTTcMar mar = tcPr.getTcMar();
        if (mar == null) mar = tcPr.addNewTcMar();
        BigInteger pad = BigInteger.valueOf(100);
        setW(mar.addNewTop(), pad); setW(mar.addNewBottom(), pad);
        setW(mar.addNewLeft(), pad); setW(mar.addNewRight(), pad);
    }

    private void ajustarAnchoTabla(XWPFTable tabla, int cols) {
        CTTbl ctTbl = tabla.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr();
        if (tblPr == null) tblPr = ctTbl.addNewTblPr();
        CTTblWidth tblW = tblPr.getTblW();
        if (tblW == null) tblW = tblPr.addNewTblW();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(9360));
        long anchoCelda = 9360L / cols;
        for (XWPFTableRow row : tabla.getRows()) {
            for (XWPFTableCell c : row.getTableCells()) {
                CTTc ctTc = c.getCTTc();
                CTTcPr tcPr = ctTc.getTcPr();
                if (tcPr == null) tcPr = ctTc.addNewTcPr();
                CTTblWidth tcW = tcPr.getTcW();
                if (tcW == null) tcW = tcPr.addNewTcW();
                tcW.setType(STTblWidth.DXA);
                tcW.setW(BigInteger.valueOf(anchoCelda));
            }
        }
    }

    private void configurarMargenes(XWPFDocument doc) {
        CTBody body = doc.getDocument().getBody();
        CTSectPr sectPr = body.getSectPr();
        if (sectPr == null) sectPr = body.addNewSectPr();
        CTPageMar pgMar = sectPr.getPgMar();
        if (pgMar == null) pgMar = sectPr.addNewPgMar();
        BigInteger m = BigInteger.valueOf(720);
        pgMar.setTop(m); pgMar.setBottom(m); pgMar.setLeft(m); pgMar.setRight(m);
    }

    private void setW(CTTblWidth w, BigInteger val) {
        w.setType(STTblWidth.DXA); w.setW(val);
    }

    private boolean noVacio(String s) {
        return s != null && !s.isBlank();
    }
}
