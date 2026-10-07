# Architecture

## 1. High-level architecture
```
 +---------------------------------------------------------------+
 |  UI  (ui.pages, ui.components, ui.dialogs, ui.MainFrame)      |  Swing only, no logic
 +-------------------------------+-------------------------------+
                                 | calls / listens
 +-------------------------------v-------------------------------+
 |  Controller (ApplicationController, SimulationController)     |  state + Swing Timer
 +-----------+-------------------+-------------------+-----------+
             |                   |                   |
 +-----------v-----+   +---------v---------+  +------v----------------------+
 | validation      |   | simulation        |  | comparison / export /       |
 | InputValidator  |   | SimulationEngine  |  | history / generator         |
 +-----------------+   | CPU, EventManager |  +-----------------------------+
                       | ContextSwitchMgr  |
                       +----+---------+----+
                            |         |
                  +---------v--+  +---v-----------+
                  | scheduler  |  | metrics       |
                  | Scheduler  |  | MetricsCalc   |
                  +-----+------+  +---------------+
                        |
                  +-----v--------------+
                  | datastructures     |
                  | MyQueue, Circular, |
                  | MinHeap, ...       |
                  +--------------------+
        model (Process, config, results) is shared by all layers
```
**Dependency direction:** UI -> controller -> simulation -> scheduler -> datastructures. Lower layers never reference upper layers; `simulation` and below have no Swing imports.

## 2. Package structure
See README. Responsibilities per file: `PROJECT_DOCUMENTATION.md` section 8.

## 3. Class relationships
```
Scheduler <<interface>>
   ^
AbstractScheduler
   ^                         ^
AbstractHeapScheduler     MultiQueueScheduler            FCFSScheduler, RoundRobinScheduler
   ^   ^   ^                 ^        ^                  (extend AbstractScheduler directly)
 SJF SRTF Priority          MLQ      MLFQ

QueueADT<T> <<interface>>  <-  MyQueue, MyCircularQueue, MyPriorityQueue(-> MinHeap)
SimulationEngine --has--> Scheduler, CPU, SimulationClock, EventManager, ContextSwitchManager, MinHeap(arrivals), MyLinkedList(completed)
SchedulerFactory --creates--> Scheduler (from SimulationConfig)
ApplicationController --has--> workload, SimulationConfig, SimulationResult, SimulationHistoryManager, SimulationController
SimulationController --drives--> SimulationEngine (javax.swing.Timer)
```

## 4. Data flow
```
User Input -> Page -> ApplicationController (InputValidator) -> Process list + SimulationConfig
 -> SimulationController.start -> SimulationEngine(copy of workload) -> Scheduler (decision) -> Data structure
 -> CPU state + Gantt segments + events -> (listener) -> SimulationPanel.refresh -> custom painted components
 -> on completion: SimulationResult (MetricsCalculator) -> Results page / History / SimulationExporter
 -> Compare page: AlgorithmComparisonService -> many engines -> many results -> table + charts
```

## 5. UI flow
`MainFrame` holds a `NavigationPanel` (sidebar) and a `CardLayout` with one `BasePage` per `PageId`. Choosing a page calls `page.onShow()` (reload from the
controller) and shows the card. Pages navigate programmatically through `ApplicationController.navigateTo(PageId)`.
Typical path: Dashboard -> Processes -> Algorithms -> Simulation -> Results -> Compare -> History.

## 6. Simulation flow (one time unit)
```
advanceOneTimeUnit():
  admitArrivals(t)                       arrivals heap -> scheduler.admit
  if context switch finished: start pending process
  else if running: quantum expired? -> requeue(QUANTUM_EXPIRED)
                   else shouldPreempt? -> requeue(PREEMPTED)
  if CPU free: pickNext() -> (preemption event) -> context switch? -> startProcess
  executeOneUnit(): process | context switch | idle  -> Gantt segment
  clock.tick(); updateWaiting(); scheduler.onTimeUnit()   (aging, starvation warning)
```

## 7. Scheduler interaction
The engine owns *time*; the scheduler owns *order*. Contract: `admit` (new arrival), `requeue(reason)` (preempted / quantum expired),
`pickNext` (remove best), `shouldPreempt(running)`, `quantumFor(process)`, `onTimeUnit()`, `queueSnapshot()` for the UI, and an optional listener to report demotion/promotion/aging.
Because the engine only uses this interface, adding an algorithm never touches the engine.

## 8. Process lifecycle
```
 NEW --arrival--> READY --dispatch--> RUNNING --finish--> COMPLETED
                    ^                    |
                    +---- preempt / quantum expiry
```
During a context switch the chosen process is still READY.

## 9. Threading
Swing Timer fires on the Event Dispatch Thread; each tick advances the engine one unit (milliseconds of work), so the UI stays responsive.
"Run to End" and comparisons run synchronously (they are O(T * n) and finish quickly for the supported sizes).
The engine itself is single-threaded and GUI-independent, so tests run it directly.
