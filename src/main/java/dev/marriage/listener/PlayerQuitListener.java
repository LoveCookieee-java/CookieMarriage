package dev.marriage.listener;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public final class PlayerQuitListener implements Listener {

    private final MarriagePlugin plugin;

    public PlayerQuitListener(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();

plugin.getMarriageService().cleanupProposalsFor(uuid);

Optional<CoupleData> opt = plugin.getMarriageService().getCouple(uuid);
        opt.ifPresent(couple -> plugin.getAttendanceService().resetSession(couple));
    }
}
