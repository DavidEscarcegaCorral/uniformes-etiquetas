import dao.DatabaseManager;
import model.TipoPrenda;

import java.sql.*;
import java.time.LocalDate;

public class SeedData {

    static final String[] PRIMER_NOMBRE  = {"Ana","Luis","Carlos","María","Jorge","Sofía","Pedro","Laura","Diego","Valentina","Andrés","Camila","Miguel","Natalia","Juan","Isabella","Felipe","Daniela","Roberto","Paola"};
    static final String[] SEGUNDO_NOMBRE = {null,"José","Alberto",null,"Enrique",null,"Antonio","Patricia",null,"Andrea","Hernán",null,"Ángel","Cristina","Manuel",null,"Esteban","Fernanda",null,"Alejandra"};
    static final String[] PRIMER_APE     = {"García","Martínez","López","Rodríguez","Pérez","González","Sánchez","Ramírez","Torres","Flores","Rivera","Morales","Jiménez","Herrera","Díaz","Vargas","Castro","Reyes","Ortiz","Mendoza"};
    static final String[] SEGUNDO_APE    = {"Ruiz","Moreno","Alonso","Navarro","Vega","Ramos","León","Soto","Fuentes","Aguilar","Cruz","Medina","Rojas","Silva","Guerrero","Ríos","Cabrera","Salinas","Núñez","Molina"};
    static final TipoPrenda[] PRENDAS    = TipoPrenda.values();
    static final String[] TALLAS         = {"XS","S","M","L","XL","XXL"};

    public static void main(String[] args) throws Exception {
        DatabaseManager.getInstance().inicializar();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            // Buscar o crear el lote
            int loteId = buscarOCrearLote(conn);

            // Contar registros actuales
            int existentes = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM etiquetas WHERE lote_id = ?")) {
                ps.setInt(1, loteId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) existentes = rs.getInt(1);
            }
            System.out.println("Registros existentes en el lote: " + existentes);

            int objetivo  = 35;
            int insertar  = Math.max(0, objetivo - existentes);
            if (insertar == 0) {
                System.out.println("El lote ya tiene " + existentes + " registros, no se inserta nada.");
                return;
            }

            String sql = """
                INSERT INTO etiquetas (lote_id, id_empleado, primer_nombre, segundo_nombre,
                                       primer_apellido, segundo_apellido, tipo_prenda, talla)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < insertar; i++) {
                    int idx = (existentes + i) % PRIMER_NOMBRE.length;
                    ps.setInt   (1, loteId);
                    ps.setString(2, String.format("EMP%03d", existentes + i + 1));
                    ps.setString(3, PRIMER_NOMBRE[idx]);
                    ps.setString(4, SEGUNDO_NOMBRE[idx]);
                    ps.setString(5, PRIMER_APE[idx]);
                    ps.setString(6, SEGUNDO_APE[(idx + 3) % SEGUNDO_APE.length]);
                    ps.setString(7, PRENDAS[i % PRENDAS.length].name());
                    ps.setString(8, TALLAS[i % TALLAS.length]);
                    ps.addBatch();
                }
                int[] r = ps.executeBatch();
                System.out.println("Insertados: " + r.length + " registros. Total: " + (existentes + r.length));
            }
        }
    }

    private static int buscarOCrearLote(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre FROM lotes ORDER BY id LIMIT 1")) {
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("Lote encontrado: id=" + id + "  nombre=" + rs.getString("nombre"));
                return id;
            }
        }

        // No hay lotes — crear uno de prueba
        System.out.println("No hay lotes. Creando 'Lote Uno'...");
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO lotes (nombre, empresa, descripcion, fecha_creacion) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Lote Uno");
            ps.setString(2, "Empresa de Prueba S.A.");
            ps.setString(3, "Lote de prueba para +30 registros");
            ps.setString(4, LocalDate.now().toString());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                System.out.println("Lote creado con id=" + id);
                return id;
            }
        }
        throw new RuntimeException("No se pudo crear el lote");
    }
}
