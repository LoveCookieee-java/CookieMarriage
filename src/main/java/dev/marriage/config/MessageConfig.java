package dev.marriage.config;

import dev.marriage.MarriagePlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class MessageConfig {

    private final MarriagePlugin plugin;
    private FileConfiguration config;
    private String prefix;

    public MessageConfig(MarriagePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);

InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8)));
        }

String rawPrefix = Objects.requireNonNullElse(
                config.getString("prefix"), "&6[&eMarriage&6] &r");
        prefix = color(rawPrefix);
    }

public String get(String key, Object... replacements) {

String raw = Objects.requireNonNullElse(
                config.getString(key), "&cMissing message: " + key);

raw = raw.replace("%prefix%", prefix);

for (int i = 0; i + 1 < replacements.length; i += 2) {
            if (replacements[i] != null && replacements[i + 1] != null) {
                raw = raw.replace(replacements[i].toString(), replacements[i + 1].toString());
            }
        }

        return color(raw);
    }

public String getMultiline(String key, Object... replacements) {
        return get(key, replacements);
    }

private static String color(String input) {

        return LegacyComponentSerializer.legacySection().serialize(
                LegacyComponentSerializer.legacyAmpersand().deserialize(input));
    }

public String getPrefix() {
        return prefix;
    }
}
