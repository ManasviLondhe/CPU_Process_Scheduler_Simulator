package ui.pages;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.ApplicationController;
import util.UIUtils;

/** Common skeleton of every page: title header + padded body. onShow() refreshes the page. */
public abstract class BasePage extends JPanel {
    private static final long serialVersionUID = 1L;

    protected final transient ApplicationController app;
    protected final JPanel body = new JPanel(new BorderLayout(0, 12));

    protected BasePage(ApplicationController app, String title, String subtitle) {
        super(new BorderLayout());
        this.app = app;
        setBackground(UIUtils.WHITE);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)));
        JLabel t = new JLabel(title);
        t.setFont(UIUtils.font(Font.BOLD, 20));
        t.setForeground(UIUtils.TEXT);
        header.add(t, BorderLayout.NORTH);
        header.add(UIUtils.muted(subtitle), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        body.setBackground(UIUtils.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        add(body, BorderLayout.CENTER);
    }

    /** Called every time the page becomes visible; reload data from the controller here. */
    public abstract void onShow();
}
