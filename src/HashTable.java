
public class HashTable {

    private static final int DEFAULT_CAPACITY = 10;
    private static final double MAX_LOAD_FACTOR = 0.75;

    private static class Entry {
        private final String pid;
        private final Process process;
        private Entry next;

        private Entry(Process process, Entry next) {
            this.pid = process.getPid().trim();
            this.process = process;
            this.next = next;
        }
    }

    private Entry[] buckets;
    private int size;

    public HashTable() {
        buckets = new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    // Case-insensitive hash function
    private int bucketIndex(String pid) {
        return Math.floorMod(
            pid.trim().toLowerCase(java.util.Locale.ROOT).hashCode(),
            buckets.length
        );
    }

    // Insert a process
    public boolean insert(Process process) {
        if (process == null || process.getPid() == null
                || process.getPid().trim().isEmpty()) {
            return false;
        }

        if (search(process.getPid()) != null) {
            return false;
        }

        if ((double) (size + 1) / buckets.length
                > MAX_LOAD_FACTOR) {
            rehash();
        }

        int index = bucketIndex(process.getPid());
        buckets[index] = new Entry(process, buckets[index]);
        size++;

        return true;
    }

    // Search by PID
    public Process search(String pid) {
        if (pid == null || pid.trim().isEmpty()) {
            return null;
        }

        int index = bucketIndex(pid);
        Entry current = buckets[index];

        while (current != null) {
            if (current.pid.equalsIgnoreCase(pid.trim())) {
                return current.process;
            }

            current = current.next;
        }

        return null;
    }

    // Delete by PID
    public Process delete(String pid) {
        if (pid == null || pid.trim().isEmpty()) {
            return null;
        }

        int index = bucketIndex(pid);
        Entry current = buckets[index];
        Entry previous = null;

        while (current != null) {
            if (current.pid.equalsIgnoreCase(pid.trim())) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }

                size--;
                return current.process;
            }

            previous = current;
            current = current.next;
        }

        return null;
    }

    // Double capacity and redistribute all entries
    private void rehash() {
        Entry[] oldBuckets = buckets;
        buckets = new Entry[oldBuckets.length * 2];
        size = 0;

        for (Entry head : oldBuckets) {
            Entry current = head;

            while (current != null) {
                Entry next = current.next;
                int index = bucketIndex(current.pid);

                current.next = buckets[index];
                buckets[index] = current;

                size++;
                current = next;
            }
        }
    }

    // Remove all entries
    public void clear() {
        buckets = new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return buckets.length;
    }

    public double loadFactor() {
        return (double) size / buckets.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}