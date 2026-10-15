/**
 * คิวงานที่พร้อมถูกหยิบไปทำ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * สิ่งที่คลาสนี้ต้องทำได้:
 *   - เก็บงานที่รอ Worker อยู่
 *   - หยิบงานถัดไปตามนโยบายที่เลือก (FCFS หรือ Priority)
 *   - ถูกเรียกจากหลาย Thread พร้อมกันได้อย่างปลอดภัย
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้:
 *   - หัวข้อ 4: priority = 1 สูงสุด เมื่อเท่ากันต้องมีกติกาตัดสินลำดับ (tie-break)
 *     ที่ตัดสินจากข้อมูลของ Job ไม่ขึ้นกับว่า Thread ใดเข้าถึงคิวก่อน
 *   - หัวข้อ 7: ห้ามวนลูปเช็กแบบกิน CPU (busy waiting) — Worker ที่ไม่มีงานทำ
 *     ต้องถูกพักไว้ ไม่ใช่วนถามซ้ำ ๆ
 *
 * จะออกแบบเป็นคลาสเดียวที่รับนโยบายเข้ามา หรือแยกเป็นสองคลาส
 * หรือใช้โครงสร้างข้อมูลสำเร็จรูปของ Java ก็ได้ ขอให้อธิบายเหตุผลได้ใน Demo
 */
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Ready Queue สำหรับ FCFS / Priority
 */
public class ReadyQueue {

    private final Config.Policy policy;
    private final PriorityQueue<Job> queue;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private boolean closed = false;

    public ReadyQueue(Config.Policy policy) {
        this.policy = policy;
        this.queue = new PriorityQueue<>(comparatorFor(policy));
    }

    private static Comparator<Job> comparatorFor(Config.Policy policy) {

        if (policy == Config.Policy.FCFS) {

            /*
             * FCFS:
             * 1. arrivalMs น้อยกว่ามาก่อน
             * 2. sequence ใช้เป็น tie-break
             */
            return Comparator
                    .comparingLong((Job j) -> j.arrivalMs)
                    .thenComparingInt(j -> j.sequence);
        }

        /*
         * Priority:
         * priority = 1 สำคัญที่สุด
         *
         * 1. priority
         * 2. arrivalMs
         * 3. sequence
         */
        return Comparator
                .comparingInt((Job j) -> j.priority)
                .thenComparingLong(j -> j.arrivalMs)
                .thenComparingInt(j -> j.sequence);
    }

    /**
     * ใส่งานเข้าคิว
     * เรียกโดย Scheduler Thread
     */
    public void add(Job job) {

        if (job == null) {
            throw new IllegalArgumentException("job ห้ามเป็น null");
        }

        lock.lock();

        try {

            if (closed) {
                throw new IllegalStateException(
                        "ReadyQueue ถูกปิดแล้ว");
            }

            job.state = Job.State.READY;

            queue.offer(job);

            /*
             * ปลุก Worker ที่กำลังรออยู่
             */
            notEmpty.signal();

        } finally {
            lock.unlock();
        }
    }

    /**
     * หยิบงานถัดไป
     *
     * ถ้าไม่มีงานจะรอโดยไม่ busy-wait
     *
     * ถ้า queue ถูก close แล้ว และไม่มีงานเหลือ
     * จะคืน null เพื่อให้ Worker จบการทำงาน
     */
    public Job take() throws InterruptedException {

        lock.lockInterruptibly();

        try {

            while (queue.isEmpty() && !closed) {
                notEmpty.await();
            }

            /*
             * Queue ปิดแล้ว และไม่มีงานเหลือ
             */
            if (queue.isEmpty() && closed) {
                return null;
            }

            return queue.poll();

        } finally {
            lock.unlock();
        }
    }

    /**
     * ปิด ReadyQueue
     *
     * งานที่อยู่ใน queue ยังสามารถถูกทำต่อได้
     */
    public void close() {

        lock.lock();

        try {

            closed = true;

            /*
             * ปลุก Worker ทุกตัวที่กำลังรอ
             */
            notEmpty.signalAll();

        } finally {
            lock.unlock();
        }
    }

    /**
     * จำนวนงานที่กำลังรอ
     */
    public int size() {

        lock.lock();

        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean isClosed() {

        lock.lock();

        try {
            return closed;
        } finally {
            lock.unlock();
        }
    }
}