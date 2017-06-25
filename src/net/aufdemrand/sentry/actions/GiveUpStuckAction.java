package net.aufdemrand.sentry.actions;

import net.aufdemrand.sentry.SentryInstance;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class GiveUpStuckAction implements StuckAction {
    private final SentryInstance inst;

    GiveUpStuckAction(final SentryInstance inst) {
        this.inst = inst;
    }

    @Override
    public boolean run(final NPC npc, final Navigator navigator) {
        //	inst.plugin.getServer().broadcastMessage("give up stuck action");
        if (!npc.isSpawned()) { return false; }
        final Location base = navigator.getTargetAsLocation();

        if (base.getWorld() == npc.getEntity().getLocation().getWorld()) {
            if (npc.getEntity().getLocation().distanceSquared(base) <= 4) { return true; }
        }

        this.inst.clearTarget();
        return false;
    }

}
