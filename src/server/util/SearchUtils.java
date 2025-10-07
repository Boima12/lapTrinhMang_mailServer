package server.util;

import java.util.ArrayList;
import java.util.List;
import server.model.RecordListItem;

/**
 * Tiện ích lọc danh sách bản ghi theo tiêu chí tìm kiếm.
 */
public final class SearchUtils {
    private SearchUtils() {}

    public static List<RecordListItem> filterByUser(List<RecordListItem> all, String query) {
        List<RecordListItem> out = new ArrayList<>();
        String q = query == null ? "" : query.trim();
        for (RecordListItem it : all) {
            if (it.getFrom().contains(q) || it.getTo().contains(q)) out.add(it);
        }
        return out;
    }

    public static List<RecordListItem> filterByTitle(List<RecordListItem> all, String query) {
        List<RecordListItem> out = new ArrayList<>();
        String q = query == null ? "" : query.toLowerCase();
        for (RecordListItem it : all) {
            if (it.getTitle().toLowerCase().contains(q)) out.add(it);
        }
        return out;
    }

    public static List<RecordListItem> filterByTime(List<RecordListItem> all, String start, String end) {
        List<RecordListItem> out = new ArrayList<>();
        String s = start == null ? "" : start.trim();
        String e = end == null ? "" : end.trim();
        for (RecordListItem it : all) {
            String ts = it.getTimestamp();
            int cmpS = s.isEmpty() ? -1 : ts.compareTo(s);
            int cmpE = e.isEmpty() ? 1 : ts.compareTo(e);
            if ((s.isEmpty() || cmpS >= 0) && (e.isEmpty() || cmpE <= 0)) out.add(it);
        }
        return out;
    }
}


