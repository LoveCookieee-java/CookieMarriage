package dev.marriage.service;

import dev.marriage.MarriagePlugin;
import dev.marriage.model.CoupleData;
import dev.marriage.model.PendingProposal;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class MarriageService {

    private final MarriagePlugin plugin;
    private final Economy economy;

private final Map<UUID, CoupleData> coupleCache = new ConcurrentHashMap<>();

private final Map<UUID, PendingProposal> proposals = new ConcurrentHashMap<>();

    public MarriageService(MarriagePlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        loadAllCouples();
    }

private void loadAllCouples() {
        List<CoupleData> all = plugin.getDatabaseManager().getCoupleRepository().findAll();
        for (CoupleData couple : all) {
            coupleCache.put(couple.getPlayer1Uuid(), couple);
            coupleCache.put(couple.getPlayer2Uuid(), couple);
        }
        plugin.getLogger().info("Loaded " + all.size() + " couple(s) from database.");
    }

public boolean isMarried(UUID uuid) {
        return coupleCache.containsKey(uuid);
    }

    public Optional<CoupleData> getCouple(UUID uuid) {
        return Optional.ofNullable(coupleCache.get(uuid));
    }

public Collection<CoupleData> getAllCouples() {

return new HashSet<>(coupleCache.values());
    }

public void sendProposal(Player proposer, Player target) {
        UUID proposerUuid = proposer.getUniqueId();
        UUID targetUuid   = target.getUniqueId();

if (proposerUuid.equals(targetUuid)) {
            proposer.sendMessage(plugin.getMsg("marry.cannot-self-marry"));
            return;
        }
        if (isMarried(proposerUuid)) {
            proposer.sendMessage(plugin.getMsg("marry.already-married-self"));
            return;
        }
        if (isMarried(targetUuid)) {
            proposer.sendMessage(plugin.getMsg("marry.already-married-target", "%player%", target.getName()));
            return;
        }
        if (proposals.containsKey(proposerUuid)) {
            proposer.sendMessage(plugin.getMsg("marry.proposal-pending"));
            return;
        }

PendingProposal reverse = proposals.get(targetUuid);
        if (reverse != null && reverse.getTargetUuid().equals(proposerUuid)) {
            reverse.cancelExpireTask();
            proposals.remove(targetUuid);
            performMarriage(target, proposer);
            return;
        }

        int timeoutSecs = plugin.getConfigManager().getProposalTimeoutSeconds();

        PendingProposal proposal = new PendingProposal(proposerUuid, targetUuid, System.currentTimeMillis());

        BukkitTask expireTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            expireProposal(proposerUuid, target.getName());
        }, (long) timeoutSecs * 20);

        proposal.setExpireTask(expireTask);
        proposals.put(proposerUuid, proposal);

proposer.sendMessage(plugin.getMsg("marry.proposal-sent",
                "%player%", target.getName(), "%timeout%", String.valueOf(timeoutSecs)));
        target.sendMessage(plugin.getMsg("marry.proposal-received", "%player%", proposer.getName()));
    }

    public void acceptProposal(Player accepter) {
        PendingProposal proposal = findProposalTargeting(accepter.getUniqueId());
        if (proposal == null) {
            accepter.sendMessage(plugin.getMsg("marry.no-proposal"));
            return;
        }

        Player proposer = Bukkit.getPlayer(proposal.getProposerUuid());
        if (proposer == null) {
            accepter.sendMessage(plugin.getMsg("marry.proposer-offline"));
            removeProposal(proposal.getProposerUuid());
            return;
        }

        removeProposal(proposal.getProposerUuid());
        performMarriage(proposer, accepter);
    }

    public void denyProposal(Player denier) {
        PendingProposal proposal = findProposalTargeting(denier.getUniqueId());
        if (proposal == null) {
            denier.sendMessage(plugin.getMsg("marry.no-proposal"));
            return;
        }

        String proposerName = Objects.requireNonNullElse(
                Bukkit.getOfflinePlayer(proposal.getProposerUuid()).getName(), "Unknown");

        removeProposal(proposal.getProposerUuid());

        denier.sendMessage(plugin.getMsg("marry.proposal-denied-denier", "%player%", proposerName));

        Player proposer = Bukkit.getPlayer(proposal.getProposerUuid());
        if (proposer != null) {
            proposer.sendMessage(plugin.getMsg("marry.proposal-denied-proposer", "%player%", denier.getName()));
        }
    }

