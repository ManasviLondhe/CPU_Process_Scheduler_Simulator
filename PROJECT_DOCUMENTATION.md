# CPU Process Scheduler Simulator - Project Documentation

## 1. Project overview
A Java Swing application that simulates a CPU scheduling processes. The user creates processes, chooses a scheduling
algorithm, and watches the CPU, the ready queues, the process states, a live Gantt chart and an event log. Afterwards the
user can read the performance metrics and compare algorithms on the same workload.
It is a **Data Structures & Algorithms** project: each algorithm is built on a data structure implemented from scratch.

## 2. Problem statement
An operating system has many processes but (per core) one CPU. The *scheduler* decides who runs next. Different policies
give different waiting / response times and CPU utilization. Students need a tool that makes the decision process visible
and measurable.

## 3. Objectives
1. Implement linked list, queue, circular queue, min-heap and priority queue manually.
2. Implement FCFS, SJF, SRTF, Priority, Round Robin, MLQ and MLFQ with deterministic tie-breaking.
3. Build a discrete-time simulation engine independent from the GUI.
4. Visualise CPU, queues, states, Gantt chart; compute standard metrics; compare algorithms.
5. Keep the architecture modular so a new algorithm can be added in minutes.

## 4. Features
See `README.md`. Everything on screen is produced by the engine; there are no placeholder values, hard-coded charts or dead buttons.

## 5. Technologies
Java (17+ recommended, 11 works), Swing/AWT, standard library only. No JavaFX, Spring, JUnit or external charts: the
Gantt chart and the bar charts are drawn with `Graphics2D`.

## 6. Architecture
Layered, dependencies point downwards only (details and diagrams in `ARCHITECTURE.md`):
```
ui (pages, components, dialogs)  ->  controller  ->  simulation (engine, CPU, events)  ->  scheduler  ->  datastructures
                                                    \-> metrics     model is used by everybody
```
The engine and everything below it never import `javax.swing`, so the engine runs in the unit tests without a GUI.

## 7. Folder structure
See README. Eclipse files `.project` / `.classpath` are included; source root = `src`, output = `bin`.

