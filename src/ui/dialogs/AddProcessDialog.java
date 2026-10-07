package ui.dialogs;

import java.awt.Window;

import controller.ApplicationController;
import model.Process;

/** Dialog that adds a new process to the workload. */
public class AddProcessDialog extends ProcessDialog {
    private static final long serialVersionUID = 1L;
    private final transient ApplicationController app;

    public AddProcessDialog(Window owner, ApplicationController app) {
        super(owner, "Add Process", new Process(app.nextPid(), "", 0, 5, 1, 1));
        this.app = app;
    }

    @Override
    protected void commit(Process p) {
        app.addProcess(p);
    }
}
