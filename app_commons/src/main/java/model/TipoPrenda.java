package model;

public enum TipoPrenda {

    BLUSA("Blusa"),
    CHALECO("Chaleco"),
    PANTALON("Pantalón"),
    CAMISA_MANGA_LARGA("Camisa Manga Larga"),
    CAMISA_MANGA_CORTA("Camisa Manga Corta"),
    CAMISETA_POLO("Camiseta Tipo Polo"),
    BERMUDA("Bermuda / Short"),
    CHAQUETA("Chaqueta"),
    OVEROL("Overol"),
    DELANTAL("Delantal"),
    GORRA("Gorra / Cap"),
    OTRO("Otro");

    private final String etiqueta;

    TipoPrenda(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
