
package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Lưu / đọc cấu hình nhỏ cho ứng dụng (ví dụ thư mục lưu PDF mặc định).
 *  File lưu tại: {user.home}/.ve-tau-prefs.json
 */
public class UserPrefs {
    private static final File PREF_FILE = new File(System.getProperty("user.home"), ".ve-tau-prefs.json");
    private static Map<String, String> cache = null;

    private static synchronized Map<String, String> loadAll() {
        if (cache != null) return cache;
        cache = new HashMap<>();
        if (!PREF_FILE.exists()) return cache;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(PREF_FILE), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String json = sb.toString().trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                // Rất đơn giản, tách theo cặp "k":"v" (không cần thư viện JSON).
                json = json.substring(1, json.length()-1).trim();
                if (!json.isEmpty()) {
                    String[] parts = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    for (String p : parts) {
                        String[] kv = p.split(":", 2);
                        if (kv.length == 2) {
                            String k = kv[0].trim();
                            String v = kv[1].trim();
                            if (k.startsWith("\"") && k.endsWith("\"")) k = k.substring(1, k.length()-1);
                            if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length()-1);
                            cache.put(k, v);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return cache;
    }

    private static synchronized void saveAll(Map<String, String> data) {
        cache = new HashMap<>(data);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PREF_FILE), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, String> e : data.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                sb.append("\"").append(e.getValue().replace("\"", "\\\"")).append("\"");
                first = false;
            }
            sb.append("}");
            bw.write(sb.toString());
        } catch (IOException ignored) {}
    }

    public static Optional<File> getDefaultSaveDir() {
        String v = loadAll().get("default_save_dir");
        if (v == null || v.isEmpty()) return Optional.empty();
        File f = new File(v);
        if (f.exists() && f.isDirectory()) {
            return Optional.of(f);
        }
        return Optional.empty();
    }

    public static void setDefaultSaveDir(File dir) {
        if (dir == null) return;
        Map<String, String> all = loadAll();
        all.put("default_save_dir", dir.getAbsolutePath());
        saveAll(all);
    }
}
