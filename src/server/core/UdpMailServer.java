package server.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Mail server UDP (bước 1): hỗ trợ REGISTER và SEND với ACK đơn giản.
 * - Gói văn bản UTF-8: "CMD payload..."
 * - Trả lời: "OK ..." hoặc "ERR ..."
 * - Các lệnh:
 *   REGISTER <username> <password>
 *   LOGIN <username> <password>
 *   LIST <username> sent|inbox
 *   GETFILE <username> sent|inbox <filename>
 *   SEND <from> <to> <title> | <content>   (bước 1 đơn gói; có thể nâng cấp chunk)
 */
public class UdpMailServer implements Runnable {

    private final int port;
    private final StorageService storageService;
    private volatile boolean running = true;

    public UdpMailServer(int port, Path databaseRoot) throws IOException {
        this.port = port;
        this.storageService = new StorageService(databaseRoot);
        this.storageService.ensureDatabaseRoot();
    }

    public void shutdown() { running = false; }

    @Override
    public void run() {
        byte[] buf = new byte[2048];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();
                InetAddress addr = packet.getAddress();
                int clientPort = packet.getPort();
                String resp = handle(msg);
                byte[] out = resp.getBytes(StandardCharsets.UTF_8);
                DatagramPacket reply = new DatagramPacket(out, out.length, addr, clientPort);
                socket.send(reply);
            }
        } catch (IOException ignored) {}
    }

    private String handle(String msg) {
        try {
            if (msg.startsWith("REGISTER ")) {
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) return "ERR REGISTER syntax";
                String username = ensureEmail(parts[1]);
                String password = parts[2];
                if (storageService.accountExists(username)) return "ERR Account exists";
                storageService.createAccount(username, password);
                return "OK";
            }
            if (msg.startsWith("LOGIN ")) {
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) return "ERR LOGIN syntax";
                String username = ensureEmail(parts[1]);
                String password = parts[2];
                if (!storageService.accountExists(username)) return "ERR No account";
                java.nio.file.Path infoPath = storageService.getDatabaseRoot().resolve(username).resolve("accountInfo.txt");
                java.util.List<String> lines = storageService.readAllLines(infoPath);
                boolean passMatch = lines.stream().anyMatch(s -> s.equals("password=" + password));
                return passMatch ? "OK" : "ERR Wrong password";
            }
            if (msg.startsWith("LIST ")) {
                String[] parts = msg.split(" ", 3);
                if (parts.length < 3) return "ERR LIST syntax";
                String username = ensureEmail(parts[1]);
                String box = parts[2];
                java.util.List<java.nio.file.Path> files = storageService.listMailFiles(username, box);
                StringBuilder sb = new StringBuilder("OK ").append(files.size());
                for (var p : files) sb.append('\n').append(p.getFileName());
                return sb.toString();
            }
            if (msg.startsWith("GETFILE ")) {
                String[] parts = msg.split(" ", 4);
                if (parts.length < 4) return "ERR GETFILE syntax";
                String username = ensureEmail(parts[1]);
                String box = parts[2];
                String filename = parts[3];
                java.nio.file.Path p = storageService.getDatabaseRoot().resolve(username).resolve(box).resolve(filename);
                java.util.List<String> content = java.nio.file.Files.readAllLines(p);
                StringBuilder sb = new StringBuilder();
                sb.append("FILE_BEGIN ").append(filename).append(' ').append(content.size());
                for (String s : content) sb.append('\n').append(s);
                sb.append('\n').append("FILE_END");
                return sb.toString();
            }
            if (msg.startsWith("SEND ")) {
                // SEND <from> <to> <title> | <content>
                int bar = msg.indexOf('|');
                if (bar < 0) return "ERR SEND syntax";
                String head = msg.substring(5, bar).trim();
                String content = msg.substring(bar + 1).trim();
                String[] h = head.split(" ", 3);
                if (h.length < 3) return "ERR SEND syntax";
                String from = ensureEmail(h[0]);
                String to = ensureEmail(h[1]);
                String title = h[2];

                long ts = System.currentTimeMillis();
                String tsStr = storageService.formatTimestamp(ts);
                String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
                String filename = ts + "_" + safeTitle + ".txt";
                java.util.List<String> mail = new java.util.ArrayList<>();
                mail.add("TIMESTAMP=" + tsStr);
                mail.add("FROM=" + from);
                mail.add("TO=" + to);
                mail.add("TITLE=" + title);
                mail.add("CONTENT=" + content);
                storageService.saveMailToUserFolder(from, "sent", filename, mail);
                storageService.saveMailToUserFolder(to, "inbox", filename, mail);
                // phát Overview realtime
                try {
                    java.nio.file.Path senderPath = storageService.getDatabaseRoot().resolve(from).resolve("sent").resolve(filename);
                    long size = java.nio.file.Files.size(senderPath);
                    OverviewBus.emit(tsStr, from, to, title, size);
                } catch (Exception ignored) {}
                return "OK";
            }
            return "ERR Unknown";
        } catch (Exception e) {
            return "ERR " + e.getMessage();
        }
    }

    private String ensureEmail(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return s;
        return s.contains("@") ? s : s + "@gmail.com";
    }
}


