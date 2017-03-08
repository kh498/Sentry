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

public class SentryTrait extends Trait implements Toggleable {

    private Sentry plugin = null;

    private boolean isToggled = true;
    private SentryInstance thisInstance;

    public SentryTrait() {
        super("sentry");
        plugin = (Sentry) Bukkit.getServer().getPluginManager().getPlugin("Sentry");
    }
    @SuppressWarnings("unchecked")
    @Override
    public void load(DataKey key) throws NPCLoadException {
        plugin.debug(npc.getName() + " Load");
        ensureInst();

        if (key.keyExists("traits")) { key = key.getRelative("traits"); }

        isToggled = key.getBoolean("toggled", isToggled());
        thisInstance
            .setRetaliate(key.getBoolean("Retaliate", plugin.getConfig().getBoolean("DefaultOptions.Retaliate", true)));
        thisInstance.setInvincible(
            key.getBoolean("Invincinble", plugin.getConfig().getBoolean("DefaultOptions.Invincible", false)));
        thisInstance.setDropInventory(
            key.getBoolean("DropInventory", plugin.getConfig().getBoolean("DefaultOptions.Drops", false)));
        thisInstance.setLuckyHits(
            key.getBoolean("CriticalHits", plugin.getConfig().getBoolean("DefaultOptions.Criticals", true)));
        thisInstance.setSentryHealth(key.getDouble("Health", plugin.getConfig().getInt("DefaultStats.Health", 20)));
        thisInstance.setSentryRange(key.getInt("Range", plugin.getConfig().getInt("DefaultStats.Range", 10)));
        thisInstance
            .setRespawnDelaySeconds(key.getInt("RespawnDelay", plugin.getConfig().getInt("DefaultStats.Respawn", 10)));
        thisInstance
            .setSentrySpeed((float) (key.getDouble("Speed", plugin.getConfig().getDouble("DefaultStats.Speed", 1.0))));
        thisInstance.setSentryWeight(key.getDouble("Weight", plugin.getConfig().getDouble("DefaultStats.Weight", 1.0)));
        thisInstance.setArmor(key.getInt("Armor", plugin.getConfig().getInt("DefaultStats.Armor", 0)));
        thisInstance.setStrength(key.getInt("Strength", plugin.getConfig().getInt("DefaultStats.Strength", 1)));
        thisInstance.setFollowDistance(
            key.getInt("FollowDistance", plugin.getConfig().getInt("DefaultStats.FollowDistance", 4)));
        thisInstance.setGuardTarget(key.getString("GuardTarget", null));
        thisInstance.setGreetingMessage(key.getString("Greeting", plugin.getConfig().getString("DefaultTexts.Greeting",
                                                                                               "'" +
                                                                                               ChatColor.COLOR_CHAR +
                                                                                               "b<NPC> says Welcome, <PLAYER>'")));
        thisInstance.setWarningMessage(key.getString("Warning", plugin.getConfig().getString("DefaultTexts.Warning",
                                                                                             "'" +
                                                                                             ChatColor.COLOR_CHAR +
                                                                                             "c<NPC> says Halt! Come no closer!'")));
        thisInstance
            .setWarningRange(key.getInt("WarningRange", plugin.getConfig().getInt("DefaultStats.WarningRange", 0)));
        thisInstance.setAttackRateSeconds(
            key.getDouble("AttackRate", plugin.getConfig().getDouble("DefaultStats.AttackRate", 2.0)));
        thisInstance.setHealRate(key.getDouble("HealRate", plugin.getConfig().getDouble("DefaultStats.HealRate", 0.0)));
        thisInstance
            .setNightVision(key.getInt("NightVision", plugin.getConfig().getInt("DefaultStats.NightVision", 16)));
        thisInstance.setKillsDropInventory(
            key.getBoolean("KillDrops", plugin.getConfig().getBoolean("DefaultOptions.KillDrops", true)));
        thisInstance.setIgnoreLOS(
            key.getBoolean("IgnoreLOS", plugin.getConfig().getBoolean("DefaultOptions.IgnoreLOS", false)));
        thisInstance.setMountID(key.getInt("MountID", -1));
        thisInstance.setTargetable(
            key.getBoolean("Targetable", plugin.getConfig().getBoolean("DefaultOptions.Targetable", true)));

        if (key.keyExists("getSpawn()")) {
            try {
                thisInstance.setSpawn(new Location(plugin.getServer().getWorld(key.getString("getSpawn().world")),
                                                   key.getDouble("getSpawn().x"), key.getDouble("getSpawn().y"),
                                                   key.getDouble("getSpawn().z"),
                                                   (float) key.getDouble("getSpawn().yaw"),
                                                   (float) key.getDouble("getSpawn().pitch")));
            } catch (Exception e) {
                e.printStackTrace();
                thisInstance.setSpawn(null);
            }

            if (thisInstance.getSpawn().getWorld() == null) { thisInstance.setSpawn(null); }
        }

        if (thisInstance.getGuardEntity() != null && thisInstance.getGuardEntity().isEmpty()) {
            thisInstance.setGuardEntity(null);
        }

        List<String> targetTemp;
        List<String> ignoreTemp;

        Object targets = key.getRaw("Targets");
        if (targets != null) { targetTemp = (List<String>) key.getRaw("Targets"); }
        else { targetTemp = plugin.getConfig().getStringList("DefaultTargets"); }

        Object ignores = key.getRaw("Ignores");
        if (ignores != null) { ignoreTemp = (List<String>) key.getRaw("Ignores"); }
        else { ignoreTemp = plugin.getConfig().getStringList("DefaultIgnores"); }

        for (String string : targetTemp) {
            if (!thisInstance.getValidTargets().contains(string.toUpperCase())) {
                thisInstance.getValidTargets().add(string.toUpperCase());
            }
        }

        for (String string : ignoreTemp) {
            if (!thisInstance.getIgnoreTargets().contains(string.toUpperCase())) {
                thisInstance.getIgnoreTargets().add(string.toUpperCase());
            }

        }

        thisInstance.loaded = true;

        thisInstance.processTargets();

    }
    @Override
    public void onAttach() {
        plugin.debug(npc.getName() + ":" + npc.getId() + " onAttach");
        isToggled = true;
    }
    @Override
    public void onCopy() {
        plugin.debug(npc.getName() + ":" + npc.getId() + " onCopy");
        if (thisInstance != null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    thisInstance.setSpawn(npc.getEntity().getLocation().clone());
                }
            }, 10);
        }
    }
    @Override
    public void onDespawn() {
        plugin.debug(npc.getName() + ":" + npc.getId() + " onDespawn");
        if (thisInstance != null) {
            thisInstance.isRespawnable = System.currentTimeMillis() + thisInstance.getRespawnDelaySeconds() * 1000;
            thisInstance.setSentryStatus(Status.DEAD);
            thisInstance.dismount();
        }
    }
    @Override
    public void onRemove() {

        //	plugin = (Sentry) Bukkit.getPluginManager().getPlugin("Sentry");

        if (thisInstance != null) {
            //	plugin.getServer().broadcastMessage("onRemove");
            thisInstance.cancelRunnable();
        }

        plugin.debug(npc.getName() + " onRemove");

        thisInstance = null;
        isToggled = false;
    }
    @Override
    public void onSpawn() {
        plugin.debug(npc.getName() + ":" + npc.getId() + " getSpawn()");
        ensureInst();

        if (!thisInstance.loaded) {
            try {
                plugin.debug(npc.getName() + " getSpawn() call load");
                load(new net.citizensnpcs.api.util.MemoryDataKey());
            } catch (NPCLoadException e) {
                plugin.debug(npc.getName() + " getSpawn() failed to load");
            }
        }

        if (!plugin.GroupsChecked) {
            plugin.doGroups(); // lazy checking for lazy vault.
        }

        thisInstance.initialize();

    }
    @Override
    public void save(DataKey key) {
        if (thisInstance == null) { return; }
        key.setBoolean("toggled", isToggled);
        key.setBoolean("Retaliate", thisInstance.isRetaliate());
        key.setBoolean("Invincinble", thisInstance.isInvincible());
        key.setBoolean("DropInventory", thisInstance.isDropInventory());
        key.setBoolean("KillDrops", thisInstance.doesKillsDropInventory());
        key.setBoolean("Targetable", thisInstance.isTargetable());

        key.setInt("MountID", thisInstance.getMountID());

        key.setBoolean("CriticalHits", thisInstance.isLuckyHits());
        key.setBoolean("IgnoreLOS", thisInstance.isIgnoreLOS());
        key.setRaw("Targets", thisInstance.getValidTargets());
        key.setRaw("Ignores", thisInstance.getIgnoreTargets());

        if (thisInstance.getSpawn() != null) {
            key.setDouble("getSpawn().x", thisInstance.getSpawn().getX());
            key.setDouble("getSpawn().y", thisInstance.getSpawn().getY());
            key.setDouble("getSpawn().z", thisInstance.getSpawn().getZ());
            key.setString("getSpawn().world", thisInstance.getSpawn().getWorld().getName());
            key.setDouble("getSpawn().yaw", thisInstance.getSpawn().getYaw());
            key.setDouble("getSpawn().pitch", thisInstance.getSpawn().getPitch());
        }

        key.setDouble("Health", thisInstance.getSentryHealth());
        key.setInt("Range", thisInstance.getSentryRange());
        key.setInt("RespawnDelay", thisInstance.getRespawnDelaySeconds());
        key.setDouble("Speed", thisInstance.getSentrySpeed());
        key.setDouble("Weight", thisInstance.getSentryWeight());
        key.setDouble("HealRate", thisInstance.getHealRate());
        key.setInt("Armor", thisInstance.getArmor());
        key.setInt("Strength", thisInstance.getStrength());
        key.setInt("WarningRange", thisInstance.getWarningRange());
        key.setDouble("AttackRate", thisInstance.getAttackRateSeconds());
        key.setInt("NightVision", thisInstance.getNightVision());
        key.setInt("FollowDistance", thisInstance.getFollowDistance());

        if (thisInstance.getGuardTarget() != null) { key.setString("GuardTarget", thisInstance.getGuardTarget()); }
        else if (key.keyExists("GuardTarget")) { key.removeKey("GuardTarget"); }

        key.setString("Warning", thisInstance.getWarningMessage());
        key.setString("Greeting", thisInstance.getGreetingMessage());
    }
    public SentryInstance getInstance() {
        return thisInstance;
    }
    private void ensureInst() {
        if (thisInstance == null) {
            thisInstance = new SentryInstance(plugin);
            thisInstance.myNPC = npc;
            thisInstance.myTrait = this;
        }
    }
    @EventHandler
    public void onCitReload(CitizensReloadEvent event) {
        if (thisInstance != null) {
            thisInstance.cancelRunnable();
        }
        thisInstance = null;
        isToggled = false;
    }
    @Override
    public boolean toggle() {
        isToggled = !isToggled;
        return isToggled;
    }

    public boolean isToggled() {
        return isToggled;
    }

}
