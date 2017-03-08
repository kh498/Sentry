package net.aufdemrand.sentry;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.LivingEntity;

public class MountAttackStrategy implements net.citizensnpcs.api.ai.AttackStrategy {
    // make the rider attack when in range.

    @Override
    public boolean handle(LivingEntity attacker, LivingEntity bukkitTarget) {
        if (attacker == bukkitTarget) { return true; }

        return attacker.getPassenger() != null &&
               CitizensAPI.getNPCRegistry().getNPC(attacker.getPassenger()).getNavigator().getDefaultParameters()
                          .attackStrategy().handle((LivingEntity) attacker.getPassenger(), bukkitTarget);
    }
}
