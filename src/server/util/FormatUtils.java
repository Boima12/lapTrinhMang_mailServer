package server.util;

/**
 * Tiện ích định dạng hiển thị (SRP: chỉ định dạng chuỗi/kích thước).
 */
public final class FormatUtils {
    private FormatUtils() {}

    /**
     * Định dạng kích thước theo KB/MB.
     */
    public static String formatSize(long sizeBytes) {
        if (sizeBytes <= 0) return "Unknown";
        double kb = sizeBytes / 1024.0;
        if (kb < 1024) return String.format(java.util.Locale.US, "%.0f KB", kb);
        double mb = kb / 1024.0;
        return String.format(java.util.Locale.US, "%.2f MB", mb);
    }
}


