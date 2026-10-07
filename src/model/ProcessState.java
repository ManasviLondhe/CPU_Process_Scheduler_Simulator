package model;

/** Life-cycle states of a process. */
public enum ProcessState {
    /** Not yet arrived in the system. */
    NEW,
    /** In a ready queue (or selected and waiting for a context switch to finish). */
    READY,
    /** Currently executing on the CPU. */
    RUNNING,
    /** Reserved for future I/O-wait support; the CPU-only simulator never uses it. */
    WAITING,
    /** Finished execution. */
    COMPLETED
}
