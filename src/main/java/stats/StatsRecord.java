package stats;

class StatsRecord {

    private final long timestamp;
    private final long duration;

    StatsRecord(long timestamp, long duration) {
        this.timestamp = timestamp;
        this.duration = duration;
    }

    long getTimestamp() {
        return timestamp;
    }

    long getDuration() {
        return duration;
    }
}
