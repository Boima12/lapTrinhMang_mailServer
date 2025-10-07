package client.ui;

import client.model.MailListItem;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tiện ích dùng chung cho ClientUI (SRP: gom các hàm thao tác dữ liệu/hiển thị).
 */
public final class ClientUIHelper {
    private ClientUIHelper() {}

    public static String toLocalName(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    public static void writeSentLocal(Path root, String filename, java.util.List<String> lines) throws java.io.IOException {
        Files.createDirectories(root.resolve("sent"));
        Files.write(root.resolve("sent").resolve(filename), lines, StandardCharsets.UTF_8);
    }

    public static void showMailContent(String currentUser, String folder, MailListItem item,
                                       javax.swing.JLabel header_title,
                                       javax.swing.JLabel header_from,
                                       javax.swing.JLabel header_timestamp,
                                       javax.swing.JTextArea bodyTa) {
        try {
            Path root = Path.of("src", "client", "localStorage", toLocalName(currentUser), folder);
            if (!Files.exists(root)) {
                bodyTa.setText("");
                header_title.setText(item.getTitle());
                header_from.setText(item.getFrom());
                header_timestamp.setText(item.getTimestamp());
                return;
            }
            for (Path p : Files.list(root).toList()) {
                if (!Files.isRegularFile(p)) continue;
                java.util.List<String> lines = Files.readAllLines(p);
                String ts = extractField(lines, "TIMESTAMP=");
                String from = extractField(lines, "FROM=");
                String to = extractField(lines, "TO=");
                String title = extractField(lines, "TITLE=");
                if (item.getTitle().equals(title) && item.getFrom().equals(from) && item.getTimestamp().equals(ts)) {
                    String content = extractField(lines, "CONTENT=");
                    header_title.setText(title);
                    header_from.setText(from + (folder.equals("sent") ? " -> " + to : ""));
                    header_timestamp.setText(ts);
                    bodyTa.setText(content);
                    bodyTa.setCaretPosition(0);
                    return;
                }
            }
            header_title.setText(item.getTitle());
            header_from.setText(item.getFrom());
            header_timestamp.setText(item.getTimestamp());
            bodyTa.setText("");
        } catch (Exception ex) {
            header_title.setText(item.getTitle());
            header_from.setText(item.getFrom());
            header_timestamp.setText(item.getTimestamp());
            bodyTa.setText("");
        }
    }

    public static String extractField(java.util.List<String> lines, String prefix) {
        for (String s : lines) if (s.startsWith(prefix)) return s.substring(prefix.length());
        return "";
    }
}


