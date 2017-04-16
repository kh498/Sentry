package net.aufdemrand.sentry;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BodyguardTeleportStuckAction implements StuckAction {
    private static final int MAX_ITERATIONS = 10;
    SentryInstance inst = null;
    Sentry plugin = null;

    BodyguardTeleportStuckAction(final SentryInstance inst, final Sentry plugin) {
        this.inst = inst;
        this.plugin = plugin;
    }
    @Override
    public boolean run(final NPC npc, final Navigator navigator) {

        if (!npc.isSpawned()) { return false; }

        final Location base = navigator.getTargetAsLocation();

        if (base.getWorld() == npc.getEntity().getLocation().getWorld()) {
            if (npc.getEntity().getLocation().distanceSquared(base) <= 4)
            //do nothing
            { return true; }
        }
        else {
            //do nothing, next logic tick will clear the entity.
            if (this.inst.getGuardEntity() == null || !Util.CanWarp(this.inst.getGuardEntity(), npc)) { return true; }
        }

        Block block = base.getBlock();
        int iterations = 0;
        while (!block.isEmpty()) {
            block = block.getRelative(BlockFace.UP);
            if (++iterations >= MAX_ITERATIONS && !block.isEmpty()) { block = base.getBlock(); }
            break;
        }

        final Location loc = block.getLocation();

        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> npc
            .teleport(loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN), 2);

        return false;
    }
}