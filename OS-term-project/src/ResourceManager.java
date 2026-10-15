/**
 * ควบคุมสิทธิ์การใช้ทรัพยากรร่วมของทั้งระบบ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้:
 *   - หัวข้อ 5: ใช้ Semaphore ควบคุม PRINTER และ DATABASE
 *     จำนวน permit มาจาก command line (Config)
 *     ในส่วนบังคับให้สร้าง Semaphore แบบ fair = true
 *   - Worker ทุกตัวต้องใช้ ResourceManager object เดียวกัน
 *   - หัวข้อ 7: permit ต้องไม่สูญหายหรือค้าง แม้เกิด exception
 *     หรือถูก interrupt ระหว่างถือ resource
 *
 * คำถามที่จะถูกถามใน Demo:
 *   - ทำไมต้อง fair = true และถ้าเปลี่ยนเป็น false จะเกิดอะไรขึ้น
 *   - ถ้า Thread ถูก interrupt หลัง acquire สำเร็จแต่ก่อน release
 *     โค้ดของกลุ่มยังคืน permit ได้หรือไม่
 */
import java.util.concurrent.Semaphore;

/**
 * ควบคุม PRINTER และ DATABASE ด้วย fair Semaphore
 */
public class ResourceManager {

    private final Semaphore printer;
    private final Semaphore database;

    private final int printerPermits;
    private final int databasePermits;

    public ResourceManager(int printerPermits, int databasePermits) {

        if (printerPermits < 1 || databasePermits < 1) {
            throw new IllegalArgumentException(
                    "จำนวน permit ต้องไม่น้อยกว่า 1");
        }

        this.printerPermits = printerPermits;
        this.databasePermits = databasePermits;

        /*
         * fair = true ตามข้อกำหนดของโจทย์
         */
        this.printer = new Semaphore(printerPermits, true);
        this.database = new Semaphore(databasePermits, true);
    }

    /**
     * ขอ resource
     */
    public void acquire(ResourceType type)
            throws InterruptedException {

        switch (type) {

            case PRINTER:
                printer.acquire();
                break;

            case DATABASE:
                database.acquire();
                break;

            case NONE:
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown resource type: " + type);
        }
    }

    /**
     * คืน resource
     */
    public void release(ResourceType type) {

        switch (type) {

            case PRINTER:
                printer.release();
                break;

            case DATABASE:
                database.release();
                break;

            case NONE:
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown resource type: " + type);
        }
    }

    /**
     * แสดงสถานะ resource
     *
     * ตัวอย่าง:
     * printer=1/1 database=0/2
     */
    public String status() {

        int printerUsed =
                printerPermits - printer.availablePermits();

        int databaseUsed =
                databasePermits - database.availablePermits();

        return String.format(
                "printer=%d/%d database=%d/%d",
                printerUsed,
                printerPermits,
                databaseUsed,
                databasePermits
        );
    }
}