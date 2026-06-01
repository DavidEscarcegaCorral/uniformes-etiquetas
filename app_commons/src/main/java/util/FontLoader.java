package util;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

    public static Font cargarFont(String ruta, float tamaño) {
        try (InputStream is = FontLoader.class.getResourceAsStream("/fonts/" + ruta)) {

            if (is == null) {
                System.err.println("No se encontró ninguna fuente en: " + ruta);
                return new Font("SansSerif", Font.PLAIN, (int) tamaño);
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            return font.deriveFont(tamaño);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) tamaño);
        }
    }
}
