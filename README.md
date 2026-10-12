# CPU Process Scheduler Simulator

This project simulates four CPU scheduling algorithms using the same process
workload:

- First Come First Served (FCFS)
- Shortest Job First (non-preemptive SJF)
- Non-preemptive Priority Scheduling
- Round Robin

The simulator displays execution order, a Gantt chart, per-process metrics,
averages, and CPU utilization.

## Requirements

- Java Development Kit (JDK) 8 or newer

## Compile and run

From the repository root:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory out
javac -d out src\*.java
java -cp out Main
```

Compiled files should be written to `out`, not to `src`.

## Input rules

- Process IDs are required and are unique without regard to letter case.
- Arrival time must be non-negative.
- Burst time must be greater than zero.
- Priority must be non-negative.
- A lower priority number represents a higher priority.
- Round Robin requires a positive time quantum.

## Metrics

```text
Turnaround Time = Completion Time - Arrival Time
Waiting Time    = Turnaround Time - Burst Time
Response Time   = First Start Time - Arrival Time
CPU Utilization = Busy Time / Elapsed Time * 100
```

SJF and Priority Scheduling are non-preemptive. Round Robin is preemptive and
records every time slice in its Gantt chart. Equal-arrival FCFS and Round Robin
processes retain their input order. SJF selects the ready process with the
shortest burst, then earliest arrival, then lexicographically smallest PID.
Priority Scheduling selects the ready process with the lowest priority number,
then earliest arrival, then lexicographically smallest PID.

CPU utilization uses elapsed time from time zero through the final completion
time, so any initial idle time before the first arrival is included.

## Data structures and complexity

- `ProcessManager` stores processes in insertion order and uses `HashTable` as
  its PID index. PID lookup is expected O(1), worst-case O(n); listing is O(n).
- `HashTable` uses separate chaining and resizes to keep its load factor
  controlled.
- `MinHeap` provides O(log n) insertion and removal and is used by SJF and
  Priority Scheduling. Their sorting and execution each run in O(n log n).
- `CircularQueue` provides O(1) enqueue/dequeue and is used by Round Robin.
  With arrival sorting, Round Robin runs in O(n log n + s), where s is the
  number of time slices.
- FCFS sorts by arrival time and then schedules in O(n log n).

## Run the checks

The project includes a dependency-free test harness:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory out
javac -d out src\*.java test\SchedulerTest.java
java -cp out SchedulerTest
```
