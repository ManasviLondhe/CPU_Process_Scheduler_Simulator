package scheduler;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import model.SchedulingAlgorithmType;
import model.SimulationConfig;

/**
 * Registry mapping an algorithm type to the constructor of its Scheduler.
 * No if/else chains: to add an algorithm create the class, add an enum constant
 * to SchedulingAlgorithmType and add one register(...) line below.
 */
public final class SchedulerFactory {
    private static final Map<SchedulingAlgorithmType, Function<SimulationConfig, Scheduler>> REGISTRY =
            new EnumMap<>(SchedulingAlgorithmType.class);

    static {
        register(SchedulingAlgorithmType.FCFS, FCFSScheduler::new);
        register(SchedulingAlgorithmType.SJF, SJFScheduler::new);
        register(SchedulingAlgorithmType.SRTF, SRTFScheduler::new);
        register(SchedulingAlgorithmType.PRIORITY, PriorityScheduler::new);
        register(SchedulingAlgorithmType.ROUND_ROBIN, RoundRobinScheduler::new);
        register(SchedulingAlgorithmType.MLQ, MultilevelQueueScheduler::new);
        register(SchedulingAlgorithmType.MLFQ, MLFQScheduler::new);
    }

    private SchedulerFactory() {
    }

    public static void register(SchedulingAlgorithmType type, Function<SimulationConfig, Scheduler> constructor) {
        REGISTRY.put(type, constructor);
    }

    public static Scheduler create(SimulationConfig config) {
        Function<SimulationConfig, Scheduler> constructor = REGISTRY.get(config.getAlgorithm());
        if (constructor == null) {
            throw new IllegalArgumentException("No scheduler registered for " + config.getAlgorithm());
        }
        return constructor.apply(config);
    }
}
