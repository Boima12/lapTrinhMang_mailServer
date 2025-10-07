package server.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory pub/sub for overview records so we don't touch the filesystem.
 */
public final class OverviewBus {
    public interface Listener {
        void onRecord(String timestamp, String from, String to, String title, long sizeBytes);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private OverviewBus() {}

    public static void subscribe(Listener l) { if (l != null) listeners.add(l); }
    public static void unsubscribe(Listener l) { if (l != null) listeners.remove(l); }
    public static void emit(String timestamp, String from, String to, String title, long sizeBytes) {
        for (Listener l : listeners) l.onRecord(timestamp, from, to, title, sizeBytes);
    }
}