## 8. File-by-file explanation
| File | Responsibility |
|---|---|
| `main/Main.java` | Starts Swing on the EDT, installs the theme, opens `MainFrame` |
| `model/Process.java` | Stores process identity and runtime state (remaining, state, start/completion, WT/TAT/RT, preemptions, switches, aging counters) |
| `model/ProcessState.java` | Enum NEW, READY, RUNNING, WAITING (reserved), COMPLETED |
| `model/SimulationState.java` | Enum NOT_STARTED, RUNNING, PAUSED, COMPLETED, STOPPED |
| `model/SchedulingAlgorithmType.java` | Enum of the algorithms with display name and description |
| `model/QueuePolicy.java`, `QueueLevelConfig.java` | Policy (FCFS/RR/Priority) and settings of one MLQ/MLFQ level |
| `model/SimulationConfig.java` | All user parameters: algorithm, quantum, priority direction, aging, context switch, starvation threshold, levels |
| `model/ScheduleSegment.java`, `SegmentType.java` | One Gantt block (process / idle / context switch) |
| `model/SimulationEvent.java`, `SimulationEventType.java` | Event log entries |
| `model/MetricsSummary.java` | Aggregated metrics value object |
| `model/SimulationResult.java` | Final result: config, processes, segments, events, metrics |
| `datastructures/QueueADT.java` | Common interface of the three queues |
| `datastructures/QueueNode.java` | Linked node |
| `datastructures/MyLinkedList.java` | Singly linked list (used for the *completed processes* list) |
| `datastructures/MyQueue.java` | Linked FIFO queue (FCFS, FCFS levels) |
| `datastructures/MyCircularQueue.java` | Array circular queue (Round Robin, RR levels) |
| `datastructures/MinHeap.java` | Array binary min-heap (arrival events, priority queues) |
| `datastructures/MyPriorityQueue.java` | `QueueADT` view of the heap (SJF, SRTF, Priority, priority levels) |
| `scheduler/Scheduler.java` | Strategy interface: admit, requeue, pickNext, shouldPreempt, quantumFor, onTimeUnit, snapshot |
| `scheduler/AbstractScheduler.java` | Shared defaults (non-preemptive, no quantum, listener) |
| `scheduler/AbstractHeapScheduler.java` | Shared single-heap behaviour for SJF/SRTF/Priority |
| `scheduler/FCFSScheduler.java` | FCFS with `MyQueue` |
| `scheduler/SJFScheduler.java` | Non-preemptive SJF |
| `scheduler/SRTFScheduler.java` | Preemptive SJF |
| `scheduler/PriorityScheduler.java` | Preemptive priority + aging |
| `scheduler/RoundRobinScheduler.java` | Round Robin with `MyCircularQueue` |
| `scheduler/MultiQueueScheduler.java` | Shared multi-level structure and inter-queue policy |
| `scheduler/MultilevelQueueScheduler.java` | MLQ (static class-to-queue mapping) |
| `scheduler/MLFQScheduler.java` | MLFQ (demotion, aging promotion) |
| `scheduler/RequeueReason.java` | PREEMPTED / QUANTUM_EXPIRED |
| `scheduler/SchedulerFactory.java` | Registry algorithm type -> constructor (no if/else chain) |
| `simulation/SimulationEngine.java` | Controls the simulation: clock, arrivals, dispatch, preemption, context switch, events, Gantt |
| `simulation/CPU.java` | CPU state: idle / running / context switching, time slice used |
| `simulation/EventManager.java` | Event log |
| `simulation/ContextSwitchManager.java` | Counts switches and their cost |
| `simulation/SimulationClock.java` | Discrete time |
| `metrics/MetricsCalculator.java` | Calculates performance (WT, TAT, RT, utilization, throughput, ...) |
| `generator/ProcessGenerator.java`, `WorkloadPreset.java` | Random workloads and presets |
| `generator/SampleWorkloads.java` | Predefined test workloads |
| `comparison/AlgorithmComparisonService.java` | Runs one workload through many configurations using the same engine |
| `export/SimulationExporter.java` | CSV export of results / comparison |
| `export/WorkloadFileService.java` | Save / load workloads as CSV |
| `history/SimulationHistoryManager.java`, `HistoryEntry.java` | Session history |
| `validation/InputValidator.java` | All validation and friendly error messages |
| `controller/ApplicationController.java` | Shared state: workload, config, last result, history, navigation |
| `controller/SimulationController.java` | Swing Timer that drives the engine; start/pause/step/stop/reset/speed |
| `controller/PageId.java` | Page identifiers |
| `util/Constants.java`, `FormatUtils.java`, `UIUtils.java` | Limits, formatting, theme helpers |
| `ui/MainFrame.java` | Window, sidebar + card layout |
| `ui/components/GanttChartPanel.java` | Draws Gantt chart |
| `ui/components/CpuPanel.java` | Draws CPU |
| `ui/components/ReadyQueuePanel.java` | Draws ready structures (HEAD/TAIL, circular arrow, multiple queues) |
| `ui/components/ProcessTablePanel.java` | Process / state table |
| `ui/components/EventLogPanel.java` | Event log view |
| `ui/components/BarChartPanel.java` | Custom bar chart |
| `ui/components/MetricCard.java`, `NavigationPanel.java`, `ThemedButton.java`, `LevelConfigEditor.java` | Small reusable UI pieces |
| `ui/pages/*Panel.java` | Dashboard, Processes, Algorithms, Simulation, Results, Compare, History, About |
| `ui/pages/BasePage.java` | Page skeleton with `onShow()` refresh |
| `ui/dialogs/*.java` | Add / Edit process, generator, confirmation |
| `tests/*.java` | Test runner and tests |

## 9. Process lifecycle
`NEW` (before arrival) -> `READY` (arrived, in a queue, or chosen and waiting for a context switch) -> `RUNNING` (on CPU)
-> back to `READY` when preempted / quantum expired -> `COMPLETED` when remaining time reaches 0.
(`WAITING` is reserved for a future I/O extension.)

## 10. CPU scheduling concepts
* **Burst time** - CPU time needed. **Arrival time** - when it enters the system. **Preemption** - taking the CPU from a running process.
* **Turnaround** = completion - arrival. **Waiting** = turnaround - burst. **Response** = first start - arrival.
* **Context switch** - saving one process and loading another; costs time and is pure overhead.
* **Starvation** - a process waits indefinitely. **Aging** - gradually raise the importance of long-waiting processes.

## 11-13. The algorithms, their data structure and why
| Algorithm | Rule | Structure | Why |
|---|---|---|---|
| FCFS | run in arrival order, no preemption | `MyQueue` | FIFO is exactly the rule; O(1) operations |
| SJF | smallest burst among arrived; ties: arrival, PID | min-heap | need "smallest" repeatedly: O(log n) vs O(n) scan |
| SRTF | like SJF with remaining time; preempt if a waiting process has *strictly* smaller remaining | min-heap | `peek()` is O(1) for the preemption test |
| Priority | best (effective) priority first; preempt on strictly better; ties: arrival, PID; optional aging | min-heap (`rebuild()` after aging) | same reasoning; heap rebuild is O(n) |
| Round Robin | quantum slices; expired process goes to the rear *after* same-instant arrivals | `MyCircularQueue` | rotation = wrap-around indices, no shifting |
| MLQ | process class fixes its queue; each queue has its own policy; **inter-queue: fixed priority, preemptive** (Q1 > Q2 > Q3) | one structure per level | each policy keeps its natural structure |
| MLFQ | start in level 1; **demote** after a full quantum; **promote by aging** after waiting `threshold` units; higher level preempts lower | levels of queues | processes move dynamically between structures |

