package panels;

import componentes.BotonCustom;
import dao.EtiquetaDAO;
import dao.LoteDAO;
import model.Etiqueta;
import model.LoteEtiquetas;
import service.GeneradorEtiquetas;
import util.Estilo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel principal de la aplicación.
 * Izquierda: lista de lotes guardados.
 * Derecha: detalle de etiquetas del lote seleccionado.
 */
public class PanelPrincipal extends JPanel {

    private final LoteDAO loteDAO       = new LoteDAO();
    private final EtiquetaDAO etiquetaDAO = new EtiquetaDAO();
    private final GeneradorEtiquetas generador = new GeneradorEtiquetas();

    private DefaultListModel<LoteEtiquetas> modeloLista;
    private JList<LoteEtiquetas> listaLotes;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado;

    private JFrame parentFrame;

    public PanelPrincipal(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 0));
        construirUI();
        cargarLotes();
    }

    private void construirUI() {
        add(crearToolbar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelLista(), crearPanelDetalle());
        split.setDividerLocation(310);
        split.setBorder(new EmptyBorder(4, 6, 4, 6));
        add(split, BorderLayout.CENTER);

        lblEstado = new JLabel("Listo.");
        lblEstado.setBorder(new EmptyBorder(3, 8, 3, 8));
        add(lblEstado, BorderLayout.SOUTH);
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────
    private JToolBar crearToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(new EmptyBorder(6, 6, 6, 6));

        BotonCustom btnNuevo = new BotonCustom("+ Nuevo Lote");
        btnNuevo.addActionListener(e -> abrirFormulario());

        BotonCustom btnEditar = new BotonCustom("Editar Lote");
        btnEditar.addActionListener(e -> editarLote());

        BotonCustom btnGenerar = new BotonCustom("Generar Word");
        btnGenerar.addActionListener(e -> generarWord());

        BotonCustom btnEliminar = new BotonCustom("Eliminar Lote");
        btnEliminar.addActionListener(e -> eliminarLote());

        tb.add(btnNuevo);
        tb.addSeparator(new Dimension(12, 0));
        tb.add(btnEditar);
        tb.addSeparator(new Dimension(12, 0));
        tb.add(btnGenerar);
        tb.addSeparator(new Dimension(12, 0));
        tb.add(btnEliminar);
        return tb;
    }

    // ── Panel izquierdo: lista de lotes ──────────────────────────────────────
    private JPanel crearPanelLista() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(4, 4, 4, 4));

        modeloLista = new DefaultListModel<>();
        listaLotes = new JList<>(modeloLista);
        listaLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaLotes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarDetalleLote();
        });

        p.add(new JScrollPane(listaLotes), BorderLayout.CENTER);
        return p;
    }

    // ── Panel derecho: detalle de etiquetas ──────────────────────────────────
    private JPanel crearPanelDetalle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] cols = {"ID", "Nombre formateado", "Prenda", "Talla"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setDefaultRenderer(cabeceraRenderer());

        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    private DefaultTableCellRenderer cabeceraRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(Estilo.APP_COLOR);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD));
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDER_COLOR));
                setHorizontalAlignment(CENTER);
                return this;
            }
        };
    }

    // ── Acciones ─────────────────────────────────────────────────────────────
    private void abrirFormulario() {
        panelFormulario form = new panelFormulario(parentFrame, lote -> {
            cargarLotes();
            estado("Lote guardado: \"" + lote.getNombre() + "\" (" + lote.getTotalEtiquetas() + " etiquetas)");
        });
        form.setVisible(true);
    }

    private void editarLote() {
        LoteEtiquetas lote = listaLotes.getSelectedValue();
        if (lote == null) {
            alerta("Seleccione un lote para editar.");
            return;
        }
        try {
            if (lote.getEtiquetas().isEmpty()) {
                lote.setEtiquetas(etiquetaDAO.buscarPorLote(lote.getId()));
            }
        } catch (SQLException ex) {
            error("Error al cargar etiquetas: " + ex.getMessage());
            return;
        }

        panelFormulario form = new panelFormulario(parentFrame, lote, loteActualizado -> {
            cargarLotes();
            estado("Lote actualizado: \"" + loteActualizado.getNombre() + "\" (" + loteActualizado.getTotalEtiquetas() + " etiquetas)");
        });
        form.setVisible(true);
    }

    private void generarWord() {
        LoteEtiquetas lote = listaLotes.getSelectedValue();
        if (lote == null) {
            alerta("Seleccione un lote de la lista.");
            return;
        }

        // Cargar etiquetas si no están en memoria
        if (lote.getEtiquetas().isEmpty()) {
            try {
                lote.setEtiquetas(etiquetaDAO.buscarPorLote(lote.getId()));
            } catch (SQLException ex) {
                error("Error al cargar etiquetas: " + ex.getMessage());
                return;
            }
        }

        JFileChooser fc = new JFileChooser();
        String nombreSugerido = lote.getNombre().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".docx";
        fc.setSelectedFile(new File(System.getProperty("user.home"), nombreSugerido));
        fc.setDialogTitle("Guardar documento de etiquetas");
        fc.addChoosableFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter("Documento Word (*.docx)", "docx"));

        if (fc.showSaveDialog(parentFrame) != JFileChooser.APPROVE_OPTION) return;

        File destino = fc.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".docx")) {
            destino = new File(destino.getAbsolutePath() + ".docx");
        }

        final File archivoFinal = destino;
        estado("Generando documento Word...");
        parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        LoteEtiquetas loteRef = lote;
        new SwingWorker<List<File>, Void>() {
            @Override
            protected List<File> doInBackground() throws Exception {
                return generador.generar(loteRef, archivoFinal);
            }

            @Override
            protected void done() {
                parentFrame.setCursor(Cursor.getDefaultCursor());
                try {
                    List<File> archivos = get();
                    estado(archivos.size() + " archivo(s) generado(s).");

                    if (archivos.size() == 1) {
                        int ok = JOptionPane.showConfirmDialog(parentFrame,
                            "Documento generado exitosamente.\n" + archivos.get(0).getAbsolutePath() +
                            "\n\n¿Abrir ahora?",
                            "Éxito", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (ok == JOptionPane.YES_OPTION) Desktop.getDesktop().open(archivos.get(0));
                    } else {
                        // Múltiples páginas
                        StringBuilder sb = new StringBuilder("Se generaron ")
                            .append(archivos.size()).append(" archivos (lote supera 30 etiquetas):\n\n");
                        archivos.forEach(f -> sb.append("  • ").append(f.getName()).append("\n"));
                        sb.append("\n¿Abrir la carpeta destino?");

                        int ok = JOptionPane.showConfirmDialog(parentFrame, sb.toString(),
                            "Archivos generados", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (ok == JOptionPane.YES_OPTION) {
                            Desktop.getDesktop().open(archivos.get(0).getParentFile());
                        }
                    }
                } catch (Exception ex) {
                    estado("Error al generar el documento.");
                    error("No se pudo generar el documento:\n" + ex.getMessage());
                }
            }
        }.execute();
    }

    private void eliminarLote() {
        LoteEtiquetas lote = listaLotes.getSelectedValue();
        if (lote == null) {
            alerta("Seleccione un lote para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
            "¿Eliminar el lote \"" + lote.getNombre() + "\"?\n" +
            "Se borrarán todas sus etiquetas de la base de datos.",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                loteDAO.eliminar(lote.getId());
                cargarLotes();
                modeloTabla.setRowCount(0);
                estado("Lote eliminado.");
            } catch (SQLException ex) {
                error("Error al eliminar: " + ex.getMessage());
            }
        }
    }

    // ── Carga de datos ────────────────────────────────────────────────────────
    private void cargarLotes() {
        try {
            modeloLista.clear();
            List<LoteEtiquetas> lotes = loteDAO.buscarTodos();
            lotes.forEach(modeloLista::addElement);
            estado(lotes.size() + " lote(s) cargados.");
        } catch (SQLException ex) {
            error("Error al cargar lotes: " + ex.getMessage());
        }
    }

    private void cargarDetalleLote() {
        LoteEtiquetas lote = listaLotes.getSelectedValue();
        modeloTabla.setRowCount(0);
        if (lote == null) return;

        try {
            List<Etiqueta> lista = etiquetaDAO.buscarPorLote(lote.getId());
            lote.setEtiquetas(lista);
            for (Etiqueta e : lista) {
                modeloTabla.addRow(new Object[]{
                    e.getIdEmpleado(),
                    e.getNombreFormateado(),
                    e.getTipoPrenda(),
                    e.getTalla()
                });
            }
            estado("Lote: \"" + lote.getNombre() + "\"  —  " + lista.size() + " etiqueta(s)");
        } catch (SQLException ex) {
            error("Error al cargar etiquetas: " + ex.getMessage());
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    private void estado(String msg) { lblEstado.setText(msg); }
    private void alerta(String msg) {
        JOptionPane.showMessageDialog(parentFrame, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }
    private void error(String msg) {
        JOptionPane.showMessageDialog(parentFrame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
