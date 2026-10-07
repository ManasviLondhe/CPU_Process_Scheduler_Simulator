package ui.components;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import model.SimulationEvent;
import util.UIUtils;

/** Scrolling text log of simulation events. */
public class EventLogPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JTextArea area = new JTextArea();
    private int shown;

    public EventLogPanel() {
        super(new BorderLayout());
        setBackground(UIUtils.WHITE);
        area.setEditable(false);
        area.setFont(UIUtils.mono(12));
        area.setForeground(UIUtils.TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        javax.swing.JLabel title = UIUtils.title("Event Log");
        title.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        add(title, BorderLayout.NORTH);
        add(UIUtils.scroll(area), BorderLayout.CENTER);
    }

    /** Appends only the events that are not shown yet. */
    public void sync(List<SimulationEvent> events) {
        if (events.size() < shown) {
            area.setText("");
            shown = 0;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = shown; i < events.size(); i++) {
            sb.append(events.get(i)).append('\n');
        }
        if (sb.length() > 0) {
            area.append(sb.toString());
            area.setCaretPosition(area.getDocument().getLength());
        }
        shown = events.size();
    }

    public void clear() {
        area.setText("");
        shown = 0;
    }
}