public void cleanupProposalsFor(UUID uuid) {

        removeProposal(uuid);

}

private void performMarriage(Player p1, Player p2) {

        double combinedBalance = economy.getBalance(p1) + economy.getBalance(p2);

        CoupleData couple = new CoupleData(
                p1.getUniqueId(), p2.getUniqueId(),
                p1.getName(), p2.getName(),
                Instant.now().toEpochMilli(),
                0, null, combinedBalance);

plugin.getDatabaseManager().getCoupleRepository().save(couple);

coupleCache.put(p1.getUniqueId(), couple);
        coupleCache.put(p2.getUniqueId(), couple);

plugin.getAttendanceService().initializeCouple(couple);
        plugin.getEconomyService().initializeCouple(couple);

String msg = plugin.getMsg("marry.success",
                "%player1%", p1.getName(), "%player2%", p2.getName());
        Bukkit.broadcast(plugin.msgToComponent(msg));

        plugin.getLogger().info("Marriage registered: " + couple.getCoupleDisplayName());
    }

public void divorce(Player requester) {
        Optional<CoupleData> opt = getCouple(requester.getUniqueId());
        if (opt.isEmpty()) {
            requester.sendMessage(plugin.getMsg("divorce.not-married"));
            return;
        }
        CoupleData couple = opt.get();
        performDivorce(couple);
    }

    private void performDivorce(CoupleData couple) {
        OfflinePlayer op1 = Bukkit.getOfflinePlayer(couple.getPlayer1Uuid());
        OfflinePlayer op2 = Bukkit.getOfflinePlayer(couple.getPlayer2Uuid());

        double b1 = economy.getBalance(op1);
        double b2 = economy.getBalance(op2);
        double total = b1 + b2;
        double each  = total / 2.0;

plugin.getEconomyService().removeCouple(couple);
        plugin.getAttendanceService().removeCouple(couple);

plugin.getDatabaseManager().getCoupleRepository().deleteByPlayer(couple.getPlayer1Uuid());
        coupleCache.remove(couple.getPlayer1Uuid());
        coupleCache.remove(couple.getPlayer2Uuid());

if (plugin.getConfigManager().isSplitOnDivorce() && total > 0) {
            splitBalance(op1, b1, each);
            splitBalance(op2, b2, each);
        }

String divorceMsg = plugin.getMsg("divorce.success",
                "%player1%", couple.getPlayer1Name(),
                "%player2%", couple.getPlayer2Name());
        Bukkit.broadcast(plugin.msgToComponent(divorceMsg));

String eachMsg = plugin.getMsg("divorce.each-received",
                "%amount%", formatMoney(each));
        Player p1 = Bukkit.getPlayer(couple.getPlayer1Uuid());
        Player p2 = Bukkit.getPlayer(couple.getPlayer2Uuid());
        if (p1 != null) p1.sendMessage(eachMsg);
        if (p2 != null) p2.sendMessage(eachMsg);

        plugin.getLogger().info("Divorce processed: " + couple.getCoupleDisplayName()
                + " | each received " + formatMoney(each));
    }

private void splitBalance(OfflinePlayer player, double current, double target) {
        double delta = target - current;
        if (Math.abs(delta) < 0.001) return; 

        if (delta < 0) {

            EconomyResponse resp = economy.withdrawPlayer(player, -delta);
            if (!resp.transactionSuccess()) {
                plugin.getLogger().warning("Divorce withdraw failed for " + player.getName()
                        + ": " + resp.errorMessage);
            }
        } else {

            EconomyResponse resp = economy.depositPlayer(player, delta);
            if (!resp.transactionSuccess()) {
                plugin.getLogger().warning("Divorce deposit failed for " + player.getName()
                        + ": " + resp.errorMessage);
            }
        }
    }

private PendingProposal findProposalTargeting(UUID targetUuid) {
        for (PendingProposal p : proposals.values()) {
            if (p.getTargetUuid().equals(targetUuid)) {
                return p;
            }
        }
        return null;
    }

    private void removeProposal(UUID proposerUuid) {
        PendingProposal p = proposals.remove(proposerUuid);
        if (p != null) p.cancelExpireTask();
    }

    private void expireProposal(UUID proposerUuid, String targetName) {

        proposals.remove(proposerUuid);

        Player proposer = Bukkit.getPlayer(proposerUuid);
        if (proposer != null) {
            proposer.sendMessage(plugin.getMsg("marry.proposal-expired-proposer", "%player%", targetName));
        }
    }

    private static String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}
