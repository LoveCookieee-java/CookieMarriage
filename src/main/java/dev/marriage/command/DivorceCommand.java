package dev.marriage.command;

import dev.marriage.MarriagePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DivorceCommand implements CommandExecutor {

    private final MarriagePlugin plugin;

    public DivorceCommand(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("marriage.divorce")) {
            player.sendMessage(plugin.getMsg("general.no-permission"));
            return true;
        }

        plugin.getMarriageService().divorce(player);
        return true;
    }
}
