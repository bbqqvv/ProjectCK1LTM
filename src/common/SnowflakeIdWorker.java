package common;

public class SnowflakeIdWorker {
    private final long twepoch = 1288834974657L; // Mốc thời gian (epoch) của Twitter
    private final long workerIdBits = 5L; // Số bit dành cho worker ID
    private final long datacenterIdBits = 5L; // Số bit dành cho datacenter ID
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits); // Mã tối đa cho workerId
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // Mã tối đa cho datacenterId
    private final long sequenceBits = 12L; // Số bit dành cho sequence (dãy số)
    
    private final long workerIdShift = sequenceBits; // Di chuyển workerId vào đúng vị trí
    private final long datacenterIdShift = sequenceBits + workerIdBits; // Di chuyển datacenterId
    private final long timestampShift = datacenterIdShift + datacenterIdBits; // Di chuyển timestamp vào đúng vị trí
    
    private final long sequenceMask = -1L ^ (-1L << sequenceBits); // Mặt nạ để lấy sequence
    private long workerId; // Worker ID
    private long datacenterId; // Datacenter ID
    private long sequence = 0L; // Sequence number

    private long lastTimestamp = -1L; // Timestamp của lần cuối cùng tạo ID

    // Khởi tạo SnowflakeIdWorker với workerId và datacenterId
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("workerId can't be greater than " + maxWorkerId + " or less than 0");
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + maxDatacenterId + " or less than 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // Tạo ID duy nhất
    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        // Nếu thời gian hiện tại nhỏ hơn thời gian của lần trước, thì đợi
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards, refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
        }

        // Nếu cùng một timestamp, tăng sequence number
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask; // Lấy sequence theo mặt nạ
            if (sequence == 0) {
                // Nếu hết sequence, đợi cho đến thời gian tiếp theo
                timestamp = waitForNextTimestamp(lastTimestamp);
            }
        } else {
            sequence = 0; // Reset sequence nếu là timestamp mới
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long waitForNextTimestamp(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
