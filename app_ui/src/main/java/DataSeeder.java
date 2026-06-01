import dao.EtiquetaDAO;
import dao.LoteDAO;
import model.Etiqueta;
import model.LoteEtiquetas;
import model.TipoPrenda;

import java.sql.SQLException;
import java.util.List;

public class DataSeeder {

    public static void sembrarSiVacio() {
        try {
            LoteDAO loteDAO = new LoteDAO();
            if (!loteDAO.buscarTodos().isEmpty()) return;

            sembrarLote1(loteDAO, new EtiquetaDAO());
            sembrarLote2(loteDAO, new EtiquetaDAO());
        } catch (SQLException e) {
            System.err.println("[DataSeeder] Error al sembrar datos: " + e.getMessage());
        }
    }

    private static void sembrarLote1(LoteDAO loteDAO, EtiquetaDAO etiquetaDAO) throws SQLException {
        LoteEtiquetas lote = new LoteEtiquetas();
        lote.setNombre("Dotación Semestre I-2026");
        lote.setEmpresa("Constructora Parra & Cía");
        lote.setDescripcion("Uniformes operativos primer semestre — obra Norte");
        lote.setEtiquetas(List.of(
            et("001", "Carlos",   "Alberto", "Hernandez", "Ruiz",    TipoPrenda.OVEROL,            "L"),
            et("002", "Maria",    "Eugenia", "Lopez",     "Torres",  TipoPrenda.CHALECO,           "M"),
            et("003", "Jose",     "",        "Martinez",  "Gomez",   TipoPrenda.CAMISA_MANGA_LARGA,"XL"),
            et("004", "Ana",      "Lucia",   "Ramirez",   "",        TipoPrenda.PANTALON,          "34"),
            et("005", "Ricardo",  "",        "Suarez",    "Perez",   TipoPrenda.CAMISA_MANGA_CORTA,"L"),
            et("006", "Diana",    "Carolina","Vargas",    "Castillo",TipoPrenda.CHAQUETA,          "S"),
            et("007", "Luis",     "Miguel",  "Moreno",    "",        TipoPrenda.GORRA,             "U"),
            et("008", "Sandra",   "",        "Jimenez",   "Ospina",  TipoPrenda.OVEROL,            "M")
        ));

        loteDAO.guardar(lote);
        lote.getEtiquetas().forEach(e -> e.setLoteId(lote.getId()));
        etiquetaDAO.guardarTodas(lote.getEtiquetas());
    }

    private static void sembrarLote2(LoteDAO loteDAO, EtiquetaDAO etiquetaDAO) throws SQLException {
        LoteEtiquetas lote = new LoteEtiquetas();
        lote.setNombre("Uniformes Temporada 2026");
        lote.setEmpresa("Hotel Dann Carlton Bogotá");
        lote.setDescripcion("Dotación anual personal de servicio y cocina");
        lote.setEtiquetas(List.of(
            et("H01", "Valentina", "",        "Castro",    "Mejia",   TipoPrenda.BLUSA,             "S"),
            et("H02", "Andres",    "Felipe",  "Gutierrez", "Diaz",    TipoPrenda.CAMISA_MANGA_LARGA,"M"),
            et("H03", "Paola",     "Andrea",  "Rios",      "",        TipoPrenda.DELANTAL,          "M"),
            et("H04", "Miguel",    "",        "Sanchez",   "Lara",    TipoPrenda.PANTALON,          "32"),
            et("H05", "Laura",     "Isabel",  "Pineda",    "Acosta",  TipoPrenda.BLUSA,             "XS"),
            et("H06", "Fernando",  "Jose",    "Cardenas",  "",        TipoPrenda.CHAQUETA,          "XL"),
            et("H07", "Claudia",   "",        "Mendez",    "Villa",   TipoPrenda.CAMISETA_POLO,     "L"),
            et("H08", "Sebastian", "David",   "Romero",    "Cruz",    TipoPrenda.PANTALON,          "30"),
            et("H09", "Gloria",    "Patricia","Herrera",   "Soto",    TipoPrenda.DELANTAL,          "L"),
            et("H10", "Camilo",    "",        "Vega",      "Munoz",   TipoPrenda.CAMISA_MANGA_CORTA,"M")
        ));

        loteDAO.guardar(lote);
        lote.getEtiquetas().forEach(e -> e.setLoteId(lote.getId()));
        etiquetaDAO.guardarTodas(lote.getEtiquetas());
    }

    private static Etiqueta et(String id, String pNom, String sNom,
                                String pApe, String sApe,
                                TipoPrenda prenda, String talla) {
        Etiqueta e = new Etiqueta();
        e.setIdEmpleado(id);
        e.setPrimerNombre(pNom);
        e.setSegundoNombre(sNom);
        e.setPrimerApellido(pApe);
        e.setSegundoApellido(sApe);
        e.setTipoPrenda(prenda);
        e.setTalla(talla);
        return e;
    }
}
