package dao;

import model.Etiqueta;
import model.TipoPrenda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtiquetaDAO {

    public void guardarTodas(List<Etiqueta> etiquetas) throws SQLException {
        String sql = """
                INSERT INTO etiquetas
                    (lote_id, id_empleado, primer_nombre, segundo_nombre,
                     primer_apellido, segundo_apellido, tipo_prenda, talla)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (Etiqueta e : etiquetas) {
                ps.setInt(1, e.getLoteId());
                ps.setString(2, e.getIdEmpleado());
                ps.setString(3, e.getPrimerNombre());
                ps.setString(4, e.getSegundoNombre());
                ps.setString(5, e.getPrimerApellido());
                ps.setString(6, e.getSegundoApellido());
                ps.setString(7, e.getTipoPrenda() != null ? e.getTipoPrenda().name() : null);
                ps.setString(8, e.getTalla());
                ps.addBatch();
            }

            ps.executeBatch();

            ResultSet keys = ps.getGeneratedKeys();
            int i = 0;
            while (keys.next() && i < etiquetas.size()) {
                etiquetas.get(i++).setId(keys.getInt(1));
            }
        }
    }

    public void eliminarPorLote(int loteId) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM etiquetas WHERE lote_id = ?")) {
            ps.setInt(1, loteId);
            ps.executeUpdate();
        }
    }

    public List<Etiqueta> buscarPorLote(int loteId) throws SQLException {
        String sql = "SELECT * FROM etiquetas WHERE lote_id = ? ORDER BY id";
        List<Etiqueta> lista = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, loteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Etiqueta mapear(ResultSet rs) throws SQLException {
        Etiqueta e = new Etiqueta();
        e.setId(rs.getInt("id"));
        e.setLoteId(rs.getInt("lote_id"));
        e.setIdEmpleado(rs.getString("id_empleado"));
        e.setPrimerNombre(rs.getString("primer_nombre"));
        e.setSegundoNombre(rs.getString("segundo_nombre"));
        e.setPrimerApellido(rs.getString("primer_apellido"));
        e.setSegundoApellido(rs.getString("segundo_apellido"));

        String prenda = rs.getString("tipo_prenda");
        if (prenda != null) {
            try { e.setTipoPrenda(TipoPrenda.valueOf(prenda)); }
            catch (IllegalArgumentException ignored) {}
        }

        e.setTalla(rs.getString("talla"));
        return e;
    }
}
