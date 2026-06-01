package panels;

import model.Etiqueta;
import model.LoteEtiquetas;
import model.TipoPrenda;
import dao.LoteDAO;
import dao.EtiquetaDAO;

import util.Estilo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diálogo modal para crear un nuevo lote de etiquetas.
 */
public class panelFormulario extends JDialog {

    // ── Campos del lote ────────────────────────────────────────────────────────
    private JTextField txtNombreLote;
    private JTextField txtEmpresa;
    private JTextField txtDescripcion;

    // ── Campos de la etiqueta ──────────────────────────────────────────────────
    private JTextField txtIdEmpleado;
    private JTextField txtPrimerNombre;
    private JTextField txtSegundoNombre;
    private JTextField txtPrimerApellido;
    private JTextField txtSegundoApellido;
    private JComboBox<TipoPrenda> cbTipoPrenda;
    private JComboBox<String>    cbTalla;

    // ── Lista temporal de etiquetas ────────────────────────────────────────────
    private final List<Etiqueta> etiquetasEnLote = new ArrayList<>();
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private int etiquetaEditandoIndice = -1;
    private JButton btnAccionEtiqueta;
    private JButton btnCancelarEdicion;

    private LoteEtiquetas loteGuardado;
    private final Consumer<LoteEtiquetas> onGuardado;
    private final boolean modoEdicion;
    private LoteEtiquetas loteEditando;

    private static final String[] TALLAS = {
        "XS", "S", "M", "L", "XL", "XXL", "XXXL",
        "28", "30", "32", "34", "36", "38", "40", "42", "44", "46"
    };

    public panelFormulario(JFrame parent, Consumer<LoteEtiquetas> onGuardado) {
        super(parent, "Nuevo Lote de Etiquetas", true);
        this.onGuardado = onGuardado;
        this.modoEdicion = false;
        construirUI();
    }

    public panelFormulario(JFrame parent, LoteEtiquetas loteExistente, Consumer<LoteEtiquetas> onGuardado) {
        super(parent, "Editar Lote de Etiquetas", true);
        this.onGuardado = onGuardado;
        this.modoEdicion = true;
        this.loteEditando = loteExistente;
        construirUI();
        precargarDatos();
    }

