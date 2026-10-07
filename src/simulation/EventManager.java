package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.SimulationEvent;
import model.SimulationEventType;

/** Collects the chronological event log of one simulation. */
public class EventManager {
    private final List<SimulationEvent> events = new ArrayList<>();

    public void add(int time, SimulationEventType type, String pid, String message) {
        events.add(new SimulationEvent(time, type, pid, message));
    }

    public List<SimulationEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public int size() {
        return events.size();
    }
}
