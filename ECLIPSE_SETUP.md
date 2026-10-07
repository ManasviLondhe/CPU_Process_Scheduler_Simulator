# Setting up the project in Eclipse (step by step)

## 1. Prerequisites
1. Install a **JDK** (not only a JRE): Java 11 or newer, 17 or 21 recommended (https://adoptium.net).
2. Install **Eclipse IDE for Java Developers** (https://www.eclipse.org/downloads/).
3. Unzip `CPUSchedulerSimulator.zip` anywhere (for example `C:\Projects\` or `~/projects/`).

## 2. Import the project
1. Start Eclipse and pick a workspace.
2. Menu **File > Import...**
3. Choose **General > Existing Projects into Workspace** and click *Next*.
4. *Select root directory* > **Browse...** > pick the unzipped folder `CPUSchedulerSimulator`.
5. The project `CPUSchedulerSimulator` appears ticked. Leave "Copy projects into workspace" **unticked** (or tick it if you want a copy) and click **Finish**.

The project already contains `.project` and `.classpath`, so Eclipse knows `src` is the source folder and `bin` the output folder.

## 3. Check the Java version (only if you see red errors)
1. Right-click the project > **Properties > Java Build Path > Libraries**.
2. If `JRE System Library` shows *[unbound]* or an old version: select it > **Edit...** > choose *Workspace default JRE* (or *Alternate JRE* = your JDK 11+) > Finish.
3. **Properties > Java Compiler** > set *Compiler compliance level* to 11 or higher (17 recommended) > Apply.
4. **Project > Clean...** > Clean all projects.

## 4. Run the application
1. In *Package Explorer* open `src > main > Main.java`.
2. Right-click it > **Run As > Java Application** (or press the green Run button with `Main.java` selected).
3. The window "CPU Process Scheduler Simulator" opens on the Dashboard.

## 5. Run the tests
Right-click `src > tests > TestRunner.java` > **Run As > Java Application**. The Console should end with
`Passed: 192   Failed: 0` and `ALL TESTS PASSED`.

## 6. First simulation (2 minutes)
1. Sidebar **Processes** > pick *Test 3 - Round Robin* in the sample list > **Load Sample** (or add P1..P4 with **Add Process**).
2. Sidebar **Algorithms** > choose *Round Robin* > quantum `2` > **Apply Configuration**.
3. Sidebar **Simulation** > **Start** (or **Step** repeatedly). Watch CPU, ready queue, states, Gantt, event log.
4. After completion open **Results**; then **Compare** > *Run Comparison*.

## Troubleshooting
| Problem | Fix |
|---|---|
| "Selection does not contain a main type" | Select `Main.java` (package `main`) in the editor before pressing Run. |
| Red X on project, "unbound JRE" | Step 3 above; install a JDK, not just a JRE. |
| `UnsupportedClassVersionError` | The JRE used to run is older than the compiler level; use the same JDK for both (Step 3). |
| Window too big for a small screen | Drag to resize; the minimum size is 1050 x 680. |
| Project imported but `bin` empty | Eclipse builds automatically (**Project > Build Automatically** must be ticked). |

## IntelliJ IDEA / VS Code
* **IntelliJ:** File > Open > select the folder; mark `src` as *Sources Root*; run `main.Main`.
* **VS Code:** install "Extension Pack for Java", open the folder, run `Main.java` with the *Run* code lens.
