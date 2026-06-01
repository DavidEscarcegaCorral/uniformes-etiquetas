package componentes;

import util.Estilo;
import util.FontLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonCustom extends JButton {

    private boolean isHovered = false;

    public BotonCustom(String texto) {
        super(texto);
        setForeground(Color.WHITE);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
        });
        getModel().addChangeListener(e -> repaint());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setFont(FontLoader.cargarFont(Estilo.FONT_OPEN_SANS_SEMIBOLD, Estilo.FONT_SIZE_BASE));
        setForeground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 8;

        if (getModel().isPressed()) {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } else if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 28));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        }

        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
