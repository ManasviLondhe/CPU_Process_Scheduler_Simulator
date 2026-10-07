package scheduler;

/** Why a running process is returned to the ready structures. */
public enum RequeueReason {
    /** A better process is waiting (SRTF / Priority / higher MLQ-MLFQ level). */
    PREEMPTED,
    /** The process used its whole time slice. */
    QUANTUM_EXPIRED
}
