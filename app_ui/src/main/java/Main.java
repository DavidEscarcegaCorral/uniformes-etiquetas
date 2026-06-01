import dao.DatabaseManager;
import frames.MainFrame;
import util.Estilo;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        DatabaseManager.getInstance().inicializar();
        DataSeeder.sembrarSiVacio();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            Estilo.aplicarTema();

            new MainFrame().setVisible(true);
        });
    }
}
