package panels;

import model.Etiqueta;
import model.LoteEtiquetas;
import model.TipoPrenda;
import dao.LoteDAO;
import dao.EtiquetaDAO;

import componentes.BotonCustom;
import componentes.ComboBoxCustom;
import componentes.TextFieldCustom;
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

    // ── Campos de la etiqueta ──────────────────────────────────────────────────
    private JTextField txtIdEmpleado;
    private JTextField txtPrimerNombre;
    private JTextField txtSegundoNombre;
    private JTextField txtPrimerApellido;
    private JTextField txtSegundoApellido;
    private ComboBoxCustom<TipoPrenda> cbTipoPrenda;
    private ComboBoxCustom<String>    cbTalla;

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
        setSize(820, 700);
        setMinimumSize(new Dimension(720, 580));
        setLocationRelativeTo(getOwner());

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Estilo.APP_COLOR);
        contenido.setBorder(new EmptyBorder(12, 14, 12, 14));

        contenido.add(seccionInfoLote());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionFormEtiqueta());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionTablaEtiquetas());
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(seccionBotonesPrincipales());

        setContentPane(contenido);
    }

    // ── Sección: info del lote ────────────────────────────────────────────────
    private JPanel seccionInfoLote() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fila1 = fila(
            lbl("Nombre del lote *:"), txtNombreLote = new TextFieldCustom(18),
            lbl("Empresa / Cliente *:"), txtEmpresa  = new TextFieldCustom(14));
        p.add(fila1);

        return p;
    }

    // ── Sección: datos de una etiqueta ────────────────────────────────────────
    private JPanel seccionFormEtiqueta() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ID (campo pequeño, sin fill completo)
        JPanel filaId = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaId.setOpaque(false);
        filaId.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtIdEmpleado = new TextFieldCustom(8);
        filaId.add(lbl("ID Empleado:"));
        filaId.add(txtIdEmpleado);
        p.add(filaId);
        p.add(Box.createVerticalStrut(8));

        // Nombres
        p.add(fila(
            lbl("Primer nombre *:"), txtPrimerNombre  = new TextFieldCustom(14),
            lbl("Segundo nombre:"),  txtSegundoNombre = new TextFieldCustom(10)));
        p.add(Box.createVerticalStrut(8));

        // Apellidos
        p.add(fila(
            lbl("Primer apellido *:"), txtPrimerApellido  = new TextFieldCustom(14),
            lbl("Segundo apellido:"),  txtSegundoApellido = new TextFieldCustom(10)));
        p.add(Box.createVerticalStrut(8));

        // Prenda + Talla
        cbTipoPrenda = new ComboBoxCustom<>(TipoPrenda.values());
        cbTalla      = new ComboBoxCustom<>(TALLAS);
        cbTalla.setEditable(true);
        p.add(fila(lbl("Tipo de prenda *:"), cbTipoPrenda,
                   lbl("Talla:"), cbTalla));
        p.add(Box.createVerticalStrut(8));

        // Preview
        JLabel lblPreview = new JLabel("—");
        lblPreview.setFont(lblPreview.getFont().deriveFont(Font.ITALIC));
        lblPreview.setForeground(new Color(130, 170, 255));
        JPanel filaPreview = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaPreview.setOpaque(false);
        filaPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaPreview.add(lbl("Vista previa:"));
        filaPreview.add(lblPreview);
        p.add(filaPreview);

        // Listener preview en tiempo real
        Runnable actualizar = () -> lblPreview.setText(construirPreview());
        for (JTextField tf : new JTextField[]{txtIdEmpleado, txtPrimerNombre,
                txtSegundoNombre, txtPrimerApellido, txtSegundoApellido}) {
            tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { actualizar.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { actualizar.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizar.run(); }
            });
        }

        p.add(Box.createVerticalStrut(8));

        // Botones agregar/actualizar
        JPanel panelBotonesEt = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBotonesEt.setOpaque(false);
        panelBotonesEt.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCancelarEdicion = new BotonCustom("Cancelar edición");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.addActionListener(e -> cancelarEdicion());

        btnAccionEtiqueta = new BotonCustom("+ Agregar Etiqueta");
        btnAccionEtiqueta.addActionListener(this::accionEtiqueta);

        panelBotonesEt.add(btnCancelarEdicion);
        panelBotonesEt.add(btnAccionEtiqueta);
        p.add(panelBotonesEt);

        return p;
    }

    // ── Sección: tabla de etiquetas ───────────────────────────────────────────
    private JPanel seccionTablaEtiquetas() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"ID", "Nombre formateado", "Prenda", "Talla"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(Estilo.APP_COLOR);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDER_COLOR),
                    new EmptyBorder(6, 10, 6, 10)));
                setHorizontalAlignment(LEFT);
                return this;
            }
        });
        tabla.getColumnModel().getColumn(0).setMaxWidth(80);
        tabla.getColumnModel().getColumn(2).setMaxWidth(160);
        tabla.getColumnModel().getColumn(3).setMaxWidth(60);

        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botTabla.setOpaque(false);

        BotonCustom btnEditarFila = new BotonCustom("Editar seleccionada");
        btnEditarFila.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) iniciarEdicionEtiqueta(fila);
        });

        BotonCustom btnEliminar = new BotonCustom("Eliminar seleccionada");
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) return;
            if (etiquetaEditandoIndice == fila) cancelarEdicion();
            else if (etiquetaEditandoIndice > fila) etiquetaEditandoIndice--;
            etiquetasEnLote.remove(fila);
            modeloTabla.removeRow(fila);
        });

        botTabla.add(btnEditarFila);
        botTabla.add(btnEliminar);
        p.add(botTabla, BorderLayout.SOUTH);
        return p;
    }

    // ── Sección: botones Cancelar / Guardar ───────────────────────────────────
    private JPanel seccionBotonesPrincipales() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        BotonCustom btnCancelar = new BotonCustom("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        BotonCustom btnGuardar = new BotonCustom("Guardar Lote");
        btnGuardar.addActionListener(this::guardarLote);

        p.add(btnCancelar);
        p.add(btnGuardar);
        return p;
    }

    // ── Precarga (modo edición) ───────────────────────────────────────────────
    private void precargarDatos() {
        txtNombreLote.setText(loteEditando.getNombre());
        txtEmpresa.setText(loteEditando.getEmpresa());

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
        btnAccionEtiqueta.setText("Actualizar");
        btnCancelarEdicion.setVisible(true);
        tabla.setRowSelectionInterval(fila, fila);
        txtPrimerNombre.requestFocus();
    }

    private void cancelarEdicion() {
        etiquetaEditandoIndice = -1;
        limpiarCamposEtiqueta();
        btnAccionEtiqueta.setText("+ Agregar Etiqueta");
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
    private JPanel fila(JLabel l1, JComponent c1, JLabel l2, JComponent c2) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l1); p.add(c1);
        p.add(Box.createHorizontalStrut(10));
        p.add(l2); p.add(c2);
        return p;
    }

    private JLabel lbl(String texto) {
        JLabel l = new JLabel(texto);
        l.setBorder(new EmptyBorder(0, 2, 0, 2));
        return l;
    }

    private void alerta(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public LoteEtiquetas getLoteGuardado() { return loteGuardado; }
}
