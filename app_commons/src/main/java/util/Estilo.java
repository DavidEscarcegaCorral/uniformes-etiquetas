package util;

import java.awt.*;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class Estilo {

    // Paleta
    public static final Color APP_COLOR    = new Color(24, 26, 32);   // fondo principal
    public static final Color INPUT_COLOR  = new Color(36, 39, 50);   // campos y listas
    public static final Color SEL_COLOR    = new Color(55, 95, 160);  // selección
    public static final Color BORDER_COLOR = new Color(65, 70, 85);   // bordes sutiles

    // Fonts
    public static final String FONT_OPEN_SANS_REG      = "OpenSans-Regular.ttf";
    public static final String FONT_OPEN_SANS_SEMIBOLD  = "OpenSans-Semibold.ttf";
    public static final String FONT_OPEN_SANS_COND_REG  = "OpenSans_Condensed-Regular.ttf";

    public static final float FONT_SIZE_BASE = 14f;

    /** Aplica la fuente global OpenSans y el tema oscuro a todos los componentes Swing. */
    public static void aplicarTema() {
        // ── Fuentes ───────────────────────────────────────────────────────────
        Font regular = FontLoader.cargarFont(FONT_OPEN_SANS_REG, FONT_SIZE_BASE);
        FontUIResource fui = new FontUIResource(regular);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) UIManager.put(key, fui);
        }

        // ── Colores globales ──────────────────────────────────────────────────
        Color bg  = APP_COLOR;
        Color inp = INPUT_COLOR;
        Color fg  = Color.WHITE;
        Color sel = SEL_COLOR;

        // Contenedores
        UIManager.put("Panel.background",              bg);
        UIManager.put("Panel.foreground",              fg);
        UIManager.put("SplitPane.background",          bg);
        UIManager.put("ScrollPane.background",         bg);
        UIManager.put("Viewport.background",           bg);

        // Etiquetas
        UIManager.put("Label.foreground",              fg);

        // Botones estándar (JButton sin customizar)
        UIManager.put("Button.background",             bg);
        UIManager.put("Button.foreground",             fg);

        // Campos de texto
        UIManager.put("TextField.background",          inp);
        UIManager.put("TextField.foreground",          fg);
        UIManager.put("TextField.caretForeground",     fg);
        UIManager.put("TextField.selectionBackground", sel);
        UIManager.put("TextField.selectionForeground", fg);
        UIManager.put("FormattedTextField.background", inp);
        UIManager.put("FormattedTextField.foreground", fg);

        // ComboBox
        UIManager.put("ComboBox.background",           inp);
        UIManager.put("ComboBox.foreground",           fg);
        UIManager.put("ComboBox.selectionBackground",  sel);
        UIManager.put("ComboBox.selectionForeground",  fg);

        // Lista
        UIManager.put("List.background",               inp);
        UIManager.put("List.foreground",               fg);
        UIManager.put("List.selectionBackground",      sel);
        UIManager.put("List.selectionForeground",      fg);

        // Tabla
        UIManager.put("Table.background",              inp);
        UIManager.put("Table.foreground",              fg);
        UIManager.put("Table.gridColor",               BORDER_COLOR);
        UIManager.put("Table.selectionBackground",     sel);
        UIManager.put("Table.selectionForeground",     fg);
        UIManager.put("TableHeader.background",        bg);
        UIManager.put("TableHeader.foreground",        fg);

        // OptionPane / diálogos
        UIManager.put("OptionPane.background",         bg);
        UIManager.put("OptionPane.messageForeground",  fg);
    }
}
