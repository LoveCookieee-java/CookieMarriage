package dev.marriage.reward;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public final class RewardManager {

    private final MarriagePlugin plugin;

private final TreeMap<Integer, List<String>> milestones = new TreeMap<>();

    public RewardManager(MarriagePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

public void reload() {
        milestones.clear();

        File file = new File(plugin.getDataFolder(), "reward.yml");
        if (!file.exists()) {
            plugin.saveResource("reward.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        var section = config.getConfigurationSection("milestone");

        if (section == null) {
            plugin.getLogger().warning("reward.yml is missing the 'milestone' section!");
            return;
        }

        int loaded = 0;
        for (String key : section.getKeys(false)) {
            try {
                int day = Integer.parseInt(key);
                List<String> commands = section.getStringList(key);
                if (!commands.isEmpty()) {
                    milestones.put(day, commands);
                    loaded++;
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid milestone key in reward.yml: '" + key
                        + "' (must be an integer).");
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " reward milestone(s).");
    }

public void checkAndDispatch(CoupleData couple) {
        int streak = couple.getStreak();
        List<String> commands = milestones.get(streak);

        if (commands == null || commands.isEmpty()) return;

        plugin.getLogger().info("Dispatching " + commands.size() + " reward command(s) for streak "
                + streak + " — " + couple.getCoupleDisplayName());

        if (Bukkit.isPrimaryThread()) {
            dispatchCommands(commands, couple);
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> dispatchCommands(commands, couple));
        }
    }

private void dispatchCommands(List<String> commands, CoupleData couple) {
        for (String rawCommand : commands) {
            if (rawCommand.isBlank()) continue;

            try {
                if (rawCommand.contains("%couple%")) {

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            expand(rawCommand, couple, couple.getPlayer1Name()));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            expand(rawCommand, couple, couple.getPlayer2Name()));
                } else {

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            expand(rawCommand, couple, null));
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to dispatch reward command: " + rawCommand, e);
            }
        }
    }

private static String expand(String command, CoupleData couple, String coupleSlot) {
        String result = command
                .replace("%player1%", couple.getPlayer1Name())
                .replace("%player2%", couple.getPlayer2Name())
                .replace("%streak%",  String.valueOf(couple.getStreak()));

        if (coupleSlot != null) {
            result = result.replace("%couple%", coupleSlot);
        }

        return result;
    }

public Map<Integer, List<String>> getMilestones() {
        return java.util.Collections.unmodifiableMap(milestones);
    }
}
