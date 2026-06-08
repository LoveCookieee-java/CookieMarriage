package dev.marriage.model;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class PendingProposal {

    private final UUID proposerUuid;
    private final UUID targetUuid;
    private final long proposedAtMs;        

private BukkitTask expireTask;

    public PendingProposal(UUID proposerUuid, UUID targetUuid, long proposedAtMs) {
        this.proposerUuid = proposerUuid;
        this.targetUuid = targetUuid;
        this.proposedAtMs = proposedAtMs;
    }

public UUID getProposerUuid() { return proposerUuid; }
    public UUID getTargetUuid()   { return targetUuid; }
    public long getProposedAtMs() { return proposedAtMs; }

public void setExpireTask(BukkitTask task) {
        this.expireTask = task;
    }

public void cancelExpireTask() {
        if (expireTask != null && !expireTask.isCancelled()) {
            expireTask.cancel();
        }
        expireTask = null;
    }
}
