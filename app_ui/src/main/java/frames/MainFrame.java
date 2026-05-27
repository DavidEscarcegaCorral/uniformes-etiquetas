package frames;

import panels.PanelPrincipal;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Sistema de Etiquetas — Uniformes Empresariales");
        construirUI();
    }

    private void construirUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        PanelPrincipal panel = new PanelPrincipal(this);
        add(panel);
    }
}
