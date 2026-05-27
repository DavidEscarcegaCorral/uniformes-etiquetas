package model;

public class Etiqueta {

    private int id;
    private int loteId;

    // Identificación del empleado
    private String idEmpleado;

    // Nombre desglosado para formateo correcto
    private String primerNombre;
    private String segundoNombre;   // opcional
    private String primerApellido;
    private String segundoApellido; // opcional

    // Prenda
    private TipoPrenda tipoPrenda;
    private String talla;

    // Número asignado por el taller de confección
    private int numeroTaller;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLoteId() { return loteId; }
    public void setLoteId(int loteId) { this.loteId = loteId; }

    public String getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(String idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public TipoPrenda getTipoPrenda() { return tipoPrenda; }
    public void setTipoPrenda(TipoPrenda tipoPrenda) { this.tipoPrenda = tipoPrenda; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getNumeroTaller() { return numeroTaller; }
    public void setNumeroTaller(int numeroTaller) { this.numeroTaller = numeroTaller; }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    /**
     * Formatea el nombre completo siguiendo la convención:
     * PRIMER_NOMBRE [I.] PRIMER_APELLIDO [I.]
     * donde I. es la inicial del segundo nombre / segundo apellido, en mayúsculas.
     * Ejemplo: "JUANA K. VEGA C."
     */
    public String getNombreFormateado() {
        StringBuilder sb = new StringBuilder();
        sb.append(primerNombre.trim().toUpperCase());

        if (segundoNombre != null && !segundoNombre.isBlank()) {
            sb.append(" ")
              .append(segundoNombre.trim().substring(0, 1).toUpperCase())
              .append(".");
        }

        sb.append(" ").append(primerApellido.trim().toUpperCase());

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            sb.append(" ")
              .append(segundoApellido.trim().substring(0, 1).toUpperCase())
              .append(".");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return idEmpleado + "  " + getNombreFormateado() + "  " +
               (tipoPrenda != null ? tipoPrenda.getEtiqueta() : "") + "  T:" + talla;
    }
}
