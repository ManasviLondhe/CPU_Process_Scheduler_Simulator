package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import util.UIUtils;

/** Flat rounded button in the application theme. */
public class ThemedButton extends JButton {
    private static final long serialVersionUID = 1L;

    public enum Kind { PRIMARY, SECONDARY, DANGER }

    private final Kind kind;
    private boolean hover;

    public ThemedButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setFont(UIUtils.font(Font.BOLD, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(kind == Kind.SECONDARY ? UIUtils.TEXT : Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public ThemedButton(String text) {
        this(text, Kind.SECONDARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill;
        if (!isEnabled()) {
            fill = new Color(0xEDF0F4);
        } else if (kind == Kind.PRIMARY) {
            fill = hover ? UIUtils.ACCENT_DARK : UIUtils.ACCENT;
        } else if (kind == Kind.DANGER) {
            fill = hover ? new Color(0x9B2C2C) : UIUtils.DANGER;
        } else {
            fill = hover ? UIUtils.ACCENT_LIGHT : Color.WHITE;
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        if (kind == Kind.SECONDARY || !isEnabled()) {
            g2.setColor(UIUtils.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
