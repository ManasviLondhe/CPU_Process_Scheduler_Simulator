package main;

import javax.swing.SwingUtilities;

import controller.ApplicationController;
import ui.MainFrame;
import util.UIUtils;

/** Application entry point. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIUtils.installTheme();
            new MainFrame(new ApplicationController()).setVisible(true);
        });
    }
}
