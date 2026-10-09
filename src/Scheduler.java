import java.util.List;

public interface Scheduler {
    String getName();

    List<Process> schedule(List<Process> processes);
}
