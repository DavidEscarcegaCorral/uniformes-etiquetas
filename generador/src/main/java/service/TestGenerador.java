package service;

import model.Etiqueta;
import model.LoteEtiquetas;
import model.TipoPrenda;

import java.io.File;
import java.util.List;

public class TestGenerador {

    public static void main(String[] args) throws Exception {
        LoteEtiquetas lote = new LoteEtiquetas();
        lote.setNombre("Lote Prueba Mayo 2026");
        lote.setEmpresa("EMPRESA EJEMPLO S.A.");

        Object[][] datos = {
            {"417", "Juana",    "Karina",  "Vega",    "Cruz",     TipoPrenda.BLUSA,             "32"},
            {"418", "Pedro",    "",        "García",  "López",    TipoPrenda.PANTALON,          "34"},
            {"419", "María",    "Elena",   "Rodríguez","",        TipoPrenda.CHALECO,           "M" },
            {"420", "Carlos",   "",        "Mendoza", "Ruiz",     TipoPrenda.CAMISA_MANGA_LARGA,"L" },
            {"421", "Ana",      "Lucía",   "Torres",  "Gómez",    TipoPrenda.CAMISETA_POLO,     "S" },
            {"422", "Roberto",  "",        "Herrera", "",         TipoPrenda.CHAQUETA,          "XL"},
            {"423", "Sofía",    "Isabel",  "Jiménez", "Morales",  TipoPrenda.BLUSA,             "36"},
            {"424", "Luis",     "Ángel",   "Ramírez", "Castillo", TipoPrenda.PANTALON,          "30"},
            {"425", "Patricia", "",        "Flores",  "Díaz",     TipoPrenda.DELANTAL,          "M" },
            {"426", "Miguel",   "Antonio", "Castro",  "Vargas",   TipoPrenda.OVEROL,            "L" },
            {"427", "Laura",    "",        "Ortega",  "Soto",     TipoPrenda.BLUSA,             "34"},
            {"428", "Fernando", "José",    "Guerrero","Ramos",    TipoPrenda.CAMISA_MANGA_CORTA,"M" },
            {"429", "Gabriela", "Paola",   "Mendez",  "Silva",    TipoPrenda.CHALECO,           "S" },
            {"430", "Héctor",   "",        "Guzmán",  "Reyes",    TipoPrenda.PANTALON,          "36"},
            {"431", "Claudia",  "Beatriz", "Espinoza","",         TipoPrenda.CAMISETA_POLO,     "M" },
            {"432", "Andrés",   "",        "Medina",  "Cruz",     TipoPrenda.CHAQUETA,          "XXL"},
            {"433", "Valeria",  "Renata",  "Vázquez", "Ponce",    TipoPrenda.BLUSA,             "38"},
            {"434", "Ricardo",  "",        "Delgado", "Aguilar",  TipoPrenda.PANTALON,          "32"},
            {"435", "Diana",    "Ximena",  "Núñez",   "Peña",     TipoPrenda.BERMUDA,           "M" },
            {"436", "Ernesto",  "Manuel",  "Ríos",    "Campos",   TipoPrenda.OVEROL,            "L" },
            {"437", "Mónica",   "",        "Salinas", "Luna",     TipoPrenda.BLUSA,             "36"},
            {"438", "Joaquín",  "",        "Peña",    "Ibarra",   TipoPrenda.CAMISA_MANGA_LARGA,"L" },
            {"439", "Sandra",   "Cecilia", "Fuentes", "Rojas",    TipoPrenda.CHALECO,           "M" },
            {"440", "Marco",    "Antonio", "Lara",    "Montes",   TipoPrenda.PANTALON,          "34"},
            {"441", "Daniela",  "Fernanda","Ochoa",   "Vera",     TipoPrenda.CAMISETA_POLO,     "S" },
            {"442", "Alejandro","",        "Cortés",  "Blanco",   TipoPrenda.PANTALON,          "30"},
            {"443", "Verónica", "del Mar", "Carrillo","",         TipoPrenda.BLUSA,             "40"},
            {"444", "Gustavo",  "",        "Serrano", "Padilla",  TipoPrenda.CHALECO,           "XL"},
            {"445", "Leticia",  "Soledad", "Miranda", "Tapia",    TipoPrenda.DELANTAL,          "L" },
            {"446", "Rodrigo",  "",        "Romero",  "Santana",  TipoPrenda.CHAQUETA,          "M" },
            {"447", "Irene",    "Patricia","Sánchez", "Cervantes",TipoPrenda.BLUSA,             "32"},
            {"448", "Tomás",    "",        "Acosta",  "Vidal",    TipoPrenda.CAMISA_MANGA_CORTA,"L" },
            {"449", "Adriana",  "Victoria","Molina",  "Arroyo",   TipoPrenda.CHALECO,           "S" },
            {"450", "Enrique",  "Salvador","Rubio",   "Palma",    TipoPrenda.PANTALON,          "36"},
            {"451", "Rebeca",   "",        "Lozano",  "Quiñones", TipoPrenda.BLUSA,             "34"},
        };

        for (Object[] d : datos) {
            Etiqueta e = new Etiqueta();
            e.setIdEmpleado((String) d[0]);
            e.setPrimerNombre((String) d[1]);
            e.setSegundoNombre((String) d[2]);
            e.setPrimerApellido((String) d[3]);
            e.setSegundoApellido((String) d[4]);
            e.setTipoPrenda((TipoPrenda) d[5]);
            e.setTalla((String) d[6]);
            lote.agregarEtiqueta(e);
        }

        // Guardar en carpeta pruebas/ dentro del proyecto
        // user.dir al correr desde el módulo generador apunta a generador/
        // .. sube al directorio raíz del proyecto
        File carpeta = new File(System.getProperty("user.dir"), "../pruebas");
        carpeta.mkdirs();

        File destino = new File(carpeta, "test_etiquetas.docx");

        System.out.println("Etiquetas: " + lote.getTotalEtiquetas());
        System.out.println("Destino:   " + destino.getCanonicalPath());

        List<File> archivos = new GeneradorEtiquetas().generar(lote, destino);

        System.out.println("Archivos generados: " + archivos.size());
        archivos.forEach(f -> System.out.println("  -> " + f.getAbsolutePath()));

        java.awt.Desktop.getDesktop().open(archivos.get(0));
    }
}
