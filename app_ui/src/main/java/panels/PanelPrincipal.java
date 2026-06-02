package panels;

import componentes.BotonCustom;
import dao.EtiquetaDAO;
import dao.LoteDAO;
import model.Etiqueta;
import model.LoteEtiquetas;
import service.GeneradorEtiquetas;
import util.Estilo;
import util.FontLoader;
import util.RoundBorder;

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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        construirUI();
        cargarLotes();
    }

    // ── Construcción de UI ────────────────────────────────────────────────────

    private void construirUI() {
        JLabel lblApp = new JLabel("Etiquetas D'johanna");
        lblApp.setForeground(Color.WHITE);
        lblApp.setFont(FontLoader.cargarFont(Estilo.FONT_OPEN_SANS_SEMIBOLD, 20f));
        lblApp.setBorder(new EmptyBorder(14, 12, 4, 12));
        lblApp.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblApp);

        add(crearPanelBotones());
        add(Box.createVerticalStrut(4));
        add(crearPanelContenido());

        lblEstado = new JLabel("Listo.");
        lblEstado.setBorder(new EmptyBorder(4, 10, 4, 10));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblEstado);
    }

    // Fila de botones — FlowLayout (leaf: solo contiene botones)
    private JPanel crearPanelBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        BotonCustom btnNuevo   = new BotonCustom("+ Nuevo Lote");
        BotonCustom btnEditar  = new BotonCustom("Editar Lote");
        BotonCustom btnGenerar = new BotonCustom("Generar Word");
        BotonCustom btnEliminar = new BotonCustom("Eliminar Lote");

        btnNuevo.addActionListener(e -> abrirFormulario());
        btnEditar.addActionListener(e -> editarLote());
        btnGenerar.addActionListener(e -> generarWord());
        btnEliminar.addActionListener(e -> eliminarLote());

        p.add(btnNuevo);
        p.add(btnEditar);
        p.add(btnGenerar);
        p.add(btnEliminar);
        return p;
    }

    // Fila de contenido — X_AXIS (contenedor: agrupa panelLotes + panelTabla)
    private JPanel crearPanelContenido() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(0, 8, 6, 8));

        p.add(crearPanelLotes());
        p.add(Box.createHorizontalStrut(10));
        p.add(crearPanelTabla());
        return p;
    }

    // Panel lotes — Y_AXIS (contenedor: label + scroll)
    private JPanel crearPanelLotes() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        p.setBorder(new RoundBorder(12, new Color(255, 255, 255, 45)));

        JLabel lbl = new JLabel("Lotes");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(FontLoader.cargarFont(Estilo.FONT_OPEN_SANS_SEMIBOLD, 15f));
        lbl.setBorder(new EmptyBorder(0, 2, 6, 2));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);

        modeloLista = new DefaultListModel<>();
        listaLotes  = new JList<>(modeloLista);
        listaLotes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaLotes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarDetalleLote();
        });

        JScrollPane scroll = new JScrollPane(listaLotes);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(scroll);
        return p;
    }

    // Panel tabla — Y_AXIS (contenedor: solo scroll con tabla)
    private JPanel crearPanelTabla() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentY(Component.TOP_ALIGNMENT);
        p.setBorder(new RoundBorder(12, new Color(255, 255, 255, 45)));

        JLabel lbl = new JLabel("Registros");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(FontLoader.cargarFont(Estilo.FONT_OPEN_SANS_SEMIBOLD, 15f));
        lbl.setBorder(new EmptyBorder(0, 2, 6, 2));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);

        String[] cols = {"ID", "Nombre formateado", "Prenda", "Talla"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setRowHeight(32);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setDefaultRenderer(cabeceraRenderer());
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(scroll);
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

        String nombreSugerido = lote.getNombre().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".docx";
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Guardar documento de etiquetas", java.awt.FileDialog.SAVE);
        fd.setDirectory(System.getProperty("user.home"));
        fd.setFile(nombreSugerido);
        fd.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".docx"));
        fd.setVisible(true);

        if (fd.getFile() == null) return;

        File destino = new File(fd.getDirectory(), fd.getFile());
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
