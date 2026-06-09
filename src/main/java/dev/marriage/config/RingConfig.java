package dev.marriage.config;

import dev.marriage.MarriagePlugin;
import dev.marriage.item.RingItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Đọc và cung cấp cấu hình nhẫn cưới từ ring.yml.
 */
public final class RingConfig {

    private final MarriagePlugin plugin;

    private boolean requireRing;
    private List<RingItem> ringItems = new ArrayList<>();

    public RingConfig(MarriagePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    /** Tải (hoặc tải lại) ring.yml từ thư mục data plugin. */
    public void load() {
        // Sao chép file mặc định nếu chưa tồn tại
        File file = new File(plugin.getDataFolder(), "ring.yml");
        if (!file.exists()) {
            plugin.saveResource("ring.yml", false);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        requireRing = cfg.getBoolean("require-ring", true);

        List<String> rawList = cfg.getStringList("ring-items");
        List<RingItem> parsed = new ArrayList<>();
        for (String raw : rawList) {
            RingItem item = RingItem.parse(raw);
            if (item == null) {
                plugin.getLogger().log(Level.WARNING,
                        "[RingConfig] Không thể parse ring item: \"" + raw + "\" — bỏ qua.");
            } else {
                parsed.add(item);
            }
        }
        ringItems = Collections.unmodifiableList(parsed);

        plugin.getLogger().info("[RingConfig] require-ring=" + requireRing
                + ", " + ringItems.size() + " item(s) đã load.");
    }

    /** @return true nếu nhẫn cưới là bắt buộc để cầu hôn */
    public boolean isRequireRing() {
        return requireRing;
    }

    /** @return danh sách các item hợp lệ làm nhẫn cưới */
    public List<RingItem> getRingItems() {
        return ringItems;
    }
}
