package model;

/** Configuration of one queue level (name, policy and, for Round Robin, the quantum). */
public class QueueLevelConfig {
    private String name;
    private QueuePolicy policy;
    private int quantum;

    public QueueLevelConfig(String name, QueuePolicy policy, int quantum) {
        this.name = name;
        this.policy = policy;
        this.quantum = quantum;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public QueuePolicy getPolicy() { return policy; }
    public void setPolicy(QueuePolicy policy) { this.policy = policy; }
    public int getQuantum() { return quantum; }
    public void setQuantum(int quantum) { this.quantum = quantum; }

    public QueueLevelConfig copy() {
        return new QueueLevelConfig(name, policy, quantum);
    }

    @Override
    public String toString() {
        return name + "[" + policy + (policy == QueuePolicy.ROUND_ROBIN ? " q=" + quantum : "") + "]";
    }
}
