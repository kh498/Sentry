package net.aufdemrand.sentry.strategies;

import net.aufdemrand.sentry.Sentry;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class SpiderAttackStrategy implements net.citizensnpcs.api.ai.AttackStrategy {
    private Sentry plugin = null;
    private final Random random = new Random();

    public SpiderAttackStrategy(final Sentry plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean handle(final LivingEntity arg0, final LivingEntity arg1) {

        this.plugin.debug("Spider ATTACK!");

        final Entity entity = ((CraftEntity) arg1).getHandle();
        final Entity me = ((CraftEntity) arg0).getHandle();

        if (this.random.nextInt(20) == 0) {
            final double d0 = entity.locX - me.locX;
            final double d1 = entity.locZ - me.locZ;
            final double f2 = Math.sqrt(d0 * d0 + d1 * d1);

            me.motX = d0 / f2 * 0.5D * 0.800000011920929D + me.motX * 0.20000000298023224D;
            me.motZ = d1 / f2 * 0.5D * 0.800000011920929D + me.motZ * 0.20000000298023224D;
            me.motY = 0.4000000059604645D;
        }

        return false;
    }
}