### MLFQ rules (exact)
1. New processes enter level 1. 2. Level k is served only when levels < k are empty; a ready process of a higher level preempts
the running one (which goes to the tail of its own level). 3. Using a whole quantum in a non-last RR level demotes to the next level (tail);
in the last level it stays. 4. FCFS levels have no quantum. 5. With aging enabled each waiting process in level k>1 gains +1 counter per time unit;
at the threshold it moves to the tail of level k-1 and the counter restarts (also reset on dispatch / requeue).

### Priority aging rule (exact)
Every ready process has a counter +1 per time unit waited. At the *aging interval* its effective priority improves by 1 step
(`-1` if lower is higher, never below 0; `+1` otherwise) and the counter restarts. The boost is kept while the process runs and is lost when
it is re-queued after a preemption.

### Tie-breaking
SJF: burst, arrival, PID. SRTF: remaining, arrival, PID. Priority: priority, arrival, PID. FCFS/RR: queue insertion order
(arrivals at the same time are inserted by arrival, PID). PID order is *natural* (`P2` before `P10`).

## 14. Simulation engine working
One call of `advanceOneTimeUnit()` simulates `[t, t+1)`:
1. admit arrivals with `arrival <= t` (min-heap of future arrivals); 2. if a context switch just finished start the selected process;
else if a process is running check *quantum expiry* then *preemption*; 3. if the CPU is free ask the scheduler (`pickNext`) and start a
context switch if needed; 4. execute one unit: running process / context switch / idle, appending to the Gantt list (adjacent equal blocks are merged);
5. advance clock, update waiting counters and starvation warnings, call `scheduler.onTimeUnit()` (aging).
The simulation ends when all processes are completed.

## 15. Event handling
Types: PROCESS_ARRIVAL, CPU_START, CPU_COMPLETE, PREEMPTION, CONTEXT_SWITCH, CPU_IDLE, QUANTUM_EXPIRED, PROCESS_QUEUE_CHANGE (demotion,
promotion, aging, "continues"), STARVATION_WARNING, SIMULATION_END. The log is shown live and exported.

## 16. Context switching
A switch is **counted** whenever the CPU is given to a different process than the one that held it immediately before (after an idle gap there is no switch).
It **costs** `contextSwitchCost` time units only if enabled; those units appear as `CS` blocks in the Gantt chart, count as overhead (not as useful CPU time),
and the selected process stays `READY` (its waiting time grows) until the switch ends. Once a switch started, the choice is committed.

## 17. Preemption
Counted when the running process is returned to the queues and *another* process is chosen (SRTF, Priority, higher MLQ/MLFQ level, or Round-Robin / MLFQ quantum expiry
with another process waiting). If it is the only process, it simply continues - no preemption, no switch.

## 18. Gantt chart generation
The engine appends one unit per tick to a list of `ScheduleSegment`s (process, idle or CS). `GanttChartPanel` only paints that list.

## 19. Metrics
Formulas in section 10; **CPU utilization** = useful process time / total time x 100 (idle and context-switch time are not useful);
**throughput** = completed / total time; total time = completion time of the last process (the timeline starts at 0).

## 20. Complexity analysis (n processes, T total simulated time units, L levels)
| Part | Time | Space | Notes |
|---|---|---|---|
| `MyQueue`, `MyCircularQueue` | O(1) per op (circular: amortised) | O(n) | |
| `MinHeap` insert / extract | O(log n) | O(n) | peek O(1), heapify/rebuild O(n) |
| `MyLinkedList` get/remove/search | O(n) | O(n) | add at tail O(1) |
| Arrival handling | O(n log n) total | O(n) | heap |
| Scheduling decisions | O(log n) each (heap) / O(1) (queues) / O(L) (multilevel) | O(n) | |
| Engine tick | O(n) | | waiting-time update loops over all processes |
| **Whole simulation** | **O(T * n)** worst case (+ O(n log n) for decisions) | O(n + segments) | T = sum of bursts + idle + switch time |
| Aging (Priority) | O(n) per tick when enabled (+ O(n log n) snapshot, O(n) rebuild) | | |
| Gantt blocks | O(number of dispatches) | | merging keeps it small |
The *scheduling decisions themselves* are O(n log n) (SJF, SRTF, Priority) or O(n) (FCFS) as expected; the simulation adds the per-time-unit bookkeeping
so that every visual shows a real state.

## 21. UI pages
Dashboard, Processes, Algorithms, Simulation, Results, Compare, History, About. Each is a class in `ui/pages`.