    private void construirUI() {
        setSize(800, 680);
        setMinimumSize(new Dimension(720, 580));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(6, 6));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(panelInfoLote(),      BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                panelFormEtiqueta(), panelTablaEtiquetas());
        split.setDividerLocation(250);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        add(panelBotones(), BorderLayout.SOUTH);
    }

    // ── Información del lote ──────────────────────────────────────────────────
    private JPanel panelInfoLote() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints g = gbc();

        g.gridx = 0; g.gridy = 0;
        p.add(lbl("Nombre del lote *:"), g);
        g.gridx = 1; g.weightx = 1;
        txtNombreLote = new JTextField(20);
        p.add(txtNombreLote, g);

        g.gridx = 2; g.weightx = 0;
        p.add(lbl("Empresa / Cliente *:"), g);
        g.gridx = 3; g.weightx = 1;
        txtEmpresa = new JTextField(16);
        p.add(txtEmpresa, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        p.add(lbl("Descripción:"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        txtDescripcion = new JTextField();
        p.add(txtDescripcion, g);

        return p;
    }

    // ── Formulario de una etiqueta ────────────────────────────────────────────
    private JPanel panelFormEtiqueta() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints g = gbc();

        // Fila 0: ID empleado
        g.gridy = 0; g.gridx = 0; g.weightx = 0; g.gridwidth = 1;
        g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;
        p.add(lbl("ID Empleado:"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        txtIdEmpleado = new JTextField(14);
        p.add(txtIdEmpleado, g);

        // Fila 1: Primer nombre | Segundo nombre (opcional)
        fila(p, g, 1,
            "Primer nombre *:", txtPrimerNombre  = new JTextField(16),
            "Segundo nombre:",  txtSegundoNombre = new JTextField(12));

        // Fila 2: Primer apellido | Segundo apellido (opcional)
        fila(p, g, 2,
            "Primer apellido *:", txtPrimerApellido  = new JTextField(16),
            "Segundo apellido:",  txtSegundoApellido = new JTextField(12));

        // Fila 3: Tipo de prenda | Talla
        cbTipoPrenda = new JComboBox<>(TipoPrenda.values());
        cbTalla      = new JComboBox<>(TALLAS);
        cbTalla.setEditable(true);
        fila(p, g, 3,
            "Tipo de prenda *:", cbTipoPrenda,
            "Talla:",            cbTalla);

        // Vista previa del nombre formateado
        g.gridx = 0; g.gridy = 4; g.weightx = 0; g.gridwidth = 1;
        p.add(lbl("Vista previa:"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        JLabel lblPreview = new JLabel("—");
        lblPreview.setFont(lblPreview.getFont().deriveFont(Font.ITALIC));
        lblPreview.setForeground(new Color(60, 60, 200));
        p.add(lblPreview, g);

        // Actualizar preview en tiempo real
        var actualizar = new Runnable() {
            @Override public void run() { lblPreview.setText(construirPreview()); }
        };
        for (JTextField tf : new JTextField[]{txtIdEmpleado, txtPrimerNombre, txtSegundoNombre,
                                              txtPrimerApellido, txtSegundoApellido}) {
            tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { actualizar.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { actualizar.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar.run(); }
            });
        }

        // Botones de acción (agregar / actualizar)
        g.gridx = 0; g.gridy = 5; g.gridwidth = 4; g.weightx = 1;
        g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.EAST;
        JPanel panelBotonesEt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panelBotonesEt.setOpaque(false);

        btnCancelarEdicion = new JButton("Cancelar edición");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.addActionListener(e -> cancelarEdicion());

        btnAccionEtiqueta = botonDinamico("  + Agregar Etiqueta  ", new Color(34, 139, 34), Color.WHITE);
        btnAccionEtiqueta.addActionListener(this::accionEtiqueta);

        panelBotonesEt.add(btnCancelarEdicion);
        panelBotonesEt.add(btnAccionEtiqueta);
        p.add(panelBotonesEt, g);

        return p;
    }

    // ── Tabla de etiquetas del lote ───────────────────────────────────────────
    private JPanel panelTablaEtiquetas() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[] cols = {"ID", "Nombre formateado", "Prenda", "Talla"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
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
        });
        tabla.getColumnModel().getColumn(0).setMaxWidth(80);
        tabla.getColumnModel().getColumn(2).setMaxWidth(160);
        tabla.getColumnModel().getColumn(3).setMaxWidth(60);

        JButton btnEditarFila = new JButton("Editar seleccionada");
        btnEditarFila.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) iniciarEdicionEtiqueta(fila);
        });

        JButton btnEliminar = new JButton("Eliminar seleccionada");
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) return;
            if (etiquetaEditandoIndice == fila) cancelarEdicion();
            else if (etiquetaEditandoIndice > fila) etiquetaEditandoIndice--;
            etiquetasEnLote.remove(fila);
            modeloTabla.removeRow(fila);
        });

        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        bot.add(btnEditarFila);
        bot.add(btnEliminar);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    // ── Botones principales ───────────────────────────────────────────────────
    private JPanel panelBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setBorder(new EmptyBorder(6, 0, 0, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        JButton btnGuardar = boton("  Guardar Lote  ", new Color(0, 102, 204), Color.WHITE);
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD));
        btnGuardar.addActionListener(this::guardarLote);

        p.add(btnCancelar);
        p.add(btnGuardar);
        return p;
    }

    // ── Precarga (modo edición) ───────────────────────────────────────────────
    private void precargarDatos() {
        txtNombreLote.setText(loteEditando.getNombre());
        txtEmpresa.setText(loteEditando.getEmpresa());
        txtDescripcion.setText(loteEditando.getDescripcion() != null ? loteEditando.getDescripcion() : "");

        etiquetasEnLote.clear();
        etiquetasEnLote.addAll(loteEditando.getEtiquetas());
        modeloTabla.setRowCount(0);
        for (Etiqueta et : loteEditando.getEtiquetas()) {
            modeloTabla.addRow(new Object[]{
                et.getIdEmpleado(),
                et.getNombreFormateado(),
                et.getTipoPrenda(),
                et.getTalla()
            });
        }
    }

    // ── Edición inline de etiqueta ────────────────────────────────────────────
    private void iniciarEdicionEtiqueta(int fila) {
        etiquetaEditandoIndice = fila;
        Etiqueta et = etiquetasEnLote.get(fila);
        txtIdEmpleado.setText(et.getIdEmpleado() != null ? et.getIdEmpleado() : "");
        txtPrimerNombre.setText(et.getPrimerNombre() != null ? et.getPrimerNombre() : "");
        txtSegundoNombre.setText(et.getSegundoNombre() != null ? et.getSegundoNombre() : "");
        txtPrimerApellido.setText(et.getPrimerApellido() != null ? et.getPrimerApellido() : "");
        txtSegundoApellido.setText(et.getSegundoApellido() != null ? et.getSegundoApellido() : "");
        if (et.getTipoPrenda() != null) cbTipoPrenda.setSelectedItem(et.getTipoPrenda());
        if (et.getTalla() != null) cbTalla.setSelectedItem(et.getTalla());
        setBotonModo(btnAccionEtiqueta, "  Actualizar  ", new Color(180, 100, 0));
        btnCancelarEdicion.setVisible(true);
        tabla.setRowSelectionInterval(fila, fila);
        txtPrimerNombre.requestFocus();
    }

    private void cancelarEdicion() {
        etiquetaEditandoIndice = -1;
        limpiarCamposEtiqueta();
        setBotonModo(btnAccionEtiqueta, "  + Agregar Etiqueta  ", new Color(34, 139, 34));
        btnCancelarEdicion.setVisible(false);
        tabla.clearSelection();
    }

    private void accionEtiqueta(ActionEvent e) {
        if (etiquetaEditandoIndice >= 0) actualizarEtiqueta();
        else agregarEtiqueta(e);
    }

    private void actualizarEtiqueta() {
        if (!validarCamposEtiqueta()) return;
        Etiqueta et = etiquetasEnLote.get(etiquetaEditandoIndice);
        et.setIdEmpleado(txtIdEmpleado.getText().trim());
        et.setPrimerNombre(txtPrimerNombre.getText().trim());
        et.setSegundoNombre(txtSegundoNombre.getText().trim());
        et.setPrimerApellido(txtPrimerApellido.getText().trim());
        et.setSegundoApellido(txtSegundoApellido.getText().trim());
        et.setTipoPrenda((TipoPrenda) cbTipoPrenda.getSelectedItem());
        et.setTalla(cbTalla.getSelectedItem() != null ? cbTalla.getSelectedItem().toString().trim() : "");
        modeloTabla.setValueAt(et.getIdEmpleado(),        etiquetaEditandoIndice, 0);
        modeloTabla.setValueAt(et.getNombreFormateado(),  etiquetaEditandoIndice, 1);
        modeloTabla.setValueAt(et.getTipoPrenda(),        etiquetaEditandoIndice, 2);
        modeloTabla.setValueAt(et.getTalla(),             etiquetaEditandoIndice, 3);
        cancelarEdicion();
    }

    private void setBotonModo(JButton btn, String texto, Color bg) {
        btn.putClientProperty("baseColor", bg);
        btn.setText(texto);
        btn.setBackground(bg);
    }

    // ── Lógica ────────────────────────────────────────────────────────────────
    private void agregarEtiqueta(ActionEvent e) {
        if (!validarCamposEtiqueta()) return;

        Etiqueta et = new Etiqueta();
        et.setIdEmpleado(txtIdEmpleado.getText().trim());
        et.setPrimerNombre(txtPrimerNombre.getText().trim());
        et.setSegundoNombre(txtSegundoNombre.getText().trim());
        et.setPrimerApellido(txtPrimerApellido.getText().trim());
        et.setSegundoApellido(txtSegundoApellido.getText().trim());
        et.setTipoPrenda((TipoPrenda) cbTipoPrenda.getSelectedItem());
        et.setTalla(cbTalla.getSelectedItem() != null ? cbTalla.getSelectedItem().toString().trim() : "");

        etiquetasEnLote.add(et);
        modeloTabla.addRow(new Object[]{
            et.getIdEmpleado(),
            et.getNombreFormateado(),
            et.getTipoPrenda(),
            et.getTalla()
        });

        limpiarCamposEtiqueta();
        txtIdEmpleado.requestFocus();
    }

    private void guardarLote(ActionEvent e) {
        String nombre = txtNombreLote.getText().trim();
        if (nombre.isEmpty()) {
            alerta("El nombre del lote es obligatorio.");
            txtNombreLote.requestFocus();
            return;
        }
        if (txtEmpresa.getText().trim().isEmpty()) {
            alerta("El nombre de la empresa es obligatorio.");
            txtEmpresa.requestFocus();
            return;
        }
        if (etiquetasEnLote.isEmpty()) {
            alerta("Agregue al menos una etiqueta antes de guardar.");
            return;
        }

        try {
            if (modoEdicion) {
                loteEditando.setNombre(nombre);
                loteEditando.setEmpresa(txtEmpresa.getText().trim());
                loteEditando.setDescripcion(txtDescripcion.getText().trim());
                loteEditando.setEtiquetas(new ArrayList<>(etiquetasEnLote));

                EtiquetaDAO etiquetaDAO = new EtiquetaDAO();
                new LoteDAO().actualizar(loteEditando);
                etiquetaDAO.eliminarPorLote(loteEditando.getId());
                loteEditando.getEtiquetas().forEach(et -> et.setLoteId(loteEditando.getId()));
                etiquetaDAO.guardarTodas(loteEditando.getEtiquetas());

                loteGuardado = loteEditando;
            } else {
                LoteEtiquetas lote = new LoteEtiquetas();
                lote.setNombre(nombre);
                lote.setEmpresa(txtEmpresa.getText().trim());
                lote.setDescripcion(txtDescripcion.getText().trim());
                lote.setEtiquetas(new ArrayList<>(etiquetasEnLote));

                new LoteDAO().guardar(lote);
                lote.getEtiquetas().forEach(et -> et.setLoteId(lote.getId()));
                new EtiquetaDAO().guardarTodas(lote.getEtiquetas());

                loteGuardado = lote;
            }

            if (onGuardado != null) onGuardado.accept(loteGuardado);
            dispose();

        } catch (SQLException ex) {
            alerta("Error al guardar en la base de datos:\n" + ex.getMessage());
        }
    }

    private boolean validarCamposEtiqueta() {
        if (txtPrimerNombre.getText().trim().isEmpty()) {
            alerta("El primer nombre es obligatorio.");
            txtPrimerNombre.requestFocus();
            return false;
        }
        if (txtPrimerApellido.getText().trim().isEmpty()) {
            alerta("El primer apellido es obligatorio.");
            txtPrimerApellido.requestFocus();
            return false;
        }
        return true;
    }

    private void limpiarCamposEtiqueta() {
        txtIdEmpleado.setText("");
        txtPrimerNombre.setText("");
        txtSegundoNombre.setText("");
        txtPrimerApellido.setText("");
        txtSegundoApellido.setText("");
        cbTipoPrenda.setSelectedIndex(0);
        cbTalla.setSelectedItem("M");
    }

    private String construirPreview() {
        String id    = txtIdEmpleado.getText().trim();
        String pNom  = txtPrimerNombre.getText().trim();
        String sNom  = txtSegundoNombre.getText().trim();
        String pApe  = txtPrimerApellido.getText().trim();
        String sApe  = txtSegundoApellido.getText().trim();

        if (pNom.isEmpty() && pApe.isEmpty()) return "—";

        StringBuilder nombre = new StringBuilder();
        if (!pNom.isEmpty()) nombre.append(pNom.toUpperCase());
        if (!sNom.isEmpty()) nombre.append(" ").append(sNom.substring(0, 1).toUpperCase()).append(".");
        if (!pApe.isEmpty()) nombre.append(" ").append(pApe.toUpperCase());
        if (!sApe.isEmpty()) nombre.append(" ").append(sApe.substring(0, 1).toUpperCase()).append(".");

        return (id.isEmpty() ? "" : id + "  ") + nombre;
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    private void fila(JPanel p, GridBagConstraints g, int gridy,
                      String lbl1, JComponent c1, String lbl2, JComponent c2) {
        g.gridy = gridy; g.weightx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; p.add(lbl(lbl1), g);
        g.gridx = 1; g.weightx = 1; p.add(c1, g);
        g.gridx = 2; g.weightx = 0; p.add(lbl(lbl2), g);
        g.gridx = 3; g.weightx = 0.5; p.add(c2, g);
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setBorder(new EmptyBorder(0, 4, 0, 4));
        return l;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    private JButton botonDinamico(String texto, Color bg, Color fg) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.putClientProperty("baseColor", bg);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                Color base = (Color) b.getClientProperty("baseColor");
                b.setBackground(colorHover(base));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground((Color) b.getClientProperty("baseColor"));
            }
        });
        return b;
    }

    private JButton boton(String texto, Color bg, Color fg) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setOpaque(true);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        Color hover = colorHover(bg);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    private Color colorHover(Color base) {
        return new Color(
            Math.min(255, base.getRed()   + 40),
            Math.min(255, base.getGreen() + 40),
            Math.min(255, base.getBlue()  + 40));
    }

    private void alerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public LoteEtiquetas getLoteGuardado() { return loteGuardado; }
}
