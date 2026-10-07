# DSA Mapping (viva preparation)

## 1. Where each DSA concept appears
| Concept | Where | Notes |
|---|---|---|
| **Array** | `MyCircularQueue` (`Object[]`), `MinHeap` (`Object[]`), MLQ/MLFQ list of queues | doubling on overflow -> amortised O(1) |
| **Linked list** | `MyLinkedList` (completed processes), `QueueNode` chain in `MyQueue` | head+tail pointers |
| **Queue** | `MyQueue` | FCFS; FCFS levels |
| **Circular queue** | `MyCircularQueue` | Round Robin; index = (front + i) % capacity |
| **Priority queue** | `MyPriorityQueue` | SJF, SRTF, Priority, priority levels |
| **Heap** | `MinHeap` | arrivals, ready sets; siftUp/siftDown, heapify O(n) |
| **Sorting** | heap-sort inside `MinHeap.toSortedList()`; arrival ordering via heap | O(n log n) |
| **Searching** | `MyLinkedList.indexOf/contains` (linear), lookup of processes in lists | O(n) |
| **Complexity** | per structure in JavaDoc and `PROJECT_DOCUMENTATION.md` section 20 | |
| **Abstraction / polymorphism** | `QueueADT`, `Scheduler`, `AbstractScheduler`, `SchedulerFactory` | engine never knows which algorithm runs |

## 2. Data structure operations and complexity
| Structure | Operation | Time |
|---|---|---|
| MyLinkedList | add / addFirst / removeFirst | O(1) |
| | get(i), removeAt(i), removeValue, indexOf | O(n) |
| MyQueue | enqueue, dequeue, peek, size, isEmpty | O(1) |
| MyCircularQueue | enqueue (amortised), dequeue, peek, isFull, isEmpty, size | O(1) |
| MinHeap | insert, extractMin | O(log n) |
| | peek, size, isEmpty | O(1) |
| | heapify(collection), rebuild | O(n) |
| MyPriorityQueue | enqueue, dequeue | O(log n) |

## 3. Algorithm by algorithm
Notation: n = processes, T = simulated time units.

### FCFS
1. **Structure:** `MyQueue`. 2. **Why:** the policy *is* a FIFO. 3. **Logic:** admit arrivals in (arrival, PID) order, `dequeue` when CPU free, run to completion.
4. **Time:** O(n log n) arrival ordering (heap) + O(1) per decision. 5. **Space:** O(n).
6. **Example:** P1(0,5) P2(1,3) P3(2,2) -> P1 0-5, P2 5-8, P3 8-10.

### SJF (non-preemptive)
1. `MyPriorityQueue` (min-heap by burst, arrival, PID). 2. Need the minimum repeatedly: heap O(log n) vs linear scan O(n).
3. When the CPU is free, `extractMin`; run to completion. 4. O(n log n). 5. O(n).
6. Same workload -> P1 0-5, P3 5-7, P2 7-10.

### SRTF (preemptive SJF)
1. Min-heap by (remaining, arrival, PID). 2. `peek` answers "is someone shorter waiting?" in O(1).
3. Each time unit: if `heap.peek().remaining < running.remaining` -> push running back, `extractMin`. Strict `<` avoids useless switches.
4. O(n log n) decisions (each arrival/preemption O(log n)). 5. O(n).
6. P1(0,8) P2(1,4) P3(2,2) P4(3,1) -> P1 0-1, P2 1-2, P3 2-4, P4 4-5, P2 5-8, P1 8-15.

### Priority (preemptive, aging)
1. Min-heap by effective priority (direction configurable), arrival, PID. 2. Same as SRTF; aging changes keys, so `rebuild()` (O(n)) restores the heap.
3. Preempt on strictly better priority; aging: +1 counter per wait unit, at threshold improve priority by one step. 4. O(n log n) decisions, O(n) per aging tick. 5. O(n).
6. P1(0,6,3) P2(2,4,1) P3(3,2,2) -> P1 0-2, P2 2-6, P3 6-8, P1 8-12.

### Round Robin
1. `MyCircularQueue`. 2. Rotation = move front forward and enqueue at rear with wrap-around; no element shifting.
3. Run `quantum` units; if unfinished enqueue at rear (after same-instant arrivals); finished before quantum -> next immediately.
4. O(1) per decision, O(T/q + n) decisions. 5. O(n).
6. P1 5, P2 4, P3 3, q=2 -> P1 0-2, P2 2-4, P3 4-6, P1 6-8, P2 8-10, P3 10-11, P1 11-12.

### Multilevel Queue
1. Array of structures, one per class: Priority -> heap, RR -> circular queue, FCFS -> queue. 2. Each policy keeps its natural structure.
3. Serve the first non-empty level; a newly ready process in a higher level preempts a lower-level one. 4. O(L + log n) per decision. 5. O(n).
6. Background A(0,3), Interactive B(1,3), System C(2,2) -> A 0-1, B 1-2, C 2-4, B 4-6, A 6-8.

### Multilevel Feedback Queue
1. Array of queues (RR / FCFS levels) with movement of processes between them. 2. Short jobs finish in the top levels; long jobs sink.
3. Demote on full quantum, promote by aging, higher level preempts. 4. O(L + n) per tick with aging (scan of waiting processes). 5. O(n).
6. See test 6 in the documentation: aging inserts P1 into 6-8.

## 4. OOP concepts demonstrated
* **Abstraction:** `Scheduler`, `QueueADT`. **Inheritance (justified):** `AbstractScheduler` -> `AbstractHeapScheduler` -> SJF/SRTF/Priority; `MultiQueueScheduler` -> MLQ/MLFQ.
* **Polymorphism:** the engine calls `scheduler.pickNext()`; the object decides. **Encapsulation:** private fields, behaviour in methods (`Process.consumeOneUnit`).
* **Composition:** engine *has* a CPU, clock, event manager, scheduler. **Enums:** states, event types, algorithms, policies. **Factory/registry:** `SchedulerFactory`.

## 5. Likely viva questions
* *Why a heap and not a sorted array?* insert O(n) there, O(log n) here.
* *Why a circular queue for RR?* constant-time rotation without shifting.
* *What is `heapify`?* bottom-up build in O(n); used by `heapify()` and `rebuild()`.
* *How is starvation prevented?* aging (priority) and promotion (MLFQ).
* *Is SJF optimal?* it minimises average waiting time for non-preemptive scheduling when all jobs are available; SRTF for the preemptive case.
* *Why does a very small quantum hurt?* more context switches (see the what-if analysis).
