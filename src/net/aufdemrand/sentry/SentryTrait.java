package net.aufdemrand.sentry;

import net.aufdemrand.sentry.SentryInstance.Status;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class SentryTrait extends Trait implements Toggleable {

    private Sentry plugin = null;

    private boolean isToggled = true;
    private SentryInstance thisInstance;

    public SentryTrait() {
        super("sentry");
        this.plugin = (Sentry) Bukkit.getServer().getPluginManager().getPlugin("Sentry");
    }
    @SuppressWarnings("unchecked")
    @Override
    public void load(DataKey key) throws NPCLoadException {
        this.plugin.debug(this.npc, "Load");
        ensureInst();

        if (key.keyExists("traits")) { key = key.getRelative("traits"); }

        this.isToggled = key.getBoolean("toggled", isToggled());
        this.thisInstance.setRetaliate(
            key.getBoolean("Retaliate", this.plugin.getConfig().getBoolean("DefaultOptions.Retaliate", true)));
        this.thisInstance.setInvincible(
            key.getBoolean("Invincinble", this.plugin.getConfig().getBoolean("DefaultOptions.Invincible", false)));
        this.thisInstance.setDropInventory(
            key.getBoolean("DropInventory", this.plugin.getConfig().getBoolean("DefaultOptions.Drops", false)));
        this.thisInstance.setLuckyHits(
            key.getBoolean("CriticalHits", this.plugin.getConfig().getBoolean("DefaultOptions.Criticals", true)));
        this.thisInstance
            .setSentryHealth(key.getDouble("Health", this.plugin.getConfig().getInt("DefaultStats.Health", 20)));
        this.thisInstance.setSentryRange(key.getInt("Range", this.plugin.getConfig().getInt("DefaultStats.Range", 10)));
        this.thisInstance.setRespawnDelaySeconds(
            key.getInt("RespawnDelay", this.plugin.getConfig().getInt("DefaultStats.Respawn", 10)));
        this.thisInstance.setSentrySpeed(
            (float) (key.getDouble("Speed", this.plugin.getConfig().getDouble("DefaultStats.Speed", 1.0))));
        this.thisInstance
            .setSentryWeight(key.getDouble("Weight", this.plugin.getConfig().getDouble("DefaultStats.Weight", 1.0)));
        this.thisInstance.setArmor(key.getInt("Armor", this.plugin.getConfig().getInt("DefaultStats.Armor", 0)));
        this.thisInstance
            .setStrength(key.getInt("Strength", this.plugin.getConfig().getInt("DefaultStats.Strength", 1)));
        this.thisInstance.setFollowDistance(
            key.getInt("FollowDistance", this.plugin.getConfig().getInt("DefaultStats.FollowDistance", 4)));
        this.thisInstance.setGuardTarget(key.getString("GuardTarget", null));
        this.thisInstance.setGreetingMessage(key.getString("Greeting", this.plugin.getConfig()
                                                                                  .getString("DefaultTexts.Greeting",
                                                                                             "'" +
                                                                                             ChatColor.COLOR_CHAR +
                                                                                             "b<NPC> says Welcome, <PLAYER>'")));
        this.thisInstance.setWarningMessage(key.getString("Warning", this.plugin.getConfig()
                                                                                .getString("DefaultTexts.Warning",
                                                                                           "'" + ChatColor.COLOR_CHAR +
                                                                                           "c<NPC> says Halt! Come no closer!'")));
        this.thisInstance.setWarningRange(
            key.getInt("WarningRange", this.plugin.getConfig().getInt("DefaultStats.WarningRange", 0)));
        this.thisInstance.setAttackRateSeconds(
            key.getDouble("AttackRate", this.plugin.getConfig().getDouble("DefaultStats.AttackRate", 2.0)));
        this.thisInstance
            .setHealRate(key.getDouble("HealRate", this.plugin.getConfig().getDouble("DefaultStats.HealRate", 0.0)));
        this.thisInstance
            .setNightVision(key.getInt("NightVision", this.plugin.getConfig().getInt("DefaultStats.NightVision", 16)));
        this.thisInstance.setKillsDropInventory(
            key.getBoolean("KillDrops", this.plugin.getConfig().getBoolean("DefaultOptions.KillDrops", true)));
        this.thisInstance.setIgnoreLOS(
            key.getBoolean("IgnoreLOS", this.plugin.getConfig().getBoolean("DefaultOptions.IgnoreLOS", false)));
        this.thisInstance.setMountID(key.getInt("MountID", -1));
        this.thisInstance.setTargetable(
            key.getBoolean("Targetable", this.plugin.getConfig().getBoolean("DefaultOptions.Targetable", true)));

        if (key.keyExists("getSpawn()")) {
            try {
                this.thisInstance.setSpawn(
                    new Location(this.plugin.getServer().getWorld(key.getString("getSpawn().world")),
                                 key.getDouble("getSpawn().x"), key.getDouble("getSpawn().y"),
                                 key.getDouble("getSpawn().z"), (float) key.getDouble("getSpawn().yaw"),
                                 (float) key.getDouble("getSpawn().pitch")));
            } catch (final Exception e) {
                e.printStackTrace();
                this.thisInstance.setSpawn(null);
            }

            if (this.thisInstance.getSpawn().getWorld() == null) { this.thisInstance.setSpawn(null); }
        }

        if (this.thisInstance.getGuardEntity() != null && this.thisInstance.getGuardEntity().isEmpty()) {
            this.thisInstance.setGuardEntity(null);
        }

        final List<String> targetTemp;
        final List<String> ignoreTemp;

        final Object targets = key.getRaw("Targets");
        if (targets != null) { targetTemp = (List<String>) key.getRaw("Targets"); }
        else { targetTemp = this.plugin.getConfig().getStringList("DefaultTargets"); }

        final Object ignores = key.getRaw("Ignores");
        if (ignores != null) { ignoreTemp = (List<String>) key.getRaw("Ignores"); }
        else { ignoreTemp = this.plugin.getConfig().getStringList("DefaultIgnores"); }

        for (final String string : targetTemp) {
            if (!this.thisInstance.getValidTargets().contains(string.toUpperCase())) {
                this.thisInstance.getValidTargets().add(string.toUpperCase());
            }
        }

        for (final String string : ignoreTemp) {
            if (!this.thisInstance.getIgnoreTargets().contains(string.toUpperCase())) {
                this.thisInstance.getIgnoreTargets().add(string.toUpperCase());
            }

        }

        this.thisInstance.loaded = true;

        this.thisInstance.processTargets();

    }
    @Override
    public void onAttach() {
        this.plugin.debug(this.npc, "onAttach");
        this.isToggled = true;
    }
    @Override
    public void onCopy() {
        this.plugin.debug(this.npc, "onCopy");
        if (this.thisInstance != null) {
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.thisInstance
                .setSpawn(this.npc.getEntity().getLocation().clone()), 10);
        }
    }
    @Override
    public void onDespawn() {
        this.plugin.debug(this.npc, "onDespawn");
        if (this.thisInstance != null) {
            this.thisInstance.isRespawnable =
                System.currentTimeMillis() + this.thisInstance.getRespawnDelaySeconds() * 1000;
            this.thisInstance.setSentryStatus(Status.DEAD);
            this.thisInstance.dismount();
        }
    }
    @Override
    public void onRemove() {

        //	plugin = (Sentry) Bukkit.getPluginManager().getPlugin("Sentry");

        if (this.thisInstance != null) {
            //	plugin.getServer().broadcastMessage("onRemove");
            this.thisInstance.cancelRunnable();
        }

        this.plugin.debug(this.npc, "onRemove");

        this.thisInstance = null;
        this.isToggled = false;
    }
    @Override
    public void onSpawn() {
        this.plugin.debug(this.npc, "getSpawn()");
        ensureInst();

        if (!this.thisInstance.loaded) {
            try {
                this.plugin.debug(this.npc, "getSpawn() call load");
                load(new net.citizensnpcs.api.util.MemoryDataKey());
            } catch (final NPCLoadException e) {
                this.plugin.debug(this.npc, "getSpawn() failed to load");
            }
        }

        if (!this.plugin.groupsChecked) {
            this.plugin.doGroups(); // lazy checking for lazy vault.
        }

        this.thisInstance.initialize();

    }
    @Override
    public void save(final DataKey key) {
        if (this.thisInstance == null) { return; }
        key.setBoolean("toggled", this.isToggled);
        key.setBoolean("Retaliate", this.thisInstance.isRetaliate());
        key.setBoolean("Invincible", this.thisInstance.isInvincible());
        key.setBoolean("DropInventory", this.thisInstance.isDropInventory());
        key.setBoolean("KillDrops", this.thisInstance.doesKillsDropInventory());
        key.setBoolean("Targetable", this.thisInstance.isTargetable());

        key.setInt("MountID", this.thisInstance.getMountID());

        key.setBoolean("CriticalHits", this.thisInstance.isLuckyHits());
        key.setBoolean("IgnoreLOS", this.thisInstance.isIgnoreLOS());
        key.setRaw("Targets", this.thisInstance.getValidTargets());
        key.setRaw("Ignores", this.thisInstance.getIgnoreTargets());

        if (this.thisInstance.getSpawn() != null) {
            key.setDouble("getSpawn().x", this.thisInstance.getSpawn().getX());
            key.setDouble("getSpawn().y", this.thisInstance.getSpawn().getY());
            key.setDouble("getSpawn().z", this.thisInstance.getSpawn().getZ());
            key.setString("getSpawn().world", this.thisInstance.getSpawn().getWorld().getName());
            key.setDouble("getSpawn().yaw", this.thisInstance.getSpawn().getYaw());
            key.setDouble("getSpawn().pitch", this.thisInstance.getSpawn().getPitch());
        }

        key.setDouble("Health", this.thisInstance.getSentryHealth());
        key.setInt("Range", this.thisInstance.getSentryRange());
        key.setInt("RespawnDelay", this.thisInstance.getRespawnDelaySeconds());
        key.setDouble("Speed", this.thisInstance.getSentrySpeed());
        key.setDouble("Weight", this.thisInstance.getSentryWeight());
        key.setDouble("HealRate", this.thisInstance.getHealRate());
        key.setInt("Armor", this.thisInstance.getArmor());
        key.setInt("Strength", this.thisInstance.getStrength());
        key.setInt("WarningRange", this.thisInstance.getWarningRange());
        key.setDouble("AttackRate", this.thisInstance.getAttackRateSeconds());
        key.setInt("NightVision", this.thisInstance.getNightVision());
        key.setInt("FollowDistance", this.thisInstance.getFollowDistance());

        if (this.thisInstance.getGuardTarget() != null) {
            key.setString("GuardTarget", this.thisInstance.getGuardTarget());
        }
        else if (key.keyExists("GuardTarget")) { key.removeKey("GuardTarget"); }

        key.setString("Warning", this.thisInstance.getWarningMessage());
        key.setString("Greeting", this.thisInstance.getGreetingMessage());
    }
    public SentryInstance getInstance() {
        return this.thisInstance;
    }
    private void ensureInst() {
        if (this.thisInstance == null) {
            this.thisInstance = new SentryInstance(this.plugin);
            this.thisInstance.myNPC = this.npc;
            this.thisInstance.myTrait = this;
        }
    }
    @EventHandler
    public void onCitReload(final CitizensReloadEvent event) {
        if (this.thisInstance != null) {
            this.thisInstance.cancelRunnable();
        }
        this.thisInstance = null;
        this.isToggled = false;
    }
    @Override
    public boolean toggle() {
        this.isToggled = !this.isToggled;
        return this.isToggled;
    }

    public boolean isToggled() {
        return this.isToggled;
    }

}
