package componentes;

import util.Estilo;
import util.FontLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class ComboBoxCustom<T> extends JComboBox<T> {

    public ComboBoxCustom(T[] items) {
        super(items);
        setup();
    }

    private void setup() {
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(FontLoader.cargarFont(Estilo.FONT_OPEN_SANS_REG, Estilo.FONT_SIZE_BASE));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? Estilo.SEL_COLOR : Estilo.INPUT_COLOR);
                setForeground(Color.WHITE);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    @Override
    public void updateUI() {
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        int[] xs = {cx - 5, cx + 5, cx};
                        int[] ys = {cy - 3, cy - 3, cy + 3};
                        g2.fillPolygon(xs, ys, 3);
                        g2.dispose();
                    }
                };
                btn.setPreferredSize(new Dimension(24, 24));
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}

            @Override
            protected ComboBoxEditor createEditor() {
                return new BasicComboBoxEditor() {
                    @Override
                    protected JTextField createEditorComponent() {
                        JTextField tf = super.createEditorComponent();
                        tf.setOpaque(false);
                        tf.setForeground(Color.WHITE);
                        tf.setCaretColor(Color.WHITE);
                        tf.setBorder(new EmptyBorder(0, 6, 0, 0));
                        return tf;
                    }
                };
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = 8;
        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {}
}
