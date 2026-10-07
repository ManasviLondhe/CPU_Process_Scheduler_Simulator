package controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import model.SimulationState;
import simulation.SimulationEngine;
import util.Constants;
import validation.InputValidator;

/**
 * Connects the Simulation page to the SimulationEngine. It owns the Swing Timer that drives the engine
 * (one time unit per tick) so the Event Dispatch Thread is never blocked by a sleeping loop.
 */
public class SimulationController {

    /** Implemented by the Simulation page to be told when it must redraw. */
    public interface Listener {
        void simulationUpdated();
    }

    private final ApplicationController app;
    private final List<Listener> listeners = new ArrayList<>();
    private final Timer timer;
    private SimulationEngine engine;
    private boolean resultRecorded;
    private double speed = 1.0;

    public SimulationController(ApplicationController app) {
        this.app = app;
        this.timer = new Timer(Constants.BASE_TICK_MILLIS, e -> tick());
        this.timer.setRepeats(true);
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public SimulationEngine getEngine() {
        return engine;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
        int delay = (int) Math.max(10, Constants.BASE_TICK_MILLIS / speed);
        timer.setDelay(delay);
        timer.setInitialDelay(delay);
    }

    public boolean isTimerRunning() {
        return timer.isRunning();
    }

    /** Creates a fresh engine from the current workload/configuration (throws on invalid input). */
    private void createEngine() {
        InputValidator.validateWorkload(app.getProcesses());
        InputValidator.validateConfig(app.getConfig());
        engine = new SimulationEngine(app.getProcesses(), app.getConfig());
        resultRecorded = false;
    }

    /** Silently prepares an idle engine so the page can show the current workload. */
    public void prepare() {
        if (engine != null && engine.getStatus() != SimulationState.NOT_STARTED) {
            return;
        }
        try {
            createEngine();
        } catch (IllegalArgumentException e) {
            engine = null;
        }
        fire();
    }

    public void start() {
        if (engine == null || engine.isFinished()) {
            createEngine();
        }
        if (engine.getStatus() == SimulationState.NOT_STARTED) {
            engine.startSimulation();
        } else if (engine.getStatus() == SimulationState.PAUSED) {
            engine.resumeSimulation();
        }
        timer.start();
        fire();
    }

    public void pause() {
        timer.stop();
        if (engine != null) {
            engine.pauseSimulation();
        }
        fire();
    }

    public void resume() {
        if (engine != null && engine.getStatus() == SimulationState.PAUSED) {
            engine.resumeSimulation();
            timer.start();
            fire();
        }
    }

    /** Executes exactly one time unit. */
    public void step() {
        timer.stop();
        if (engine == null) {
            createEngine();
        }
        if (engine.isFinished()) {
            return;
        }
        engine.pauseSimulation();
        engine.stepSimulation();
        afterTick();
    }

    public void stop() {
        timer.stop();
        if (engine != null) {
            engine.stopSimulation();
        }
        fire();
    }

    public void reset() {
        timer.stop();
        engine = null;
        prepare();
    }

    public void restart() {
        reset();
        start();
    }

    /** Finishes the whole simulation immediately. */
    public void runToEnd() {
        timer.stop();
        if (engine == null || engine.isFinished()) {
            createEngine();
        }
        engine.startSimulation();
        while (!engine.isFinished()) {
            engine.advanceOneTimeUnit();
        }
        afterTick();
    }

    private void tick() {
        if (engine == null || engine.isFinished()) {
            timer.stop();
            return;
        }
        engine.advanceOneTimeUnit();
        afterTick();
    }

    private void afterTick() {
        if (engine.getStatus() == SimulationState.COMPLETED) {
            timer.stop();
            if (!resultRecorded) {
                resultRecorded = true;
                app.recordResult(engine.buildResult());
            }
        }
        fire();
    }

    private void fire() {
        for (Listener l : listeners) {
            l.simulationUpdated();
        }
    }
}
