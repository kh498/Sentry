package net.aufdemrand.sentry;

import net.aufdemrand.sentry.enums.Status;
import net.aufdemrand.sentry.enums.TargetMask;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import static org.bukkit.Effect.ENDER_SIGNAL;

@SuppressWarnings("WeakerAccess")
public class SentryListener implements Listener {

    public final Sentry plugin;

    public SentryListener(final Sentry sentry) {
        this.plugin = sentry;
    }

    @EventHandler
    public static void kill(final org.bukkit.event.entity.EntityDeathEvent event) {

        if (event.getEntity() == null) { return; }

        //don't mess with player death.
        if (event.getEntity() instanceof Player && !event.getEntity().hasMetadata("NPC")) { return; }

        Entity killer = event.getEntity().getKiller();
        if (killer == null) {
            //might have been a projectile.
            final EntityDamageEvent ev = event.getEntity().getLastDamageCause();
            if (ev != null && ev instanceof EntityDamageByEntityEvent) {
                killer = ((EntityDamageByEntityEvent) ev).getDamager();
                if (killer instanceof Projectile && ((Projectile) killer).getShooter() instanceof Entity) {
                    killer = (Entity) ((Projectile) killer).getShooter();
                }
            }
        }

        final SentryInstance sentry = Sentry.getSentry(killer);

        if (sentry != null && !sentry.doesKillsDropInventory()) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public static void despawn(final net.citizensnpcs.api.event.NPCDespawnEvent event) {
        final SentryInstance sentry = Sentry.getSentry(event.getNPC());
        //don't despawn active bodyguards on chunk unload
        if (sentry != null && event.getReason() == net.citizensnpcs.api.event.DespawnReason.CHUNK_UNLOAD &&
            sentry.getGuardEntity() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void projectileHit(final ProjectileHitEvent event) {
        if (event.getEntity() instanceof EnderPearl && event.getEntity().getShooter() instanceof Entity) {
            final SentryInstance sentry = Sentry.getSentry((Entity) event.getEntity().getShooter());
            if (sentry != null) {
                sentry.setEpCount(sentry.getEpCount() - 1);
                if (sentry.getEpCount() < 0) { sentry.setEpCount(0); }
                event.getEntity().getLocation().getWorld()
                     .playEffect(event.getEntity().getLocation(), ENDER_SIGNAL, 1, 100);
                //enderpearl from a sentry
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public static void EnvDamage(final EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent || event.isCancelled()) { return; }

        final SentryInstance inst = Sentry.getSentry(event.getEntity());
        if (inst == null) { return; }

        event.setCancelled(true);

        final DamageCause cause = event.getCause();
        //	plugin.getLogger().log(Level.INFO, "Damage " + cause.toString() + " " + event.getDamage());

        switch (cause) {
            case CONTACT:
            case DROWNING:
            case LAVA:
            case SUFFOCATION:
            case CUSTOM:
            case BLOCK_EXPLOSION:
            case VOID:
            case SUICIDE:
            case MAGIC:
                inst.onEnvironmentDamage(event);
                break;
            case FALL:
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST) //highest for worldguard...
    public void onDamage(final EntityDamageByEntityEvent event) {

        Entity attacker = event.getDamager();
        final Entity defender = event.getEntity();

        if (attacker instanceof Projectile) {
            final ProjectileSource source = ((Projectile) attacker).getShooter();
            if (source instanceof Entity) {
                attacker = (Entity) ((org.bukkit.entity.Projectile) attacker).getShooter();
            }
        }

        final SentryInstance sentryAttacker = Sentry.getSentry(attacker);
        final SentryInstance sentryDefender = Sentry.getSentry(defender);

        this.plugin.debug(
            "start: from: " + attacker + " to " + defender + " cancelled " + event.isCancelled() + " damage " +
            event.getDamage() + " cause " + event.getCause());

        if (sentryAttacker != null) {
            //projectiles go through ignore targets.
            if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
                if (defender instanceof LivingEntity && sentryAttacker.isIgnored((LivingEntity) defender)) {
                    event.setCancelled(true);
                    event.getDamager().remove();
                    final Projectile newProjectile = (Projectile) (attacker.getWorld().spawnEntity(
                        event.getDamager().getLocation().add(event.getDamager().getVelocity()),
                        event.getDamager().getType()));
                    newProjectile.setVelocity(event.getDamager().getVelocity());
                    newProjectile.setShooter((LivingEntity) attacker);
                    newProjectile.setTicksLived(event.getDamager().getTicksLived());
                    return;
                }
            }

            //from a sentry
            event.setDamage(sentryAttacker.getStrength());

            //un-cancel if not bodyguard.
            if (sentryAttacker.getGuardTarget() == null || !this.plugin.bodyguardsObeyProtection) {
                event.setCancelled(false);
            }

            //cancel if invulnerable non-sentry npc
            if (sentryDefender == null) {
                final NPC npcDamaged = CitizensAPI.getNPCRegistry().getNPC(defender);
                if (npcDamaged != null) {
                    final boolean isProtected = npcDamaged.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
                    event.setCancelled(isProtected);
                }
            }

            //don't hurt guard target.
            if (defender.equals(sentryAttacker.getGuardEntity())) { event.setCancelled(true); }

            //stop hitting yourself.
            if (attacker.equals(defender)) { event.setCancelled(true); }

            //apply potion effects
            if (sentryAttacker.getPotionEffects() != null && !event.isCancelled() && defender instanceof LivingEntity) {
                ((LivingEntity) defender).addPotionEffects(sentryAttacker.getPotionEffects());
            }
        }

        boolean ok = false; //When you try your best but you don't succeeded :,(

        if (sentryDefender != null) {
            //to a sentry

            //stop hitting yourself.
            if (attacker.equals(defender)) { return; }

            //only bodyguards obey pvp-protection
            if (sentryDefender.getGuardTarget() == null) { event.setCancelled(false); }

            //don't take damage from guard entity.
            if (attacker.equals(sentryDefender.getGuardEntity())) { event.setCancelled(true); }

            final NPC npc = CitizensAPI.getNPCRegistry().getNPC(attacker);
            if (npc != null && npc.hasTrait(SentryTrait.class) && sentryDefender.getGuardEntity() != null) {
                if (npc.getTrait(SentryTrait.class).getInstance().getGuardEntity()
                       .equals(sentryDefender.getGuardEntity())) { //don't take damage from co-guards.
                    event.setCancelled(true);
                }
            }

            //process event
            if (!event.isCancelled()) {
                ok = true;
                sentryDefender.onDamage(event);
            }

            //Damage to a sentry cannot be handled by the server. Always cancel the event here.
            event.setCancelled(true);
        }

        //process this event on each sentry to check for respondable events.
        if ((!event.isCancelled() || ok) && !attacker.equals(defender) && event.getDamage() > 0) {
            checkForEvents(defender, attacker, event);
        }
    }

    private void checkForEvents(final Entity defender, final Entity attacker, final EntityDamageByEntityEvent event) {
        for (final NPC npc : CitizensAPI.getNPCRegistry()) {
            final SentryInstance sentryInst = Sentry.getSentry(npc);

            if (sentryInst == null || !npc.isSpawned() || !npc.getEntity().getWorld().equals(defender.getWorld())) {
                continue; //not a sentry, or not this world, or dead.
            }

            if (sentryInst.getGuardEntity() != null && sentryInst.getGuardEntity().equals(defender)) {
                if (sentryInst.isRetaliate() && attacker instanceof LivingEntity) {
                    sentryInst.setTarget((LivingEntity) attacker, true);
                }
            }

            //are u attacking mai horse?
            if (sentryInst.getMountNPC() != null && sentryInst.getMountNPC().getEntity().equals(defender)) {
                if (attacker.equals(sentryInst.getGuardEntity())) {
                    event.setCancelled(true);
                }
                else if (sentryInst.isRetaliate() && attacker instanceof LivingEntity) {
                    sentryInst.setTarget((LivingEntity) attacker, true);
                }
            }

            if (sentryInst.hasTargetType(TargetMask.EVENTS) && sentryInst.getSentryStatus() == Status.LOOKING &&
                attacker instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(attacker)) {
                //pv-something event.
                if (npc.getEntity().getLocation().distance(defender.getLocation()) <= sentryInst.getSentryRange() ||
                    npc.getEntity().getLocation().distance(attacker.getLocation()) <= sentryInst.getSentryRange()) {
                    // in range
                    if (sentryInst.getNightVision() >= attacker.getLocation().getBlock().getLightLevel() ||
                        sentryInst.getNightVision() >= defender.getLocation().getBlock().getLightLevel()) {
                        //can see
                        if (sentryInst.hasLOS(attacker) || sentryInst.hasLOS(defender)) {
                            //have los
                            if (!(defender instanceof Player) && sentryInst.containsTarget("event:pve") ||
                                defender instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(defender) &&
                                sentryInst.containsTarget("event:pvp") ||
                                CitizensAPI.getNPCRegistry().isNPC(defender) &&
                                sentryInst.containsTarget("event:pvnpc") ||
                                sentryInst.containsTarget("event:pvsentry")) {

                                //Valid event, attack
                                if (!sentryInst.isIgnored((LivingEntity) attacker)) {
                                    this.plugin.debug("");
                                    sentryInst.setTarget((LivingEntity) attacker, true); //attack the aggressor
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(final net.citizensnpcs.api.event.NPCDeathEvent event) {
        final NPC eventNPC = event.getNPC();
        //if the mount dies carry aggression over.
        for (final NPC npc : CitizensAPI.getNPCRegistry()) {
            final SentryInstance inst = Sentry.getSentry(npc);
            if (inst == null || !npc.isSpawned() || !inst.isMounted()) {
                continue; //not a sentry, dead, or not mounted
            }
            if (eventNPC.getId() == inst.getMountID()) {
                ///nooooo butterstuff!

                Entity killer = ((LivingEntity) eventNPC.getEntity()).getKiller();
                if (killer == null) {
                    //might have been a projectile.
                    final EntityDamageEvent ev = eventNPC.getEntity().getLastDamageCause();
                    if (ev != null && ev instanceof EntityDamageByEntityEvent) {
                        killer = ((EntityDamageByEntityEvent) ev).getDamager();
                        if (killer instanceof Projectile && ((Projectile) killer).getShooter() instanceof Entity) {
                            killer = (Entity) ((Projectile) killer).getShooter();
                        }
                    }
                }

                final LivingEntity livingKiller = killer instanceof LivingEntity ? (LivingEntity) killer : null;

                if (livingKiller == null) { return; }
                if (inst.isIgnored(livingKiller)) { return; }

                //delay so the mount is gone.
                this.plugin.getServer().getScheduler()
                           .scheduleSyncDelayedTask(this.plugin, () -> inst.setTarget(livingKiller, true), 2);

                return;
            }
        }
    }

    @EventHandler
    public static void onNPCRightClick(final net.citizensnpcs.api.event.NPCRightClickEvent event) {
        final SentryInstance inst = Sentry.getSentry(event.getNPC());
        if (inst == null) { return; }

        if (inst.getMyNPC().getEntity() instanceof org.bukkit.entity.Horse) {
            if (!inst.getGuardEntity().equals(event.getClicker())) {
                event.setCancelled(true);
            }
        }
    }

}
