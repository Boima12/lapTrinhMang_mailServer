package server.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.zip.ZipOutputStream;

import shared.ZipUtils;
import shared.mailBuilderUtils;

public class SmtpClientHandler implements Runnable {
	private final String serverIP;
    private final Socket socket;
    private final Consumer<String> log;
    private Consumer<String> uiEventCallback;
    
    private final mailBuilderUtils mailUtils = new mailBuilderUtils();

    public SmtpClientHandler(String serverIP, Socket socket, Consumer<String> logCallback, Consumer<String> uiEventCallback) {
        this.serverIP = serverIP;
        this.socket = socket;
        this.log = logCallback;
        this.uiEventCallback = uiEventCallback;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            send(out, "220 Simple SMTP Ready");

            String line;
            while ((line = in.readLine()) != null) {
        		log("Received: " + line);            		

                if (line.startsWith("REGISTER")) {
                    handleRegister(line, out);
                } else if (line.startsWith("LOGIN")) {
                    handleLogin(line, out);
                } else if (line.startsWith("GET_FOLDER_DATA")) {
                	responseFolderData(line, out);
                } else if (line.startsWith("SEND_EMAIL")) {
                	handleEmail(line, out);
                } else if (line.equalsIgnoreCase("QUIT")) {
                    send(out, "221 Bye");
                    break;
                } else {
                	System.out.println(line);
                    send(out, "502 Command not implemented");
                }
            }
        } catch (IOException e) {
            log("Client handler error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            log("Client disconnected: " + socket.getInetAddress());
        }
    }

    private void handleRegister(String line, BufferedWriter out) throws IOException {
        String[] parts = line.split(" ");
        if (parts.length == 3) {
            String user = parts[1];
            String pass = parts[2];

            File userDir = new File("src/server/database/" + user);

            if (userDir.exists()) {
                send(out, "550 User already exists");
                return;
            }

            mailUtils.buildBaseStructure(userDir, serverIP ,socket.getInetAddress().getHostAddress(), user, pass);

            log("Registered user: " + user + " / [hashed password]");
            send(out, "250 OK Account created.");
            
            notifyUI("new account " + user + " registered.");

        } else {
            send(out, "500 Syntax error");
        }
    }

    private void handleLogin(String line, BufferedWriter out) throws IOException {    	
        String[] parts = line.split(" ");
        if (parts.length == 3) {
            String user = parts[1];
            String pass = parts[2];
            
            // --- 1. Locate the user directory ---
            File userDir = new File("src/server/database/" + user);
            
            // --- 2. Validate existence ---
            if (!userDir.exists() || !userDir.isDirectory()) {
                send(out, "550 User folder not found");
                log("User folder not found for: " + user);
                return;
            }

            // --- 3. Locate and read accountInfo.txt ---
            File infoFile = new File(userDir, "accountInfo.txt");
            if (!infoFile.exists()) {
                send(out, "451 accountInfo.txt missing for user: " + user);
                log("Missing accountInfo.txt for user: " + user);
                return;
            }

            String storedPassword = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
                String lineData;
                while ((lineData = reader.readLine()) != null) {
                    if (lineData.startsWith("PASSWORD=")) {
                        storedPassword = lineData.substring("PASSWORD=".length()).trim();
                        break;
                    }
                }
            }

            // --- 4. Validate password ---
            if (storedPassword == null) {
                send(out, "451 Password entry missing in accountInfo.txt");
                log("Password entry missing for user: " + user);
                return;
            }

            String hashedInput = mailBuilderUtils.hashSHA256(pass);
            if (!storedPassword.equals(hashedInput)) {
                send(out, "535 Authentication failed");
                log("Failed login attempt for user: " + user);
                return;
            }
            
            // --- 5. Update login metadata ---
            String newLastLogin = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date());
            String newLastLoginIP = socket.getInetAddress().getHostAddress();

