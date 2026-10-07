package comparison;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Process;
import model.SchedulingAlgorithmType;
import model.SimulationConfig;
import model.SimulationResult;
import simulation.SimulationEngine;
import validation.InputValidator;

/**
 * Runs the SAME workload through several configurations using the normal SimulationEngine and
 * collects the results. It contains no scheduling logic of its own.
 */
public class AlgorithmComparisonService {

    /** One run per selected algorithm; every run starts from a fresh copy of the workload. */
    public List<SimulationResult> compareAlgorithms(List<Process> workload, SimulationConfig baseConfig,
                                                    List<SchedulingAlgorithmType> algorithms) {
        InputValidator.validateWorkload(workload);
        Map<String, SimulationConfig> configs = new LinkedHashMap<>();
        for (SchedulingAlgorithmType type : algorithms) {
            SimulationConfig c = baseConfig.copy();
            c.setAlgorithm(type);
            configs.put(type.getDisplayName(), c);
        }
        return compareConfigurations(workload, configs);
    }

    /** What-if analysis for Round Robin: one run per quantum value. */
    public List<SimulationResult> compareQuantums(List<Process> workload, SimulationConfig baseConfig, int[] quantums) {
        InputValidator.validateWorkload(workload);
        Map<String, SimulationConfig> configs = new LinkedHashMap<>();
        for (int q : quantums) {
            if (q <= 0) {
                throw new IllegalArgumentException("Time quantum must be greater than 0.");
            }
            SimulationConfig c = baseConfig.copy();
            c.setAlgorithm(SchedulingAlgorithmType.ROUND_ROBIN);
            c.setTimeQuantum(q);
            configs.put("RR q=" + q, c);
        }
        return compareConfigurations(workload, configs);
    }

    /** General form: label -> configuration. */
    public List<SimulationResult> compareConfigurations(List<Process> workload, Map<String, SimulationConfig> configs) {
        List<SimulationResult> results = new ArrayList<>();
        for (Map.Entry<String, SimulationConfig> e : configs.entrySet()) {
            InputValidator.validateConfig(e.getValue());
            SimulationResult r = new SimulationEngine(workload, e.getValue()).runToCompletion();
            results.add(r.withLabel(e.getKey()));
        }
        return results;
    }
}
