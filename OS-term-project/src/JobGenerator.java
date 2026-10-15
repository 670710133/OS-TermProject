import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ปล่อย Job เข้าระบบตาม arrivalMs
 */
public class JobGenerator extends Thread {

    private final List<Job> jobs;
    private final BlockingQueue<Job> schedulerInput;
    private final ProjectLogger logger;

    public JobGenerator(
            List<Job> jobs,
            BlockingQueue<Job> schedulerInput,
            ProjectLogger logger) {

        super("generator");

        this.jobs = new ArrayList<>(jobs);
        this.schedulerInput = schedulerInput;
        this.logger = logger;
    }

    @Override
    public void run() {

        /*
         * WorkloadLoader คืน Job ตามลำดับในไฟล์
         * ไม่ได้การันตีว่า arrivalMs เรียงกัน
         *
         * ดังนั้นต้องเรียงสำเนาก่อน
         */
        jobs.sort(
                Comparator
                        .comparingLong((Job j) -> j.arrivalMs)
                        .thenComparingInt(j -> j.sequence)
        );

        try {

            for (Job job : jobs) {

                /*
                 * รอจนถึง arrivalMs
                 */
                waitUntil(job.arrivalMs);

                /*
                 * บันทึกเวลาที่เข้าสู่ระบบจริง
                 */
                job.enteredAtMs = logger.now();

                job.state = Job.State.ARRIVED;

                logger.jobArrived(job);

                /*
                 * ส่งไป Scheduler
                 */
                schedulerInput.put(job);
            }

            /*
             * บอก Scheduler ว่า
             * ไม่มี Job ใหม่อีกแล้ว
             */
            schedulerInput.put(
                    Scheduler.POISON_PILL
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }

    /**
     * รอจนถึงเวลาที่ต้องปล่อย Job
     */
    private void waitUntil(long targetMs)
            throws InterruptedException {

        while (true) {

            long remaining =
                    targetMs - logger.now();

            if (remaining <= 0) {
                return;
            }

            Thread.sleep(remaining);
        }
    }
}