            // Read all lines so we can rewrite
            java.util.List<String> lines = new java.util.ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
                String lineData;
                while ((lineData = reader.readLine()) != null) {
                    if (lineData.startsWith("LAST_LOGIN=")) {
                        lineData = "LAST_LOGIN=" + newLastLogin;
                    } else if (lineData.startsWith("LAST_LOGIN_IP=")) {
                        lineData = "LAST_LOGIN_IP=" + newLastLoginIP;
                    }
                    lines.add(lineData);
                }
            }

            // Overwrite updated info
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
            }

            // --- 6. Login success ---
            send(out, "235 User login succeed for: " + user);  
            log("User login successful: " + user);
            
            notifyUI(user + " login successful.");
            
        } else {
            send(out, "500 Syntax error");
        } 
    }
    
    private void responseFolderData(String line, BufferedWriter out) throws IOException {    	
    	String[] parts = line.split(" ");
    	if (parts.length == 2) {
    		String user = parts[1];
    		
    		// --- 1. Locate the user directory ---
            File userDir = new File("src/server/database/" + user);
    		
            // --- 2. Validate existence ---
            if (!userDir.exists() || !userDir.isDirectory()) {
                send(out, "550 User folder not found");
                log("User folder not found for: " + user);
                return;
            }
    		
            // --- 3. Notify client that data transfer will begin ---
            send(out, "150 Opening ZIP data transfer for user: " + user);
    		
    		// --- 4. Send user folder as ZIP ---
    		sendDirectoryAsZip(userDir, socket.getOutputStream());
    		
    		log("Sent folder structure to client for user: " + user);    		
    	} else {
            send(out, "500 Syntax error");
        } 
    }

    private void handleEmail(String line, BufferedWriter out) throws IOException {
        String[] parts = line.split(" ");
        if (parts.length != 2) {
            send(out, "500 Syntax error");
            return;
        }

        String sender = parts[1];
        File senderDir = new File("src/server/database/" + sender);

        if (!senderDir.exists()) {
            send(out, "550 Sender account not found");
            return;
        }

     	// --- 1. Prepare temp folder ---
        File tempDir = new File("src/server/temp_" + sender + "_" + System.currentTimeMillis());
        if (!tempDir.exists()) tempDir.mkdirs();

        // --- 2. Receive the mail file sent by client as ZIP ---
        send(out, "354 Start mail input (send ZIP now)");
        out.flush(); // critical before switching to binary stream

        try {
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            int zipLength = dataIn.readInt();  // read prefix
            if (zipLength <= 0) {
                send(out, "451 Invalid ZIP length");
                out.flush();
                return;
            }

            System.out.println("[Server] Expecting " + zipLength + " bytes of ZIP data");

            byte[] zipBytes = new byte[zipLength];
            dataIn.readFully(zipBytes);

            File tempZip = new File(tempDir, "mail.zip");
            try (FileOutputStream fos = new FileOutputStream(tempZip)) {
                fos.write(zipBytes);
            }

            try (FileInputStream fis = new FileInputStream(tempZip)) {
                ZipUtils.unZipFile(fis, tempDir);
            }

            tempZip.delete();
            System.out.println("[Server] Received and extracted ZIP (" + zipLength + " bytes)");
        } catch (EOFException eof) {
            System.err.println("Client disconnected mid-transfer");
            send(out, "451 Transfer aborted");
            out.flush();
            cleanup(tempDir);
            return;
        } catch (Exception e) {
            send(out, "451 Failed to receive mail data");
            out.flush();
            e.printStackTrace();
            cleanup(tempDir);
            return;
        }

        // --- 3. Find .txt mail file ---
        File[] receivedFiles = tempDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (receivedFiles == null || receivedFiles.length == 0) {
            send(out, "550 No mail file received");
            cleanup(tempDir);
            return;
        }

        File receivedMail = receivedFiles[0];

        // --- 4. Read the recipient field from the mail ---
        String recipientName = null;
        String title = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(receivedMail))) {
            String lineData;
            while ((lineData = reader.readLine()) != null) {
            	if (lineData.startsWith("TITLE=")) {
            		title = lineData.substring("TITLE=".length());
            	} else if (lineData.startsWith("TO=")) {
                    recipientName = lineData.substring("TO=".length())
                            .replace("@mailServer.com", "")
                            .trim();
                    break;
                }
            }
        }

        if (recipientName == null || recipientName.isEmpty()) {
            send(out, "550 Invalid recipient in mail file");
            cleanup(tempDir);
            return;
        }

        // --- 5. Verify recipient existence ---
        if (!isAccountExists(recipientName)) {
            send(out, "550 Recipient not found on server");
            cleanup(tempDir);
            out.flush();
            return;
        }

        // --- 6. Move mail to sender's sent/ ---
        File senderSentDir = new File(senderDir, "sent");
        if (!senderSentDir.exists()) senderSentDir.mkdirs();
		File sentMailFile = new File(senderSentDir, receivedMail.getName());
		boolean moved = receivedMail.renameTo(sentMailFile);
		
		if (!moved) {
		    // Fallback: manual copy if rename fails
		    try (InputStream inMail = new FileInputStream(receivedMail);
		         OutputStream outMail = new FileOutputStream(sentMailFile)) {
		        inMail.transferTo(outMail);
		    }
		    // Clean up temp file
		    if (!receivedMail.delete()) {
		        System.err.println("Warning: failed to delete temp mail file: " + receivedMail.getAbsolutePath());
		    }
		}
		
        // --- 7. Copy mail to recipient's inbox/ ---
        File recipientInboxDir = new File("src/server/database/" + recipientName + "/inbox");
        if (!recipientInboxDir.exists()) recipientInboxDir.mkdirs();

        File recipientMailFile = new File(recipientInboxDir, receivedMail.getName());
        try (InputStream inMail = new FileInputStream(sentMailFile);
             OutputStream outMail = new FileOutputStream(recipientMailFile)) {
            inMail.transferTo(outMail);
        }

        // --- 8. Clean up temporary folder ---
        cleanup(tempDir);

        // --- 9. Notify success ---
        send(out, "250 OK Mail delivered to " + recipientName + "@mailServer.com");
        log("Delivered mail from " + sender + " to " + recipientName);
        
        notifyUI(sender + "@mailServer.com -> " + recipientName + "@mailServer.com | " + title);
    }
    
    private void send(BufferedWriter out, String msg) throws IOException {
        out.write(msg + "\r\n");
        out.flush();
    }

    private void log(String msg) {
        if (log != null) log.accept(msg);
        else System.out.println(msg);
    }

    /**
     * Helper to send the folder as a ZIP archive over the socket.
     */
    private void sendDirectoryAsZip(File userDir, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            ZipUtils.zipFile(userDir, userDir.getName(), zipOut);
            zipOut.finish();
        }
    }
    
    private boolean isAccountExists(String accountName) {
        File userDir = new File("src/server/database/" + accountName);
        return userDir.exists() && userDir.isDirectory();
    }
    
    private void cleanup(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) cleanup(f);
                else f.delete();
            }
        }
        dir.delete();
    }
    
    private void notifyUI(String text) {
        if (uiEventCallback != null) {
            uiEventCallback.accept(text);
        }
    }

}
