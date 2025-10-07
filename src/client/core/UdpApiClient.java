package client.core;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * UDP client đơn giản với retry/timeout cho gói nhỏ.
 * Bước 1: hỗ trợ REGISTER, SEND (1 gói) để kiểm tra đường truyền.
 */
public class UdpApiClient {
    private final String host;
    private final int port;
    private final int timeoutMs;
    private final int maxRetry;

    public UdpApiClient(String host, int port) {
        this(host, port, 2000, 3);
    }

    public UdpApiClient(String host, int port, int timeoutMs, int maxRetry) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.maxRetry = maxRetry;
    }

    public boolean register(String username, String password) throws IOException {
        String cmd = "REGISTER " + username + " " + password;
        String res = sendAndWait(cmd);
        return res != null && res.startsWith("OK");
    }

    public boolean send(String from, String to, String title, String content) throws IOException {
        // Ghép nội dung vào một gói (tạm thời, sẽ chuyển sang chunk nếu dài)
        String cmd = "SEND " + from + " " + to + " " + title + " | " + content;
        String res = sendAndWait(cmd);
        return res != null && res.startsWith("OK");
    }

    public boolean login(String username, String password) throws IOException {
        String res = sendAndWait("LOGIN " + username + " " + password);
        return res != null && res.startsWith("OK");
    }

    public String[] list(String username, String box) throws IOException {
        String res = sendAndWait("LIST " + username + " " + box);
        if (res == null || !res.startsWith("OK ")) return new String[0];
        String[] lines = res.split("\n");
        int n = Integer.parseInt(lines[0].substring(3).trim());
        String[] names = new String[Math.max(0, n)];
        for (int i = 0; i < n && i + 1 < lines.length; i++) names[i] = lines[i + 1];
        return names;
    }

    public java.util.List<String> getFile(String username, String box, String filename) throws IOException {
        String res = sendAndWait("GETFILE " + username + " " + box + " " + filename);
        if (res == null || !res.startsWith("FILE_BEGIN ")) return java.util.List.of();
        String[] lines = res.split("\n");
        // FILE_BEGIN filename N
        int space = lines[0].lastIndexOf(' ');
        int n = Integer.parseInt(lines[0].substring(space + 1));
        java.util.List<String> out = new java.util.ArrayList<>();
        for (int i = 1; i <= n && i < lines.length; i++) out.add(lines[i]);
        return out;
    }

    private String sendAndWait(String payload) throws IOException {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        InetAddress addr = InetAddress.getByName(host);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeoutMs);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
            byte[] buf = new byte[2048];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);
            for (int i = 0; i < maxRetry; i++) {
                socket.send(packet);
                try {
                    socket.receive(resp);
                    return new String(resp.getData(), resp.getOffset(), resp.getLength(), StandardCharsets.UTF_8);
                } catch (SocketTimeoutException ignored) {
                    // retry
                }
            }
            return null;
        }
    }
}


