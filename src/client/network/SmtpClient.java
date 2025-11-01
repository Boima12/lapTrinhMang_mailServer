package client.network;

import java.io.*;
import java.net.Socket;
import java.util.zip.ZipOutputStream;

import shared.ZipUtils;

public class SmtpClient implements Closeable {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public SmtpClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        socket.setSoTimeout(10000); // 10 seconds
        
        String greeting = in.readLine();
        if (greeting == null || !greeting.startsWith("220")) {
            throw new IOException("Invalid SMTP greeting from server: " + greeting);
        }
    }

    private String readResponse() throws IOException {
        String resp = in.readLine();
        System.out.println("[Server]: " + resp);
        return resp;
    }

    private void sendCommand(String cmd) throws IOException {
        System.out.println("[Client → Server]: " + cmd);
        out.write(cmd + "\r\n");
        out.flush();
    }

    public boolean register(String username, String password) throws IOException {
        sendCommand("REGISTER " + username + " " + password);
        String resp = readResponse();

        if (resp == null) return false;
        if (resp.startsWith("550")) {
            System.out.println("Account already exists.");
            return false;
        }
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        sendCommand("LOGIN " + username + " " + password);
        String resp = readResponse();
        return resp != null && resp.startsWith("235");
    }

    public void quit() throws IOException {
        sendCommand("QUIT");
        readResponse(); // expect "221 Bye"
        System.out.println("");
    }
    
    public void requestFolderData(String accountName) throws IOException {
    	sendCommand("GET_FOLDER_DATA " + accountName);
    	String resp = readResponse();
    	
        if (resp.startsWith("150")) {
            System.out.println("Receiving folder data from server...");

            File localDir = new File("src/client/localStorage/");
            if (!localDir.exists()) localDir.mkdirs();

            // use ZipUtils to unzip received data
            ZipUtils.unZipFile(socket.getInputStream(), localDir);

            System.out.println("Folder extracted to: " + localDir.getAbsolutePath());
        }
    }
    
    public void sendEmail(File newMail, String accountName) throws IOException {
        if (newMail == null || !newMail.exists() || !newMail.isFile()) {
            throw new IOException("Invalid mail file: " + (newMail != null ? newMail.getAbsolutePath() : "null"));
        }

        // --- 1. Tell the server you're sending mail ---
        sendCommand("SEND_EMAIL " + accountName);

        // --- 2. Wait for server readiness ---
        String resp = readResponse();
        if (resp == null) {
            System.err.println("No response from server after SEND_EMAIL.");
            return;
        } else if (!resp.startsWith("354")) { 
            System.err.println("Server not ready to receive mail: " + resp);
            return;
        }

        // --- 3. Send the mail file as ZIP ---
        System.out.println("Zipping and sending mail: " + newMail.getName());

        // Step 1: Compress the mail into a byte array first
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteBuffer)) {
            ZipUtils.zipFile(newMail, newMail.getName(), zipOut);
        }
        byte[] zipBytes = byteBuffer.toByteArray();

        // Step 2: Send length prefix, then ZIP data
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
        dataOut.writeInt(zipBytes.length);     // 4-byte header
        dataOut.write(zipBytes);
        dataOut.flush();

        System.out.println("[Client] Sent " + zipBytes.length + " bytes of mail ZIP data");


        // --- 4. Wait for server confirmation ---
        String finalResp;
        try {
            finalResp = readResponse(); // expect either 250 or 550 etc.
        } catch (IOException e) {
            System.err.println("Error waiting for server response after sending mail: " + e.getMessage());
            return;
        }

        if (finalResp == null) {
            System.err.println("No final response from server after sending mail.");
            return;
        }

        System.out.println("[Server]: " + finalResp);

        if (finalResp.startsWith("250")) {
            System.out.println("Mail successfully delivered: " + finalResp);
        } else if (finalResp.startsWith("550")) {
            System.err.println("Mail rejected by server: " + finalResp);
            // --- Optional cleanup if rejected ---
            if (newMail.exists()) {
                boolean deleted = newMail.delete();
                if (!deleted) {
                    System.err.println("Failed to delete local mail file: " + newMail.getAbsolutePath());
                }
            }
        } else {
            System.err.println("Unexpected server response: " + finalResp);
        }
    }

    @Override
    public void close() throws IOException {
        if (!socket.isClosed()) {
            try {
                quit();
            } catch (IOException ignored) {}
            socket.close();
        }
    }
}
