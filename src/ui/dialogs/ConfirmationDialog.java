package ui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

/** Yes/No confirmation helper. */
public final class ConfirmationDialog {
    private ConfirmationDialog() {
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
