/**
 * รวบรวมและคำนวณค่าที่ใช้วัดผลของการรันหนึ่งครั้ง
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้ (หัวข้อ 8):
 *   - Waiting Time, Turnaround Time, Throughput, Resource Wait Time
 *   - ต้องถูกอัปเดตจากหลาย Worker พร้อมกันได้อย่างปลอดภัย
 *   - ผลต้องสอดคล้องกับสมการตรวจสอบ:
 *       Turnaround = Waiting + workMs + Resource Wait + resourceMs
 *     ใช้สมการนี้ตรวจงานทีละชิ้นได้ว่าค่าไหนคำนวณผิด
 *
 * ข้อควรระวัง: ค่าเฉลี่ยของ Resource Wait ให้คิดเฉพาะงานที่ใช้ resource
 * ส่วนงานที่ resource = NONE ให้ถือว่า Resource Wait เป็น 0
 */
import java.util.List;

/**
 * Thread-safe statistics ของการรันหนึ่งครั้ง
 */
public class Statistics {

    private long totalWaitingMs = 0L;
    private long totalTurnaroundMs = 0L;
    private long totalResourceWaitMs = 0L;

    private int completedJobs = 0;
    private int runningJobs = 0;
    private int resourceJobs = 0;

    /**
     * Worker เริ่มทำ Job
     */
    public synchronized void jobStarted() {
        runningJobs++;
    }

    /**
     * บันทึก Job ที่เสร็จ
     */
    public synchronized void recordCompletion(Job job) {

        /*
         * Waiting Time
         *
         * actual arrival -> worker start
         */
        long waiting =
                job.startedAtMs
                        - job.enteredAtMs;

        /*
         * Turnaround Time
         *
         * actual arrival -> completion
         */
        long turnaround =
                job.completedAtMs
                        - job.enteredAtMs;

        totalWaitingMs += waiting;
        totalTurnaroundMs += turnaround;

        /*
         * Resource Wait
         * คิดเฉพาะ Job ที่ใช้ resource
         */
        if (job.resource != ResourceType.NONE) {

            totalResourceWaitMs +=
                    job.resourceWaitMs;

            resourceJobs++;
        }

        completedJobs++;
        runningJobs--;
    }

    /**
     * จำนวน Job ที่เสร็จแล้ว
     */
    public synchronized int completedCount() {
        return completedJobs;
    }

    /**
     * จำนวน Job ที่กำลังทำ
     */
    public synchronized int runningCount() {
        return runningJobs;
    }

    /**
     * แสดงผลสรุป
     */
    public synchronized void printSummary(
            List<Job> allJobs,
            long makespanMs) {

        int totalJobs = allJobs.size();

        long avgWaiting =
                totalJobs == 0
                        ? 0
                        : totalWaitingMs / totalJobs;

        long avgTurnaround =
                totalJobs == 0
                        ? 0
                        : totalTurnaroundMs / totalJobs;

        long avgResourceWait =
                resourceJobs == 0
                        ? 0
                        : totalResourceWaitMs
                            / resourceJobs;

        /*
         * Throughput:
         *
         * completed jobs / makespan in seconds
         */
        double throughput =
                makespanMs <= 0
                        ? 0.0
                        : completedJobs
                            / (makespanMs / 1000.0);

        System.out.println();
        System.out.println("========== SUMMARY ==========");

        System.out.println(
                "Jobs                 : "
                        + completedJobs
                        + "/"
                        + totalJobs
        );

        System.out.println(
                "Avg Waiting Time     : "
                        + avgWaiting
                        + " ms"
        );

        System.out.println(
                "Avg Turnaround Time  : "
                        + avgTurnaround
                        + " ms"
        );

        System.out.printf(
                "Throughput           : %.2f jobs/sec%n",
                throughput
        );

        System.out.println(
                "Avg Resource Wait    : "
                        + avgResourceWait
                        + " ms"
        );

        System.out.println(
                "Makespan             : "
                        + makespanMs
                        + " ms"
        );

        System.out.println(
                "============================="
        );
    }
}