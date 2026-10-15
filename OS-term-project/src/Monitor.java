/**
 * Thread ที่รายงานสถานะระบบเป็นระยะ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ (หัวข้อ 10):
 *   - รายงานประมาณทุก 1,000 ms ไม่ต้องแม่นตรงทุกครั้ง
 *   - อย่างน้อยต้องมี ready, running, completed และสถานะการใช้ resource
 *   - ข้อมูลที่อ่านต้องเป็น snapshot ที่ปลอดภัย
 *     โดยเฉพาะตัวนับ running ซึ่ง Worker หลายตัวเพิ่ม/ลดพร้อมกัน
 *   - ห้ามอ่าน collection หรือตัวนับที่กำลังถูกแก้ไขโดยไม่มีการป้องกัน
 *
 * ให้พิมพ์ผ่าน logger.monitor(ready, running, completed, resources.status())
 * เพื่อให้รูปแบบตรงกับกลุ่มอื่น
 *
 * ข้อควรคิด: ตัวนับ running ควรอยู่ที่ไหน ใครเป็นคนเพิ่มและลด
 * และจะอ่านพร้อมกับ ready กับ completed ให้เป็นภาพเดียวกันได้อย่างไร
 */
/**
 * Thread สำหรับรายงานสถานะระบบ
 */
/**
 * Thread สำหรับรายงาน snapshot ของระบบทุกประมาณ 1 วินาที
 */
public class Monitor extends Thread {

    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    private volatile boolean running = true;

    public Monitor(
            ReadyQueue readyQueue,
            ResourceManager resources,
            Statistics statistics,
            ProjectLogger logger) {

        super("monitor");

        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
    }

    @Override
    public void run() {

        try {

            while (running) {

                /*
                 * อ่าน snapshot ของสถานะ
                 */
                logger.monitor(
                        readyQueue.size(),
                        statistics.runningCount(),
                        statistics.completedCount(),
                        resources.status()
                );

                /*
                 * รายงานประมาณทุก 1 วินาที
                 */
                Thread.sleep(1000L);
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }

    /**
     * หยุด Monitor
     */
    public void shutdown() {

        running = false;

        interrupt();
    }
}