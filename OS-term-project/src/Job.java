/**
 * ข้อมูลของงานหนึ่งชิ้น
 *
 * ฟิลด์ทั้งหมดในไฟล์นี้มาจากไฟล์ workload CSV โดยตรง และถูกกำหนดครั้งเดียว
 * ตอนโหลด จึงประกาศเป็น final และปลอดภัยเมื่อหลาย Thread อ่านพร้อมกัน
 *
 * ไฟล์นี้เป็นโค้ดตั้งต้นที่อาจารย์แจก แต่ต่างจากไฟล์อื่นตรงที่
 * นักศึกษา "ต้องแก้" โดยเพิ่มฟิลด์ของตัวเองในส่วน TODO ด้านล่าง
 */
/**
 * ข้อมูลของงานหนึ่งชิ้น
 */
public class Job {

    public final String id;
    public final long arrivalMs;
    public final int priority;
    public final long workMs;
    public final ResourceType resource;
    public final long resourceMs;
    public final int sequence;

    /**
     * สถานะของ Job ใน simulation
     */
    public enum State {
        ARRIVED,
        READY,
        RUNNING,
        WAITING_RESOURCE,
        COMPLETED
    }

    /*
     * ฟิลด์สำหรับเก็บเวลาจริงที่เกิดขึ้นระหว่าง simulation
     *
     * ใช้ volatile เพราะมีหลาย Thread เกี่ยวข้อง
     */
    public volatile long enteredAtMs = -1L;
    public volatile long startedAtMs = -1L;
    public volatile long completedAtMs = -1L;

    public volatile long resourceWaitStartMs = -1L;
    public volatile long resourceWaitMs = 0L;

    public volatile State state = null;

    public Job(String id, long arrivalMs, int priority, long workMs,
               ResourceType resource, long resourceMs, int sequence) {
        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource;
        this.resourceMs = resourceMs;
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return String.format("%s(priority=%d, work=%dms, %s)",
                id, priority, workMs,
                resource == ResourceType.NONE
                        ? "no resource"
                        : resource + " " + resourceMs + "ms");
    }
}