package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoteEtiquetas {

    private int id;
    private String nombre;
    private String empresa;
    private String descripcion;
    private String fechaCreacion;
    private List<Etiqueta> etiquetas;

    public LoteEtiquetas() {
        this.etiquetas = new ArrayList<>();
        this.fechaCreacion = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<Etiqueta> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<Etiqueta> etiquetas) { this.etiquetas = etiquetas; }

    public void agregarEtiqueta(Etiqueta etiqueta) { this.etiquetas.add(etiqueta); }

    public int getTotalEtiquetas() { return etiquetas.size(); }

    @Override
    public String toString() {
        return "[" + fechaCreacion + "]  " + nombre +
               (empresa != null && !empresa.isBlank() ? "  ·  " + empresa : "") +
               "  (" + getTotalEtiquetas() + " etiq.)";
    }
}
