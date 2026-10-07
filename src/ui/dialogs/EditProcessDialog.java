package ui.dialogs;

import java.awt.Window;

import controller.ApplicationController;
import model.Process;

/** Dialog that edits an existing process (the PID may be changed if it stays unique). */
public class EditProcessDialog extends ProcessDialog {
    private static final long serialVersionUID = 1L;
    private final transient ApplicationController app;
    private final String originalPid;

    public EditProcessDialog(Window owner, ApplicationController app, Process existing) {
        super(owner, "Edit Process " + existing.getPid(), existing);
        this.app = app;
        this.originalPid = existing.getPid();
    }

    @Override
    protected void commit(Process p) {
        app.updateProcess(originalPid, p);
    }
}
