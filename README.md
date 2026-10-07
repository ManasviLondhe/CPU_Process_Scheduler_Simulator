# CPU Process Scheduler Simulator and Performance Analyzer

A complete **Java Swing** desktop application (B.Tech Data Structures & Algorithms project) that simulates how an
operating system schedules processes on a CPU, visualises every step, and compares the algorithms.
Pure Java - **no external libraries, no build tool, no database**.

## Features
* 7 algorithms: **FCFS, SJF, SRTF, Priority (with aging), Round Robin, Multilevel Queue, Multilevel Feedback Queue**
* Real discrete-time simulation engine (not a pre-computed animation): start / pause / resume / **step** / stop / reset / restart / run-to-end, speed 0.5x - 10x
* Live **CPU view**, **ready-queue view** (linked queue, circular queue, heap order, multiple queues), process-state table, **Gantt chart**, event log
* Preemption, **context-switch cost**, CPU **idle** periods, starvation warnings, aging / promotion / demotion
* Metrics: completion, turnaround, waiting, response time; averages; CPU utilization; throughput; context switches; preemptions
* **Comparison page** with 5 custom-painted bar charts, plus Round-Robin **what-if (quantum) analysis**
* Process manager (add / edit / delete / validate), random workload generator with presets, CSV import / save, 7 ready-made test workloads
* Result export (CSV), session history
* **Custom data structures**: `MyLinkedList`, `MyQueue`, `MyCircularQueue`, `MinHeap`, `MyPriorityQueue`
* 192 automated checks with hand-verified expected Gantt charts and metrics

## Screenshots
_Add your own screenshots here (Dashboard, Simulation, Results, Compare)._

## Requirements
* JDK 11 or newer (JDK 17+ recommended) - https://adoptium.net
* Eclipse, IntelliJ IDEA, VS Code, or just a terminal

## Installation / Run
**Eclipse (recommended):** see `ECLIPSE_SETUP.md` (File > Import > Existing Projects into Workspace, then Run `main.Main`).

**Terminal:**
```
./run.sh        (Linux / macOS)
run.bat         (Windows)
```
**Tests:** `./test.sh`  or in Eclipse run `tests.TestRunner` as a Java Application.

## Project structure
```
CPUSchedulerSimulator/
  .project .classpath          Eclipse project files
  src/
    main/            Main.java (entry point)
    model/           Process, enums, config, segments, events, results
    datastructures/  MyLinkedList, MyQueue, MyCircularQueue, MinHeap, MyPriorityQueue, QueueADT, QueueNode
    scheduler/       Scheduler interface + 7 algorithms + SchedulerFactory
    simulation/      SimulationEngine, CPU, EventManager, ContextSwitchManager, SimulationClock
    metrics/         MetricsCalculator
    generator/       ProcessGenerator, WorkloadPreset, SampleWorkloads
    comparison/      AlgorithmComparisonService
    export/          SimulationExporter, WorkloadFileService
    history/         SimulationHistoryManager, HistoryEntry
    controller/      ApplicationController, SimulationController, PageId
    validation/      InputValidator
    util/            Constants, FormatUtils, UIUtils
    ui/              MainFrame, components/, pages/, dialogs/
    tests/           TestRunner + test classes (no JUnit needed)
  README.md  PROJECT_DOCUMENTATION.md  DSA_MAPPING.md  ARCHITECTURE.md  ECLIPSE_SETUP.md
```

## Algorithms and data structures (summary)
| Algorithm | Structure | Preemptive |
|---|---|---|
| FCFS | `MyQueue` | no |
| SJF | `MyPriorityQueue` (min-heap, burst) | no |
| SRTF | min-heap, remaining time | yes |
| Priority | min-heap, effective priority + aging | yes |
| Round Robin | `MyCircularQueue` | by quantum |
| MLQ | one structure per level + fixed-priority between levels | between levels |
| MLFQ | levels + demotion / aging promotion | between levels + quantum |

## Testing
`tests.TestRunner` checks every data structure, every algorithm (Gantt chart + per-process + average metrics),
context switching, aging, edge cases, the engine controls, comparison/what-if, metrics, validation and the generator.

## Future enhancements
HRRN scheduler, I/O bursts (the `WAITING` state is already reserved), persistent history file, PDF/PNG chart export,
multi-core CPU simulation, real-time (EDF / RMS) schedulers.
