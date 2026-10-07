package model;

/** Kinds of events recorded by the simulation engine. */
public enum SimulationEventType {
    PROCESS_ARRIVAL, CPU_START, CPU_COMPLETE, PREEMPTION, CONTEXT_SWITCH, CPU_IDLE,
    QUANTUM_EXPIRED, PROCESS_QUEUE_CHANGE, STARVATION_WARNING, SIMULATION_END
}
