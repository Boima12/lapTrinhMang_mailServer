package server.model;

public class RecordListItem {
    private String timestamp;
    private String message;

    public RecordListItem(String timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        // fallback string (not used by renderer)
        return "[" + timestamp + "] " + message;
    }
}
