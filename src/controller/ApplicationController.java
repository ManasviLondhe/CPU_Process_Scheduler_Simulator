package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import history.SimulationHistoryManager;
import model.Process;
import model.SimulationConfig;
import model.SimulationResult;
import validation.InputValidator;

/**
 * Application state shared by all pages: the workload, the algorithm configuration, the last result
 * and the history. Pages talk to this class, never to each other.
 */
public class ApplicationController {
    private final List<Process> processes = new ArrayList<>();
    private final SimulationHistoryManager history = new SimulationHistoryManager();
    private final SimulationController simulationController;
    private SimulationConfig config = new SimulationConfig();
    private SimulationResult lastResult;
    private Consumer<PageId> navigator;

    public ApplicationController() {
        this.simulationController = new SimulationController(this);
    }

    // ------------------------------------------------------------ workload

    public List<Process> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    public void addProcess(Process p) {
        InputValidator.validateProcess(p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(),
                p.getQueueLevel(), p.getName(), processes, null);
        processes.add(p);
    }

    public void updateProcess(String originalPid, Process updated) {
        InputValidator.validateProcess(updated.getPid(), updated.getArrivalTime(), updated.getBurstTime(),
                updated.getPriority(), updated.getQueueLevel(), updated.getName(), processes, originalPid);
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getPid().equals(originalPid)) {
                processes.set(i, updated);
                return;
            }
        }
        throw new IllegalArgumentException("Process " + originalPid + " no longer exists.");
    }

    public void removeProcess(String pid) {
        processes.removeIf(p -> p.getPid().equals(pid));
    }

    public void clearProcesses() {
        processes.clear();
    }

    public void replaceProcesses(List<Process> list) {
        processes.clear();
        processes.addAll(list);
    }

    public void appendProcesses(List<Process> list) {
        for (Process p : list) {
            addProcess(p);
        }
    }

    /** Next free number for generated PIDs (P1, P2, ...). */
    public int nextPidNumber() {
        int max = 0;
        for (Process p : processes) {
            String id = p.getPid();
            int i = id.length();
            while (i > 0 && Character.isDigit(id.charAt(i - 1))) {
                i--;
            }
            if (i < id.length() && id.length() - i < 9) {
                max = Math.max(max, Integer.parseInt(id.substring(i)));
            }
        }
        return max + 1;
    }

    public String nextPid() {
        return "P" + nextPidNumber();
    }

    // ------------------------------------------------------------ configuration / results

    public SimulationConfig getConfig() {
        return config;
    }

    public void setConfig(SimulationConfig config) {
        this.config = config;
    }

    public SimulationResult getLastResult() {
        return lastResult;
    }

    public void setLastResult(SimulationResult result) {
        this.lastResult = result;
    }

    /** Called when a simulation finished normally. */
    public void recordResult(SimulationResult result) {
        this.lastResult = result;
        history.add(result);
    }

    public SimulationHistoryManager getHistory() {
        return history;
    }

    public SimulationController getSimulationController() {
        return simulationController;
    }

    // ------------------------------------------------------------ navigation

    public void setNavigator(Consumer<PageId> navigator) {
        this.navigator = navigator;
    }

    public void navigateTo(PageId page) {
        if (navigator != null) {
            navigator.accept(page);
        }
    }
}
