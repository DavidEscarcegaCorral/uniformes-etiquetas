package dao;

import model.LoteEtiquetas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoteDAO {

    public LoteEtiquetas guardar(LoteEtiquetas lote) throws SQLException {
        String sql = "INSERT INTO lotes (nombre, empresa, descripcion, fecha_creacion) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lote.getNombre());
            ps.setString(2, lote.getEmpresa());
            ps.setString(3, lote.getDescripcion());
            ps.setString(4, lote.getFechaCreacion());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                lote.setId(keys.getInt(1));
            }
        }
        return lote;
    }

    public List<LoteEtiquetas> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM lotes ORDER BY id DESC";
        List<LoteEtiquetas> lista = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void actualizar(LoteEtiquetas lote) throws SQLException {
        String sql = "UPDATE lotes SET nombre=?, empresa=?, descripcion=? WHERE id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lote.getNombre());
            ps.setString(2, lote.getEmpresa());
            ps.setString(3, lote.getDescripcion());
            ps.setInt(4, lote.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM lotes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private LoteEtiquetas mapear(ResultSet rs) throws SQLException {
        LoteEtiquetas lote = new LoteEtiquetas();
        lote.setId(rs.getInt("id"));
        lote.setNombre(rs.getString("nombre"));
        lote.setEmpresa(rs.getString("empresa"));
        lote.setDescripcion(rs.getString("descripcion"));
        lote.setFechaCreacion(rs.getString("fecha_creacion"));
        return lote;
    }
}
