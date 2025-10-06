package server.model;

public class RecordListItem {
    private String timestamp;
    private String from;
    private String to;
    private String title;

    public RecordListItem(String timestamp, String from, String to, String title) {
        this.timestamp = timestamp;
        this.from = from;
        this.to = to;
        this.title = title;
    }

    public String getTimestamp() { return timestamp; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        // fallback string (not used by renderer)
        return "[" + timestamp + "] " + from + " -> " + to + " | " + title;
    }
}
