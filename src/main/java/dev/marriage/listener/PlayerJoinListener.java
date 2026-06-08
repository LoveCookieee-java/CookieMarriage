package dev.marriage.listener;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public final class PlayerJoinListener implements Listener {

    private final MarriagePlugin plugin;

    public PlayerJoinListener(MarriagePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {

Optional<CoupleData> opt = plugin.getMarriageService()
                .getCouple(event.getPlayer().getUniqueId());

        opt.ifPresent(couple -> {

            plugin.getAttendanceService().resetSession(couple);
        });
    }
}
