import dao.DatabaseManager;
import frames.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Inicializar base de datos antes de arrancar la UI
        DatabaseManager.getInstance().inicializar();

        // Ejecutar interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new MainFrame().setVisible(true);
        });
    }
}
