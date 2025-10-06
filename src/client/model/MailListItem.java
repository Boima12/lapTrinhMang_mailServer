package client.model;

public class MailListItem {
    private String title;
    private String from;
    private String timestamp;

    public MailListItem(String title, String from, String timestamp) {
        this.title = title;
        this.from = from;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getFrom() { return from; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return title + " - " + from + " (" + timestamp + ")";
    }
}
