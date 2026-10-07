package ui.pages;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import controller.ApplicationController;
import util.UIUtils;

/** Project description, algorithms, data structures and team placeholders. */
public class AboutPanel extends BasePage {
    private static final long serialVersionUID = 1L;

    public AboutPanel(ApplicationController app) {
        super(app, "About", "CPU Process Scheduler Simulator and Performance Analyzer");
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(UIUtils.WHITE);
        grid.add(block("Purpose",
                "An educational tool that visualises how an operating system schedules processes on a CPU and "
                + "demonstrates the Data Structures and Algorithms behind each policy. The simulation is "
                + "discrete-time and event driven; nothing in the charts is pre-computed."));
        grid.add(block("Scheduling algorithms and data structures",
                "FCFS - MyQueue (linked FIFO)\n"
                + "SJF - MyPriorityQueue (min-heap by burst)\n"
                + "SRTF - min-heap by remaining time (preemptive)\n"
                + "Priority - min-heap with aging (preemptive)\n"
                + "Round Robin - MyCircularQueue (array)\n"
                + "MLQ - one queue per class, fixed-priority between queues\n"
                + "MLFQ - multiple queues, demotion + aging promotion\n"
                + "Also: MyLinkedList (completed list), MinHeap (future arrivals)"));
        grid.add(block("Project objectives",
                "1. Implement core DSA structures from scratch.\n"
                + "2. Implement seven scheduling algorithms with deterministic tie-breaking.\n"
                + "3. Simulate time, preemption, context switching and CPU idle periods.\n"
                + "4. Measure waiting, turnaround and response time, CPU utilization and throughput.\n"
                + "5. Compare algorithms on identical workloads."));
        grid.add(block("Team / project information (fill in)",
                "Project title: CPU Process Scheduler Simulator\n"
                + "Institute / Department: <placeholder>\n"
                + "Guide: <placeholder>\n"
                + "Member 1 - Data structures and process management: <name>\n"
                + "Member 2 - Scheduling algorithms: <name>\n"
                + "Member 3 - Simulation engine and analytics: <name>\n"
                + "Member 4 - Swing UI and visualisation: <name>"));
        body.add(grid, BorderLayout.CENTER);
    }

    private JPanel block(String title, String text) {
        JPanel p = UIUtils.card(new BorderLayout(0, 8));
        p.add(UIUtils.title(title), BorderLayout.NORTH);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UIUtils.font(java.awt.Font.PLAIN, 12));
        area.setForeground(UIUtils.TEXT);
        area.setBackground(UIUtils.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void onShow() {
        // static content
    }
}
