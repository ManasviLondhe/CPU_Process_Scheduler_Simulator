package simulation;

/** Discrete simulation time (one tick = one time unit). */
public class SimulationClock {
    private int now;

    public int now() {
        return now;
    }

    public void tick() {
        now++;
    }
}
