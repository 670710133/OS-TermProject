/**
 * รับงานจาก JobGenerator แล้วจัดเข้า Ready Queue
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ (หัวข้อ 2 และ 4):
 *   - Scheduler เป็น Thread บังคับ ห้ามให้ JobGenerator ใส่งานลง Ready Queue โดยตรง
 *   - รับผิดชอบการจัดลำดับตามนโยบาย FCFS หรือ Priority
 *
 * ข้อควรคิด:
 *   - Scheduler รับงานจาก JobGenerator ผ่านอะไร และรอโดยไม่กิน CPU อย่างไร
 *   - เมื่อ JobGenerator ปล่อยงานครบแล้ว Scheduler รู้ได้อย่างไรว่าควรหยุด
 */
import java.util.concurrent.BlockingQueue;

/**
 * รับงานจาก JobGenerator แล้วส่งต่อเข้า ReadyQueue
 */
public class Scheduler extends Thread {

    /**
     * สัญญาณว่า JobGenerator ปล่อยงานครบแล้ว
     */
    public static final Job POISON_PILL =
            new Job(
                    "__SCHEDULER_STOP__",
                    0,
                    Integer.MAX_VALUE,
                    0,
                    ResourceType.NONE,
                    0,
                    Integer.MAX_VALUE
            );

    private final BlockingQueue<Job> inputQueue;
    private final ReadyQueue readyQueue;
    private final ProjectLogger logger;

    public Scheduler(
            BlockingQueue<Job> inputQueue,
            ReadyQueue readyQueue,
            ProjectLogger logger) {

        super("scheduler");

        this.inputQueue = inputQueue;
        this.readyQueue = readyQueue;
        this.logger = logger;
    }

    @Override
    public void run() {

        try {

            while (true) {

                /*
                 * รอรับงานจาก JobGenerator
                 */
                Job job = inputQueue.take();

                /*
                 * ถ้าได้รับ POISON_PILL
                 * แปลว่า Generator ไม่มีงานอีกแล้ว
                 */
                if (job == POISON_PILL) {

                    readyQueue.close();

                    return;
                }

                /*
                 * ส่งงานเข้า ReadyQueue
                 */
                readyQueue.add(job);

                logger.systemEvent(
                        job.id + " READY"
                );
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            readyQueue.close();
        }
    }
}