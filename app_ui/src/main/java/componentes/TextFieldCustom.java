package componentes;

import util.Estilo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TextFieldCustom extends JTextField {

    public TextFieldCustom() {
        super();
        init();
    }

    public TextFieldCustom(int columns) {
        super(columns);
        init();
    }

    private void init() {
        setOpaque(false);
        setBorder(new EmptyBorder(6, 10, 6, 10));
        setSelectionColor(Estilo.SEL_COLOR);
        setSelectedTextColor(Color.WHITE);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = 8;
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {}
}
