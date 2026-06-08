package dev.marriage.command;

import dev.marriage.MarriagePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MarryCommand implements CommandExecutor, TabCompleter {

    private final MarriagePlugin plugin;

    public MarryCommand(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("marriage.marry")) {
            player.sendMessage(plugin.getMsg("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMsg("general.usage-marry"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "accept" -> plugin.getMarriageService().acceptProposal(player);
            case "deny"   -> plugin.getMarriageService().denyProposal(player);
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(plugin.getMsg("general.player-not-found", "%player%", args[0]));
                    return true;
                }
                plugin.getMarriageService().sendProposal(player, target);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return List.of();

        String input = args[0].toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();

if ("accept".startsWith(input)) completions.add("accept");
        if ("deny".startsWith(input))   completions.add("deny");

for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ROOT).startsWith(input) && !p.equals(sender)) {
                completions.add(p.getName());
            }
        }

        return completions;
    }
}
