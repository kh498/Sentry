package net.aufdemrand.sentry;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.LivingEntity;

class MountAttackStrategy implements net.citizensnpcs.api.ai.AttackStrategy {
    // make the rider attack when in range.

    @Override
    public boolean handle(final LivingEntity attacker, final LivingEntity bukkitTarget) {
        return attacker == bukkitTarget || attacker.getPassenger() != null &&
                                           CitizensAPI.getNPCRegistry().getNPC(attacker.getPassenger()).getNavigator()
                                                      .getDefaultParameters().attackStrategy()
                                                      .handle((LivingEntity) attacker.getPassenger(), bukkitTarget);
    }
}
