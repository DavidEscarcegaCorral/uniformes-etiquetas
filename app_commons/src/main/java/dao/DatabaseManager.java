package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:etiquetas.db";
    private static DatabaseManager instance;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.execute("PRAGMA journal_mode = WAL");
        }
        return conn;
    }

    public void inicializar() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            migrarSiEsNecesario(conn, stmt);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lotes (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre         TEXT NOT NULL,
                    empresa        TEXT,
                    descripcion    TEXT,
                    fecha_creacion TEXT NOT NULL
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS etiquetas (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    lote_id          INTEGER NOT NULL,
                    id_empleado      TEXT,
                    primer_nombre    TEXT NOT NULL,
                    segundo_nombre   TEXT,
                    primer_apellido  TEXT NOT NULL,
                    segundo_apellido TEXT,
                    tipo_prenda      TEXT NOT NULL,
                    talla            TEXT,
                    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE CASCADE
                )
                """);

        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar la base de datos: " + e.getMessage(), e);
        }
    }

    /** Descarta y recrea las tablas si detecta columnas del esquema anterior. */
    private void migrarSiEsNecesario(Connection conn, Statement stmt) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet tablas = meta.getTables(null, null, "etiquetas", null)) {
            if (!tablas.next()) return;
        }

        boolean esquemaViejo = false;
        for (String col : new String[]{"nombre_empleado", "numero_taller"}) {
            try (ResultSet cols = meta.getColumns(null, null, "etiquetas", col)) {
                if (cols.next()) { esquemaViejo = true; break; }
            }
        }

        if (esquemaViejo) {
            stmt.execute("DROP TABLE IF EXISTS etiquetas");
            stmt.execute("DROP TABLE IF EXISTS lotes");
        }
    }
}
