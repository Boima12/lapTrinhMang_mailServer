package server.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Xử lý thao tác hệ thống file cho tài khoản và email.
 * SRP: Chỉ đọc/ghi file và tổ chức thư mục, không xử lý giao thức.
 */
public class StorageService {

    private final Path databaseRoot;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public StorageService(Path databaseRoot) {
        this.databaseRoot = databaseRoot;
    }

    public void ensureDatabaseRoot() throws IOException {
        Files.createDirectories(databaseRoot);
    }

    public boolean accountExists(String username) {
        Path userDir = databaseRoot.resolve(username);
        return Files.exists(userDir);
    }

    public void createAccount(String username, String password) throws IOException {
        Path userDir = databaseRoot.resolve(username);
        Path inboxDir = userDir.resolve("inbox");
        Path sentDir = userDir.resolve("sent");
        Files.createDirectories(inboxDir);
        Files.createDirectories(sentDir);

        // accountInfo.txt lưu thông tin đăng nhập (demo, không mã hoá)
        Path accountInfo = userDir.resolve("accountInfo.txt");
        List<String> lines = new ArrayList<>();
        lines.add("username=" + username);
        lines.add("password=" + password);
        Files.write(accountInfo, lines, StandardCharsets.UTF_8);

        // No personal mail file at database root; keep database clean per user folder only
    }

    // Removed file-based overview logging per requirement; use OverviewBus instead.

    public String formatTimestamp(long ts) {
        return timestampFormat.format(new Date(ts));
    }

    public void saveMailToUserFolder(String username, String folder, String filename, List<String> contentLines) throws IOException {
        Path folderPath = databaseRoot.resolve(username).resolve(folder);
        Files.createDirectories(folderPath);
        Path filePath = folderPath.resolve(filename);
        Files.write(filePath, contentLines, StandardCharsets.UTF_8);
    }

    public List<String> readAllLines(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public List<Path> listMailFiles(String username, String folder) throws IOException {
        Path folderPath = databaseRoot.resolve(username).resolve(folder);
        if (!Files.exists(folderPath)) return new ArrayList<>();
        List<Path> files = new ArrayList<>();
        Files.list(folderPath).filter(Files::isRegularFile).forEach(files::add);
        return files;
    }

    public Path getDatabaseRoot() {
        return databaseRoot;
    }
}


