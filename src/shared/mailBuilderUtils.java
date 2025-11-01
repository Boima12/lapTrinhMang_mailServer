package shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

public class mailBuilderUtils {

    /**
     * Build the base mail directory structure and user metadata file.
     * @param userDir  Root directory for the user (e.g. src/server/database/username)
     * @param clientIP IP address of client registering
     * @param user     Username (email-like string)
     * @param pass     Plaintext password (will be hashed with SHA-256)
     * @return The created userDir File
     * @throws IOException if writing or directory creation fails
     */
    public File buildBaseStructure(File userDir, String serverIP, String clientIP, String user, String pass) throws IOException {

        // --- Create folder structure ---
        File inboxDir = new File(userDir, "inbox");
        File sentDir = new File(userDir, "sent");
        File infoFile = new File(userDir, "accountInfo.txt");

        if (!inboxDir.mkdirs() || !sentDir.mkdirs()) {
            throw new IOException("Failed to create mailbox structure for " + user);
        }

        // --- Prepare metadata ---
        String dateCreated = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date());
        String lastLogin = dateCreated;
        String lastLoginIP = clientIP;

        String hashedPass = hashSHA256(pass);

        // --- Write account info ---
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile))) {
            writer.write("USERNAME=" + user);
            writer.newLine();
            writer.write("PASSWORD=" + hashedPass);
            writer.newLine();
            writer.write("DATE_CREATED=" + dateCreated);
            writer.newLine();
            writer.write("LAST_LOGIN=" + lastLogin);
            writer.newLine();
            writer.write("LAST_LOGIN_IP=" + lastLoginIP);
        }
        
        // create a welcome.txt mail inside src/server/database/<user>/inbox/
        createEmail(inboxDir,
    		"Welcome to mailServer!",
        	"The MailServer Team",
        	clientIP,
        	user,
        	"Dear User,\r\n"
        	+ "\r\n"
        	+ "Thank you for joining our email service. We’re excited to have you onboard!\r\n"
        	+ "\r\n"
        	+ "We hope you’ll find SimpleMail easy, secure, and reliable for all your communication needs.  \r\n"
        	+ "If you encounter any issues or have suggestions, feel free to reach out — we’re always happy to help."
		);

        return userDir;
    }
    
    // this method is a util method that help make mail .txt file
    public File createEmail(File userDir, String title, String from, String from_ip, String to, String content) throws IOException {    	
        File aMail = new File(userDir, FileNameEncryptor.encryptFileName(title) + ".txt");

        String timestamp = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new java.util.Date()); 

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aMail))) {
            writer.write("TITLE=" + title);
            writer.newLine();
            writer.write("FROM=" + from);
            writer.newLine();
            writer.write("FROM_IP=" + from_ip);
            writer.newLine();
            writer.write("TO=" + to);
            writer.newLine();
            writer.write("TIMESTAMP=" + timestamp);
            writer.newLine();
            writer.write("CONTENT=");
            writer.newLine();
            writer.write(content);
        }

        return aMail;
    }

    public static String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
