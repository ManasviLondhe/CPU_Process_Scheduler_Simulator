package ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.Process;
import simulation.CPU;
import util.UIUtils;

/** Custom-painted CPU "chip" showing what the processor is doing right now. */
public class CpuPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private CPU.Status status = CPU.Status.IDLE;
    private transient Process process;
    private transient Process pending;
    private int time;
    private int slice;
    private int quantum = -1;
    private int switchRemaining;
    private String algorithm = "";

    public CpuPanel() {
        setBackground(UIUtils.WHITE);
        setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        setPreferredSize(new Dimension(300, 190));
        setMinimumSize(new Dimension(260, 150));
    }

    public void update(CPU cpu, int time, int quantum, String algorithm) {
        this.status = cpu.getStatus();
        this.process = cpu.getProcess();
        this.pending = cpu.getPendingProcess();
        this.slice = cpu.getSliceUsed();
        this.switchRemaining = cpu.getSwitchRemaining();
        this.time = time;
        this.quantum = quantum;
        this.algorithm = algorithm;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setFont(UIUtils.font(Font.BOLD, 13));
        g2.setColor(UIUtils.TEXT);
        g2.drawString("CPU", 14, 22);
        g2.setFont(UIUtils.font(Font.PLAIN, 12));
        g2.setColor(UIUtils.MUTED);
        g2.drawString(algorithm + "   |   t = " + time, 56, 22);

        int boxW = Math.min(w - 60, 250);
        int boxH = h - 62;
        int bx = (w - boxW) / 2;
        int by = 36;
        Color fill = new Color(0xF3F4F6);
        Color edge = new Color(0x9CA3AF);
        String head = "IDLE";
        String big = "-";
        String l1 = "No process on the CPU";
        String l2 = "";
        if (status == CPU.Status.RUNNING && process != null) {
            fill = UIUtils.ACCENT_LIGHT;
            edge = UIUtils.ACCENT;
            head = "RUNNING";
            big = process.getPid();
            l1 = "Remaining: " + process.getRemainingTime() + " / " + process.getBurstTime();
            l2 = "Queue " + process.getCurrentQueueLevel() + "   Priority " + process.getEffectivePriority()
                    + (quantum > 0 ? "   Slice " + slice + "/" + quantum : "");
        } else if (status == CPU.Status.CONTEXT_SWITCHING && pending != null) {
            fill = new Color(0xFEF3C7);
            edge = UIUtils.WARNING;
            head = "CONTEXT SWITCH";
            big = "\u2192 " + pending.getPid();
            l1 = "Switching, " + switchRemaining + " time unit(s) left";
            l2 = "Next burst remaining: " + pending.getRemainingTime();
        }
        g2.setColor(fill);
        g2.fillRoundRect(bx, by, boxW, boxH, 16, 16);
        g2.setColor(edge);
        g2.drawRoundRect(bx, by, boxW, boxH, 16, 16);
        // chip "pins"
        for (int i = 0; i < 5; i++) {
            int px = bx + 24 + i * (boxW - 48) / 4;
            g2.drawLine(px, by - 6, px, by);
            g2.drawLine(px, by + boxH, px, by + boxH + 6);
        }
        int bigSize = boxH >= 120 ? 32 : 24;
        center(g2, head, UIUtils.font(Font.BOLD, 12), edge, bx + boxW / 2, by + 18);
        center(g2, big, UIUtils.font(Font.BOLD, bigSize), UIUtils.TEXT, bx + boxW / 2, by + 20 + (boxH - 62) / 2 + bigSize / 2 + 4);
        center(g2, l1, UIUtils.font(Font.PLAIN, 12), UIUtils.TEXT, bx + boxW / 2, by + boxH - 26);
        center(g2, l2, UIUtils.font(Font.PLAIN, 11), UIUtils.MUTED, bx + boxW / 2, by + boxH - 10);
        g2.dispose();
    }

    private static void center(Graphics2D g2, String text, Font font, Color color, int cx, int baseline) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }
}
