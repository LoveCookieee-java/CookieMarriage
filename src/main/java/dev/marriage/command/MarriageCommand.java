package dev.marriage.command;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MarriageCommand implements CommandExecutor, TabCompleter {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT)
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final MarriagePlugin plugin;

    public MarriageCommand(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("marriage.use")) {
            player.sendMessage(plugin.getMsg("general.no-permission"));
            return true;
        }

        String sub = (args.length > 0) ? args[0].toLowerCase(Locale.ROOT) : "help";

        switch (sub) {
            case "balance", "bal" -> showBalance(player);
            case "info", "status" -> showInfo(player);
            default               -> showHelp(player);
        }

        return true;
    }

    private void showBalance(Player player) {
        Optional<CoupleData> opt = plugin.getMarriageService().getCouple(player.getUniqueId());
        if (opt.isEmpty()) {
            player.sendMessage(plugin.getMsg("general.not-married"));
            return;
        }
        CoupleData couple = opt.get();
        double combined = plugin.getEconomyService().getCombinedBalance(couple);
        player.sendMessage(plugin.getMsg("economy.combined-balance",
                "%couple%",   couple.getCoupleDisplayName(),
                "%balance%",  String.format("%.2f", combined)));
    }

    private void showInfo(Player player) {
        Optional<CoupleData> opt = plugin.getMarriageService().getCouple(player.getUniqueId());
        if (opt.isEmpty()) {
            player.sendMessage(plugin.getMsg("general.not-married"));
            return;
        }
        CoupleData c = opt.get();
        String marriedDate = DATE_FMT.format(Instant.ofEpochMilli(c.getMarriedAtEpoch()));
        int session = plugin.getAttendanceService().getSessionMinutes(c);

        player.sendMessage(plugin.getMsg("marriage.info",
                "%couple%",       c.getCoupleDisplayName(),
                "%married_date%", marriedDate,
                "%streak%",       String.valueOf(c.getStreak()),
                "%session%",      String.valueOf(session)));
    }

    private void showHelp(Player player) {
        player.sendMessage(plugin.getMsg("general.help"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return List.of("balance", "info", "help").stream()
                    .filter(s -> s.startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
