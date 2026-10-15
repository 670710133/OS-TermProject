/**
 * Thread ที่ดึงงานจาก Ready Queue ไปทำจนเสร็จ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ลำดับการทำงานของ Job หนึ่งชิ้น บังคับตามหัวข้อ 6 ของโจทย์:
 *   1. รับงานจาก Ready Queue แล้วบันทึกเวลาเริ่ม
 *   2. จำลองงานหลักด้วย Thread.sleep(job.workMs)
 *   3. ถ้า job.resource != NONE ให้บันทึกเวลาเริ่มรอ แล้ว acquire
 *   4. จำลองการถือครองด้วย Thread.sleep(job.resourceMs)
 *   5. release แล้วบันทึกเวลาจบ
 *
 * ห้ามสลับขั้นที่ 2 กับ 3 เพราะจะทำให้ผลของทุกกลุ่มเทียบกันไม่ได้
 *
 * จุดที่มักพลาด:
 *   - ถ้า exception หรือ interrupt เกิดขึ้นหลัง acquire แต่ก่อน release
 *     permit จะค้างถาวรและระบบจะแขวน ต้องออกแบบให้คืนได้เสมอ
 *   - Worker ต้องหยุดเองได้เมื่อไม่มีงานเหลือแล้ว ไม่ใช่วนรอตลอดไป
 */
/**
 * Thread ที่ดึงงานจาก ReadyQueue
 */
/**
 * Worker ที่ดึงงานจาก ReadyQueue แล้วทำตาม lifecycle
 */
public class Worker extends Thread {

    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    public Worker(
            String name,
            ReadyQueue readyQueue,
            ResourceManager resources,
            Statistics statistics,
            ProjectLogger logger) {

        super(name);

        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
    }

    @Override
    public void run() {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                /*
                 * รอรับงาน
                 */
                Job job = readyQueue.take();

                /*
                 * null = queue ปิดแล้วและไม่มีงานเหลือ
                 */
                if (job == null) {
                    return;
                }

                processJob(job);
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }

    /**
     * ทำงานหนึ่ง Job ตามลำดับที่โจทย์กำหนด
     */
    private void processJob(Job job)
            throws InterruptedException {

        /*
         * ==================================================
         * 1. Worker รับ Job และบันทึกเวลาเริ่ม
         * ==================================================
         */
        job.startedAtMs = logger.now();

        job.state = Job.State.RUNNING;

        statistics.jobStarted();

        logger.jobStarted(job);


        /*
         * ==================================================
         * 2. ทำงานหลัก
         * ==================================================
         */
        Thread.sleep(job.workMs);

        logger.workFinished(job);


        boolean acquired = false;

        try {

            /*
             * ==================================================
             * 3. ถ้าต้องใช้ resource -> เริ่มรอแล้ว acquire
             * ==================================================
             */
            if (job.resource != ResourceType.NONE) {

                job.resourceWaitStartMs = logger.now();

                job.state = Job.State.WAITING_RESOURCE;

                logger.resourceWaitStarted(job);

                /*
                 * รอจนได้ permit
                 */
                resources.acquire(job.resource);

                /*
                 * สำคัญ:
                 * ตั้ง true หลัง acquire สำเร็จเท่านั้น
                 */
                acquired = true;

                long acquiredAtMs =
                        logger.now();

                job.resourceWaitMs =
                        acquiredAtMs
                                - job.resourceWaitStartMs;

                logger.resourceAcquired(
                        job,
                        job.resourceWaitMs
                );

                /*
                 * ได้ resource แล้ว
                 * กลับมาอยู่ในสถานะ RUNNING
                 */
                job.state = Job.State.RUNNING;


                /*
                 * ==================================================
                 * 4. ถือ resource
                 * ==================================================
                 */
                Thread.sleep(job.resourceMs);
            }

        } finally {

            /*
             * ==================================================
             * 5. คืน resource
             *
             * ถ้า acquire สำเร็จแล้ว ต้อง release เสมอ
             * แม้ Thread.sleep จะถูก interrupt
             * ==================================================
             */
            if (acquired) {

                resources.release(job.resource);

                logger.resourceReleased(job);
            }
        }


        /*
         * ==================================================
         * 6. Job เสร็จ
         * ==================================================
         */
        job.completedAtMs = logger.now();

        job.state = Job.State.COMPLETED;

        logger.jobCompleted(job);

        statistics.recordCompletion(job);
    }
}