## 22. UI-to-backend data flow
```
User input -> page -> ApplicationController (validates, stores workload/config)
Simulation page -> SimulationController.start() -> new SimulationEngine(workload copy, config)
Swing Timer tick -> engine.advanceOneTimeUnit() -> listeners -> SimulationPanel.refresh() reads engine getters -> repaint
on COMPLETED -> engine.buildResult() -> ApplicationController.recordResult -> Results page / History / Export
Compare page -> AlgorithmComparisonService -> N engines -> N SimulationResults -> table + charts
```

## 23. Testing
`tests.TestRunner` (192 checks). Hand-verified workloads (all arrival times in brackets):

| Test | Workload | Algorithm | Expected Gantt | Expected metrics |
|---|---|---|---|---|
| 1 | P1(0,5) P2(1,3) P3(2,2) | FCFS | P1 0-5, P2 5-8, P3 8-10 | WT 0/4/6 avg 3.33; TAT avg 6.67 |
| 1 | same | SJF | P1 0-5, P3 5-7, P2 7-10 | avg WT 3.00 |
| 1 | same | SRTF | P1 0-1, P2 1-4, P3 4-6, P1 6-10 | avg WT 2.33; P1 preempted once |
| 2 | P1(0,3) P2(8,4) | FCFS | P1 0-3, IDLE 3-8, P2 8-12 | util 58.33%, idle 5 |
| 3 | P1 5, P2 4, P3 3 all at 0 | RR q=2 | P1 0-2, P2 2-4, P3 4-6, P1 6-8, P2 8-10, P3 10-11, P1 11-12 | avg WT 7.00, TAT 11.00, RT 2.00, 6 switches, 4 preemptions |
| 3 | same, switch cost 1 | RR q=2 | 18 time units with 6 `CS` blocks | util 66.67% |
| 4 | P1(0,6,pr3) P2(2,4,pr1) P3(3,2,pr2) | Priority | P1 0-2, P2 2-6, P3 6-8, P1 8-12 | avg WT 3.00 |
| 5 | P1(0,8) P2(1,4) P3(2,2) P4(3,1) | SRTF | P1 0-1, P2 1-2, P3 2-4, P4 4-5, P2 5-8, P1 8-15 | avg WT 2.75, 2 preemptions |
| 6 | P1(0,6) S1(2,2) S2(4,2) S3(6,2) S4(8,2) | MLFQ [RR2, FCFS], no aging | P1 0-2, S1 2-4, S2 4-6, S3 6-8, S4 8-10, P1 10-14 | P1 starves until 10 |
| 6 | same, aging threshold 4 | MLFQ | P1 0-2, S1 2-4, S2 4-6, **P1 6-8**, S3 8-10, S4 10-12, P1 12-14 | promotion event logged |
| 7 | A(0,3,q3) B(1,3,q2) C(2,2,q1) | MLQ | A 0-1, B 1-2, C 2-4, B 4-6, A 6-8 | avg WT 2.33 |
Edge cases covered: single process, equal bursts / priorities, natural PID order, burst 100 000, late first arrival, arrival at completion,
arrival at quantum expiry, quantum larger than all bursts, single-level MLFQ == Round Robin, 150 processes in all 7 algorithms, engine
step / pause / resume / stop / reset, duplicate PID, invalid input, aging unit tests.

## 24. Edge cases
Handled explicitly: see the list above and the validator (`PID cannot be empty`, `PID already exists`, `Arrival time must be 0 or greater`,
`Burst time must be greater than 0`, `Time quantum must be greater than 0`, `Please add at least one process`, ...). A process whose queue class exceeds the number of MLQ queues
is placed in the last queue.

## 25. Future enhancements
HRRN, I/O bursts using the reserved `WAITING` state, multi-core simulation, EDF/RMS, saving history to disk, chart export as image.

## 26. How to run / 27. How to build
Eclipse: `ECLIPSE_SETUP.md`. Terminal: `./run.sh` / `run.bat`. Build manually: `javac -d bin $(find src -name "*.java")` then `java -cp bin main.Main`.
To produce a jar: `jar cfe Simulator.jar main.Main -C bin .`

## 28. How to add a new scheduling algorithm (example: HRRN)
1. Create `scheduler/HRRNScheduler.java` extending `AbstractHeapScheduler` (or implementing `Scheduler`); give it a comparator, e.g. highest response ratio first.
2. Add a constant `HRRN("HRRN", "description")` to `model/SchedulingAlgorithmType`.
3. Add `register(SchedulingAlgorithmType.HRRN, HRRNScheduler::new);` in `SchedulerFactory`.
4. The algorithm now appears in the Algorithms page, Simulation and Compare automatically. Add UI parameters only if it needs new settings.
No other class needs to change.
