package client;

import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

import client.ui.ClientUI;
import client.ui.Landing;
import shared.LoadingPopup;
import client.core.UdpApiClient;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientApp {

    private Landing landing;
    private ClientUI clientUI;
    private LoadingPopup loadingPopup;
    private UdpApiClient api;
    private String currentUser;
    private String currentPassword;

    public ClientApp() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // You can change host/port here or prompt user for server
        api = new UdpApiClient(getServerHost(), getServerPort());
    }

    public void start() {
        showLanding();
    }

    /** ------------------------
     *  Landing (màn hình đầu tiên: đăng nhập/đăng ký)
     * ------------------------ */
    private void showLanding() {
        landing = new Landing();
        landing.setOnLoginOrRegisterSuccess(() -> {
            landing.undisplay();
            showLoadingAndOpenClientUI();
        });
        landing.setLoginHandler((u, p) -> doLogin(u, p));
        landing.setRegisterHandler((u, p) -> doRegister(u, p));
        landing.display();
    }

    /** ------------------------
     *  ClientUI (màn hình chính sau khi đăng nhập)
     * ------------------------ */
    private void showLoadingAndOpenClientUI() {
        loadingPopup = new LoadingPopup(landing.getFrame(), "Loading mail client");
        loadingPopup.display();

        // Chạy công việc khởi tạo nặng ở background để UI không bị đứng
        new Thread(() -> {
            clientUI = new ClientUI();
            clientUI.setApi(api);
            clientUI.setOnLogout(() -> {
                clientUI.undisplay();
                showLanding(); // return to landing on logout
            });
            // login sync qua UDP: LIST + GETFILE
            if (currentUser != null) {
                clientUI.setCurrentUser(currentUser);
                try {
                    syncUserData(currentUser);
                } catch (IOException ignored) {}
            }
            clientUI.setSyncFromServer(() -> {
                if (currentUser == null || currentPassword == null) return;
                try {
                    if (api.login(currentUser, currentPassword)) {
                        clientUI.clearLists();
                        syncUserData(currentUser);
                    }
                } catch (IOException ignored) {}
            });

            SwingUtilities.invokeLater(() -> {
                loadingPopup.close();
                clientUI.display();
            });
        }).start();
    }

    private String getServerHost() {
        String env = System.getProperty("mailserver.host");
        if (env != null && !env.isBlank()) return env.trim();
        env = System.getenv("MAILSERVER_HOST");
        if (env != null && !env.isBlank()) return env.trim();
        return "127.0.0.1";
    }

    private int getServerPort() {
        String env = System.getProperty("mailserver.port");
        if (env != null && !env.isBlank()) try { return Integer.parseInt(env.trim()); } catch (Exception ignored) {}
        env = System.getenv("MAILSERVER_PORT");
        if (env != null && !env.isBlank()) try { return Integer.parseInt(env.trim()); } catch (Exception ignored) {}
        return 5000;
    }

    private boolean doRegister(String username, String password) {
        try {
            return api.register(username, password);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean doLogin(String username, String password) {
        try {
            boolean ok = api.login(username, password);
            if (ok) {
                currentUser = username;
                currentPassword = password;
                // Prepare UI after landing callback
                if (landing != null) landing.setOnLoginOrRegisterSuccess(() -> {
                    landing.undisplay();
                    showLoadingAndOpenClientUI();
                });
            }
            return ok;
        } catch (IOException e) {
            return false;
        }
    }

    private void syncUserData(String username) throws IOException {
        java.util.List<String> info = java.util.List.of("username=" + username);
        ApiLikeLoginData data = new ApiLikeLoginData();
        data.username = username;
        data.accountInfo = info;
        // list sent/inbox and fetch files
        for (String box : new String[]{"sent", "inbox"}) {
            String[] files = api.list(username, box);
            for (String fn : files) {
                java.util.List<String> lines = api.getFile(username, box, fn);
                data.addFile(box, fn, lines);
            }
        }
        createLocalStorageFromLists(data);
    }

    private void createLocalStorageFromLists(ApiLikeLoginData data) throws IOException {
        java.nio.file.Path root = java.nio.file.Path.of("src", "client", "localStorage", toLocalName(data.username));
        java.nio.file.Files.createDirectories(root.resolve("inbox"));
        java.nio.file.Files.createDirectories(root.resolve("sent"));
        java.nio.file.Files.write(root.resolve("accountInfo.txt"), data.accountInfo, java.nio.charset.StandardCharsets.UTF_8);
        for (var f : data.sentFiles) {
            java.nio.file.Files.write(root.resolve("sent").resolve(f.filename), f.lines, java.nio.charset.StandardCharsets.UTF_8);
            String title = extractField(f.lines, "TITLE=");
            String from = extractField(f.lines, "FROM=");
            String ts = extractField(f.lines, "TIMESTAMP=");
            clientUI.addSentListItem(title, from, ts);
        }
        for (var f : data.inboxFiles) {
            java.nio.file.Files.write(root.resolve("inbox").resolve(f.filename), f.lines, java.nio.charset.StandardCharsets.UTF_8);
            String title = extractField(f.lines, "TITLE=");
            String from = extractField(f.lines, "FROM=");
            String ts = extractField(f.lines, "TIMESTAMP=");
            clientUI.addInboxListItem(title, from, ts);
        }
    }

    private static class ApiLikeLoginData {
        String username;
        java.util.List<String> accountInfo;
        java.util.List<FileLines> sentFiles = new java.util.ArrayList<>();
        java.util.List<FileLines> inboxFiles = new java.util.ArrayList<>();
        void addFile(String box, String filename, java.util.List<String> lines) {
            if ("sent".equals(box)) sentFiles.add(new FileLines(filename, lines));
            else inboxFiles.add(new FileLines(filename, lines));
        }
    }
    private static class FileLines { String filename; java.util.List<String> lines; FileLines(String f, java.util.List<String> l){filename=f;lines=l;} }

    private static String toLocalName(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    // remove TCP-based createLocalStorage(ApiClient.LoginData) – replaced by createLocalStorageFromLists(...)

    private static String extractField(java.util.List<String> lines, String prefix) {
        for (String s : lines) if (s.startsWith(prefix)) return s.substring(prefix.length());
        return "";
    }

    // toLocalName defined above; remove duplicate
}
