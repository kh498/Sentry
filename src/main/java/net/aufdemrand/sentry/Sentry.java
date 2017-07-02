package net.aufdemrand.sentry;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.logging.Level;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class Sentry extends JavaPlugin {

    public static Sentry instance;
    //FactionsSupport
    static boolean FactionsActive;
    private static String LOG_PREFIX = "";
    public final List<Material> Helmets = new LinkedList<Material>() {{
        add(Material.LEATHER_HELMET);
        add(Material.CHAINMAIL_HELMET);
        add(Material.IRON_HELMET);
        add(Material.DIAMOND_HELMET);
        add(Material.GOLD_HELMET);
        add(Material.JACK_O_LANTERN);
        add(Material.PUMPKIN);
    }};
    public final List<Material> Chestplates = new LinkedList<Material>() {{
        add(Material.LEATHER_CHESTPLATE);
        add(Material.CHAINMAIL_CHESTPLATE);
        add(Material.IRON_CHESTPLATE);
        add(Material.DIAMOND_CHESTPLATE);
        add(Material.GOLD_CHESTPLATE);
    }};
    public final List<Material> Leggings = new LinkedList<Material>() {{
        add(Material.LEATHER_LEGGINGS);
        add(Material.CHAINMAIL_LEGGINGS);
        add(Material.IRON_LEGGINGS);
        add(Material.DIAMOND_LEGGINGS);
        add(Material.GOLD_LEGGINGS);
    }};
    public final List<Material> Boots = new LinkedList<Material>() {{
        add(Material.LEATHER_BOOTS);
        add(Material.CHAINMAIL_BOOTS);
        add(Material.IRON_BOOTS);
        add(Material.DIAMOND_BOOTS);
        add(Material.GOLD_BOOTS);
    }};
    public final Map<Material, List<PotionEffect>> WeaponEffects = new EnumMap<>(Material.class);
    public final Map<Material, Double> SpeedBuffs = new EnumMap<>(Material.class);
    public final Map<Material, Double> StrengthBuffs = new EnumMap<>(Material.class);
    public final Map<Material, Double> ArmorBuffs = new EnumMap<>(Material.class);
    public final Queue<Projectile> arrows = new LinkedList<>();
    public int sentryEXP = 5;
    public int logicTicks = 10;
    public int critical1Chance;
    public int critical2Chance;
    public int critical3Chance;
    public int missChance;
    public int glanceChance;
    public String critical1Message = "";
    public String criticalMessage = "";
    public String critical3Message = "";
    public String glanceMessage = "";
    public String hitMessage = "";
    public String missMessage = "";
    public String blockMessage = "";
    public boolean bodyguardsObeyProtection = true;
    public boolean ignoreListInvincibility = true;
    public boolean groupsChecked;
    public Permission perms;
    public Material archer;
    public Material pyro1;
    public Material pyro2;
    public Material pyro3;
    public Material sc1;
    public Material sc2;
    public Material sc3;
    public Material warlock1;
    public Material warlock2;
    public Material warlock3;
    public Material witchdoctor;
    public Material magi;
    public Material bombardier;
    public boolean DieLikePlayers;
    public boolean debug;
    public static Sentry getInstance() {
        return instance;
    }

    private boolean checkPlugin(final String name) {
        if (getServer().getPluginManager().getPlugin(name) != null) {
            if (getServer().getPluginManager().getPlugin(name).isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void debug(final String s) {
        if (this.debug) { this.getServer().getLogger().info(LOG_PREFIX + s); }
    }

    public void debug(final NPC npc, final String s) {
        debug(npc.getFullName() + ":" + npc.getId() + " " + s);
    }

    public void doGroups() {
        if (!setupPermissions()) {
            getLogger().log(Level.WARNING, "Could not register with Vault! the GROUP target will not function.");
        }
        else {
            try {
                final String[] Gr = this.perms.getGroups();
                if (Gr.length == 0) {
                    getLogger().log(Level.WARNING, "No permission groups found. the GROUP target will not function.");
                    this.perms = null;
                }
                else {
                    getLogger().log(Level.INFO, "Registered successfully with Vault: " + Gr.length +
                                                " groups found. The GROUP: target will function");
                }

            } catch (final Exception e) {
                getLogger().log(Level.WARNING, "Error getting groups. the GROUP target will not function.");
                this.perms = null;
            }
        }

        this.groupsChecked = true;

    }
    /**
     * Equip a npc an item
     * <p>
     * If the item is a piece of armour the npc will equip it at the correct slot
     *
     * @param npc      The NPC to equip
     * @param material the material of the item
     *
     * @return true if the item was equipped successfully
     */
    public boolean equip(final NPC npc, final Material material) {
        return equip(npc, material, 1);
    }
    /**
     * Equip a npc an item
     * <p>
     * If the item is a piece of armour the npc will equip it at the correct slot
     *
     * @param npc      The NPC to equip
     * @param material the material of the item
     * @param amount   the amount of material
     *
     * @return true if the item was equipped successfully
     */
    @SuppressWarnings("SameParameterValue")
    public boolean equip(final NPC npc, final Material material, final int amount) {
        return equip(npc, new ItemStack(material, amount));
    }
    /**
     * Equip a npc an item
     * <p>
     * If the item is a piece of armour the npc will equip it at the correct slot
     *
     * @param npc  The NPC to equip
     * @param item The itemstack to equip
     *
     * @return true if the item was equipped successfully
     */
    public boolean equip(final NPC npc, final ItemStack item) {
        final Equipment trait = npc.getTrait(Equipment.class);
        if (trait == null) { return false; }
        int slot = 0;
        final Material type = item == null ? Material.AIR : item.getType();
        // First, determine the slot to edit

        if (this.Helmets.contains(type)) { slot = 1; }
        else if (this.Chestplates.contains(type)) { slot = 2; }
        else if (this.Leggings.contains(type)) { slot = 3; }
        else if (this.Boots.contains(type)) { slot = 4; }

        // Now edit the equipment based on the slot
        // Set the proper slot with one of the item

        if (type == Material.AIR) {
            for (int i = 0; i < 5; i++) {
                if (trait.get(i) != null && trait.get(i).getType() != Material.AIR) {
                    try {
                        trait.set(i, null);
                    } catch (final Exception e) {
                        //
                    }
                }
            }
            return true;
        }
        else {
            final ItemStack clone = item.clone();
            clone.setAmount(1);

            try {
                trait.set(slot, clone);
            } catch (final Exception e) {
                return false;
            }
            return true;
        }

    }

    private static Material getMaterial(final String str) {
        return str == null ? null : Material.getMaterial(str.toUpperCase());
    }

    private static PotionEffect getPotion(final String S) {
        if (S == null) { return null; }
        final String[] args = S.trim().split(":");

        final PotionEffectType type;

        int dur = 10;
        int amp = 1;

        type = PotionEffectType.getByName((args[0]));

        if (type == null) { return null; }

        if (args.length > 1) {
            try {
                dur = Integer.parseInt(args[1]);
            } catch (final Exception e) {
                //
            }
        }

        if (args.length > 2) {
            try {
                amp = Integer.parseInt(args[2]);
            } catch (final Exception e) {
                //
            }
        }

        return new PotionEffect(type, dur, amp);
    }

    public static SentryInstance getSentry(final Entity ent) {
        if (ent == null) { return null; }
        if (!(ent instanceof org.bukkit.entity.LivingEntity)) { return null; }
        final NPC npc = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(ent);
        if (npc != null && npc.hasTrait(SentryTrait.class)) {
            return npc.getTrait(SentryTrait.class).getInstance();
        }

        return null;
    }

    public static SentryInstance getSentry(final NPC npc) {
        if (npc != null && npc.hasTrait(SentryTrait.class)) {
            return npc.getTrait(SentryTrait.class).getInstance();
        }
        return null;
    }

    public String getMCTeamName(final Player player) {
        @SuppressWarnings("deprecation") final Team t =
            getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if (t != null) {
            return t.getName();
        }
        return null;
    }

    public void loadItemList(final String key, final List<Material> list) {
        final List<String> stringList = getConfig().getStringList(key);

        if (stringList.size() > 0) { list.clear(); }

        for (final String s : getConfig().getStringList(key)) {
            list.add(getMaterial(s.trim()));
        }

    }
    private void loadMap(final String node, final Map<Material, Double> map) {
        map.clear();
        for (final String s : getConfig().getStringList(node)) {
            final String[] args = s.trim().split(" ");
            if (args.length != 2) { continue; }

            double val = 0;

            try {
                val = Double.parseDouble(args[1]);
            } catch (final Exception e) {
                //
            }

            final Material item = getMaterial(args[0]);

            if (item != null && val != 0 && !map.containsKey(item)) {
                map.put(item, val);
            }
        }
    }
    private void loadPotions(final Map<Material, List<PotionEffect>> map) {
        map.clear();
        for (final String s : getConfig().getStringList("WeaponEffects")) {
            final String[] args = s.trim().split(" ");

            if (args.length < 2) { continue; }

            final Material item = getMaterial(args[0]);

            final List<PotionEffect> list = new ArrayList<>();

            for (int i = 1; i < args.length; i++) {
                final PotionEffect val = getPotion(args[i]);
                if (val != null) { list.add(val); }

            }

            if (item != null && !list.isEmpty()) { map.put(item, list); }

        }
    }
    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String cmdLabel,
                             final String[] inargs) {

        if (inargs.length < 1) {
            sender.sendMessage(ChatColor.RED + "Use /sentry help for command reference.");
            return true;
        }

        int npcID = -1;
        int i = 0;

        //did player specify a id?
        if (tryParseInt(inargs[0])) {
            npcID = Integer.parseInt(inargs[0]);
            i = 1;
        }

        final String[] args = new String[inargs.length - i];

        System.arraycopy(inargs, i, args, 0, inargs.length - i);

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Use /sentry help for command reference.");
            return true;
        }

        Boolean set = null;
        if (args.length == 2) {
            if ("true".equalsIgnoreCase(args[1])) { set = true; }
            else if ("false".equalsIgnoreCase(args[1])) { set = false; }
        }

        if ("help".equalsIgnoreCase(args[0])) {

            sender.sendMessage(ChatColor.GOLD + "------- Sentry Commands -------");
            sender.sendMessage(ChatColor.GOLD +
                               "You can use /sentry (id) [command] [args] to perform any of these commands on a sentry without having it selected.");
            sender.sendMessage(ChatColor.GOLD + "");
            sender.sendMessage(ChatColor.GOLD + "/sentry reload");
            sender.sendMessage(ChatColor.YELLOW + " reload the config.yml");
            sender.sendMessage(ChatColor.GOLD + "/sentry target [add|remove] [target]");
            sender.sendMessage(ChatColor.YELLOW + " Adds or removes a target to attack.");
            sender.sendMessage(ChatColor.GOLD + "/sentry target [list|clear]");
            sender.sendMessage(ChatColor.YELLOW + " View or clear the target list..");
            sender.sendMessage(ChatColor.GOLD + "/sentry ignore [add|remove] [target]");
            sender.sendMessage(ChatColor.YELLOW + " Adds or removes a target to ignore.");
            sender.sendMessage(ChatColor.GOLD + "/sentry ignore [list|clear]");
            sender.sendMessage(ChatColor.YELLOW + " View or clear the ignore list..");
            sender.sendMessage(ChatColor.GOLD + "/sentry info");
            sender.sendMessage(ChatColor.YELLOW + " View all Sentry attributes");
            sender.sendMessage(ChatColor.GOLD + "/sentry equip [item|none]");
            sender.sendMessage(ChatColor.YELLOW + " Equip an item on the Sentry, or remove all equipment.");
            sender.sendMessage(ChatColor.GOLD + "/sentry speed [0-1.5]");
            sender.sendMessage(ChatColor.YELLOW + " Sets speed of the Sentry when attacking.");
            sender.sendMessage(ChatColor.GOLD + "/sentry health [1-2000000]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Health .");
            sender.sendMessage(ChatColor.GOLD + "/sentry armor [0-2000000]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Armor.");
            sender.sendMessage(ChatColor.GOLD + "/sentry strength [0-2000000]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Strength.");
            sender.sendMessage(ChatColor.GOLD + "/sentry attackrate [0.0-30.0]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the time between the Sentry's projectile attacks.");
            sender.sendMessage(ChatColor.GOLD + "/sentry healrate [0.0-300.0]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the frequency the sentry will heal 1 point. 0 to disable.");
            sender.sendMessage(ChatColor.GOLD + "/sentry range [1-100]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the Sentry's detection range.");
            sender.sendMessage(ChatColor.GOLD + "/sentry warningrange [0-50]");
            sender.sendMessage(
                ChatColor.YELLOW + " Sets the range, beyond the detection range, that the Sentry will warn targets.");
            sender.sendMessage(ChatColor.GOLD + "/sentry respawn [-1-2000000]");
            sender.sendMessage(ChatColor.YELLOW + " Sets the number of seconds after death the Sentry will respawn.");
            sender.sendMessage(ChatColor.GOLD + "/sentry follow [0-32]");
            sender
                .sendMessage(ChatColor.YELLOW + " Sets the number of block away a bodyguard will follow. Default is 4");
            sender.sendMessage(ChatColor.GOLD + "/sentry invincible");
            sender.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to take no damage or knockback.");
            sender.sendMessage(ChatColor.GOLD + "/sentry retaliate");
            sender.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to always attack an attacker.");
            sender.sendMessage(ChatColor.GOLD + "/sentry criticals");
            sender.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to take critical hits and misses");
            sender.sendMessage(ChatColor.GOLD + "/sentry drops");
            sender.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to drop equipped items on death");
            sender.sendMessage(ChatColor.GOLD + "/sentry killdrops");
            sender.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry's victims drop items and exp");
            sender.sendMessage(ChatColor.GOLD + "/sentry mount");
            sender.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry rides a mount");
            sender.sendMessage(ChatColor.GOLD + "/sentry targetable");
            sender.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry is attacked by hostile mobs");
            sender.sendMessage(ChatColor.GOLD + "/sentry spawn");
            sender.sendMessage(ChatColor.YELLOW + " Set the sentry to respawn at its current location");
            sender.sendMessage(ChatColor.GOLD + "/sentry warning 'The Test to use'");
            sender.sendMessage(
                ChatColor.YELLOW + " Change the warning text. <NPC> and <PLAYER> can be used as placeholders");
            sender.sendMessage(ChatColor.GOLD + "/sentry greeting 'The text to use'");
            sender.sendMessage(
                ChatColor.YELLOW + " Change the greeting text. <NPC> and <PLAYER> can be used as placeholders");
            return true;
        }
        else if ("debug".equalsIgnoreCase(args[0])) {

            this.debug = !this.debug;

            sender.sendMessage(ChatColor.GREEN + "Debug now: " + this.debug);
            return true;
        }
        else if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            this.reloadPluginConfig();
            sender.sendMessage(ChatColor.GREEN + "reloaded Sentry/config.yml");
            return true;
        }
        NPC ThisNPC;

        if (npcID == -1) {

            ThisNPC = ((Citizens) this.getServer().getPluginManager().getPlugin("Citizens")).getNPCSelector()
                                                                                            .getSelected(sender);

            if (ThisNPC != null) {
                // Gets NPC Selected
                npcID = ThisNPC.getId();
            }

            else {
                sender.sendMessage(ChatColor.RED + "You must have a NPC selected to use this command");
                return true;
            }
        }

        ThisNPC = CitizensAPI.getNPCRegistry().getById(npcID);

        if (ThisNPC == null) {
            sender.sendMessage(ChatColor.RED + "NPC with id " + npcID + " not found");
            return true;
        }

        if (!ThisNPC.hasTrait(SentryTrait.class)) {
            sender.sendMessage(ChatColor.RED + "That command must be performed on a Sentry!");
            return true;
        }

        if (sender instanceof Player && !CitizensAPI.getNPCRegistry().isNPC((Entity) sender)) {

            if (!ThisNPC.getTrait(Owner.class).getOwner().equalsIgnoreCase(sender.getName())) {
                //not player is owner
                if (!sender.hasPermission("citizens.admin")) {
                    //no c2 admin.
                    sender.sendMessage(ChatColor.RED + "You must be the owner of this Sentry to execute commands.");
                    return true;
                }
                else {
                    //has citizens.admin
                    if (!"server".equalsIgnoreCase(ThisNPC.getTrait(Owner.class).getOwner())) {
                        //not server-owned NPC
                        sender.sendMessage(ChatColor.RED +
                                           "You, or the server, must be the owner of this Sentry to execute commands.");
                        return true;
                    }
                }
            }
        }

        // Commands

        final SentryInstance inst = ThisNPC.getTrait(SentryTrait.class).getInstance();

        if ("spawn".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.spawn")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (ThisNPC.getEntity() == null) {
                sender.sendMessage(ChatColor.RED + "Cannot set spawn while " + ThisNPC.getName() + " is dead.");
                return true;
            }
            inst.setSpawn(ThisNPC.getEntity().getLocation());
            sender.sendMessage(
                ChatColor.GREEN + ThisNPC.getName() + " will respawn at its present location."); // Talk to the player.
            return true;
        }

        else if ("invincible".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.invincible")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setInvincible(set == null ? !inst.isInvincible() : set);

            if (!inst.isInvincible()) {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " now takes damage.."); // Talk to the player.
            }
            else {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " now INVINCIBLE."); // Talk to the player.
            }

            return true;
        }
        else if ("retaliate".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.retaliate")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setRetaliate(set == null ? !inst.isRetaliate() : set);

            if (!inst.isRetaliate()) {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will not retaliate."); // Talk to the player.
            }
            else {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                   " will retaliate against all attackers."); // Talk to the player.
            }

            return true;
        }
        else if ("criticals".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.criticals")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setLuckyHits(set == null ? !inst.isLuckyHits() : set);

            if (!inst.isLuckyHits()) {
                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will take normal damage."); // Talk to the player.
            }
            else {
                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will take critical hits."); // Talk to the player.
            }

            return true;
        }
        else if ("drops".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.drops")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setDropInventory(set == null ? !inst.isDropInventory() : set);

            if (inst.isDropInventory()) {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will drop items"); // Talk to the player.
            }
            else {
                sender
                    .sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will not drop items."); // Talk to the player.
            }

            return true;
        }
        else if ("killdrops".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.killdrops")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setKillsDropInventory(set == null ? !inst.doesKillsDropInventory() : set);

            if (inst.doesKillsDropInventory()) {
                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + "'s kills will drop items or exp"); // Talk to the player.
            }
            else {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                   "'s kills will not drop items or exp."); // Talk to the player.
            }

            return true;
        }
        else if ("targetable".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.targetable")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setTargetable(set == null ? !inst.isTargetable() : set);
            ThisNPC.data().set(NPC.TARGETABLE_METADATA, inst.isTargetable());

            if (inst.isTargetable()) {
                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will be targeted by mobs"); // Talk to the player.
            }
            else {
                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will not be targeted by mobs"); // Talk to the player.
            }

            return true;
        }
        else if ("mount".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.options.mount")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            set = set == null ? !inst.isMounted() : set;

            if (set) {
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " is now Mounted"); // Talk to the player.
                inst.createMount();
                inst.mount();
            }
            else {
                sender
                    .sendMessage(ChatColor.GREEN + ThisNPC.getName() + " is no longer Mounted"); // Talk to the player.
                if (inst.isMounted()) { Util.removeMount(inst.getMountID()); }
                inst.setMountID(-1);
            }

            return true;
        }
        else if ("guard".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.guard")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            boolean localOnly = false;
            boolean playersOnly = false;
            int start = 1;

            if (args.length > 1) {

                if ("-p".equalsIgnoreCase(args[1])) {
                    start = 2;
                    playersOnly = true;
                }

                if ("-l".equalsIgnoreCase(args[1])) {
                    start = 2;
                    localOnly = true;
                }

                StringBuilder arg = new StringBuilder();
                for (i = start; i < args.length; i++) {
                    arg.append(" ").append(args[i]);
                }
                arg = new StringBuilder(arg.toString().trim());

                @SuppressWarnings("deprecation") final LivingEntity et = Bukkit.getPlayer(arg.toString());

                boolean ok = false;

                if (!playersOnly) {
                    ok = inst.setGuardTarget(et, false);
                }

                if (!localOnly) {
                    ok = inst.setGuardTarget(et, true);
                }

                if (ok) {
                    sender.sendMessage(
                        ChatColor.GREEN + ThisNPC.getName() + " is now guarding " + arg); // Talk to the player.
                }
                else {
                    sender.sendMessage(
                        ChatColor.RED + ThisNPC.getName() + " could not find " + arg + "."); // Talk to the player.
                }

            }

            else {
                if (inst.getGuardTarget() == null) {
                    sender.sendMessage(ChatColor.RED + ThisNPC.getName() +
                                       " is already set to guard its immediate area"); // Talk to the player.
                }
                else {
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                       " is now guarding its immediate area. "); // Talk to the player.
                }
                inst.setGuardTarget(null, false);

            }
            return true;
        }

        else if ("follow".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.follow")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(
                    ChatColor.GOLD + ThisNPC.getName() + "'s Follow Distance is " + inst.getFollowDistance());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry follow [#]. Default is 4. ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 32) { HPs = 32; }
                if (HPs < 0) { HPs = 0; }

                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " follow distance set to " + HPs +
                                   "."); // Talk to the player.
                inst.setFollowDistance(HPs * HPs);

            }

            return true;
        }

        else if ("health".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.health")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Health is " + inst.getSentryHealth());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry health [#]  note: Typically players");
                sender.sendMessage(ChatColor.GOLD + " have 20 HPs when fully healed");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 1) { HPs = 1; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " health set to " + HPs + "."); // Talk to the player.
                inst.setSentryHealth(HPs);
                inst.setHealth(HPs);
            }

            return true;
        }

        else if ("armor".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.armor")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Armor is " + inst.getArmor());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry armor [#] ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 0) { HPs = 0; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " armor set to " + HPs + "."); // Talk to the player.
                inst.setArmor(HPs);

            }

            return true;
        }
        else if ("strength".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.strength")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Strength is " + inst.getStrength());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry strength # ");
                sender.sendMessage(ChatColor.GOLD + "Note: At Strength 0 the Sentry will do no damage.");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 0) { HPs = 0; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " strength set to " + HPs + "."); // Talk to the player.
                inst.setStrength(HPs);

            }

            return true;
        }
        else if ("nightvision".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.nightvision")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Night Vision is " + inst.getNightVision());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry nightvision [0-16] ");
                sender.sendMessage(ChatColor.GOLD + "Usage: 0 = See nothing, 16 = See everything. ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 16) { HPs = 16; }
                if (HPs < 0) { HPs = 0; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " Night Vision set to " + HPs + "."); // Talk to the player.
                inst.setNightVision(HPs);

            }

            return true;
        }

        else if ("respawn".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.respawn")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                if (inst.getRespawnDelaySeconds() == 0) {
                    sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + " will not automatically respawn.");
                }
                if (inst.getRespawnDelaySeconds() == -1) {
                    sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + " will be deleted upon death");
                }
                if (inst.getRespawnDelaySeconds() > 0) {
                    sender.sendMessage(
                        ChatColor.GOLD + ThisNPC.getName() + " respawns after " + inst.getRespawnDelaySeconds() + "s");
                }

                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry respawn [-1 - 2000000] ");
                sender.sendMessage(ChatColor.GOLD + "Usage: set to 0 to prevent automatic respawn");
                sender.sendMessage(ChatColor.GOLD + "Usage: set to -1 to *permanently* delete the Sentry on death.");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < -1) { HPs = -1; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " now respawns after " + HPs + "s."); // Talk to the player.
                inst.setRespawnDelaySeconds(HPs);

            }
            return true;
        }

        else if ("speed".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.speed")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Speed is " + inst.getSentrySpeed());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry speed [0.0 - 2.0]");
            }
            else {

                Float HPs = Float.valueOf(args[1]);
                if (HPs > 2.0) { HPs = 2.0f; }
                if (HPs < 0.0) { HPs = 0f; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " speed set to " + HPs + "."); // Talk to the player.
                inst.setSentrySpeed(HPs);

            }

            return true;
        }
        else if ("attackrate".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.attackrate")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(
                    ChatColor.GOLD + ThisNPC.getName() + "'s Projectile Attack Rate is " + inst.getAttackRateSeconds() +
                    "s between shots.");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry attackrate [0.0 - 30.0]");
            }
            else {

                Double HPs = Double.valueOf(args[1]);
                if (HPs > 30.0) { HPs = 30.0; }
                if (HPs < 0.0) { HPs = 0.0; }

                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Projectile Attack Rate set to " + HPs +
                                   "."); // Talk to the player.
                inst.setAttackRateSeconds(HPs);
            }

            return true;
        }
        else if ("healrate".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.healrate")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Heal Rate is " + inst.getHealRate() + "s");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry healrate [0.0 - 300.0]");
                sender.sendMessage(ChatColor.GOLD + "Usage: Set to 0 to disable healing");
            }
            else {

                Double HPs = Double.valueOf(args[1]);
                if (HPs > 300.0) { HPs = 300.0; }
                if (HPs < 0.0) { HPs = 0.0; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " Heal Rate set to " + HPs + "."); // Talk to the player.
                inst.setHealRate(HPs);

            }

            return true;
        }
        else if ("range".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.range")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Range is " + inst.getSentryRange());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry range [1 - 100]");
            }

            else {

                Integer HPs = Integer.valueOf(args[1]);
                if (HPs > 100) { HPs = 100; }
                if (HPs < 1) { HPs = 1; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " range set to " + HPs + "."); // Talk to the player.
                inst.setSentryRange(HPs);

            }

            return true;
        }
        else if ("warningrange".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.stats.warningrange")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                sender
                    .sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Warning Range is " + inst.getWarningRange());
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry warningrange [0 - 50]");
            }

            else {

                Integer HPs = Integer.valueOf(args[1]);
                if (HPs > 50) { HPs = 50; }
                if (HPs < 0) { HPs = 0; }

                sender.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " warning range set to " + HPs + "."); // Talk to the player.
                inst.setWarningRange(HPs);

            }

            return true;
        }
        else if ("equip".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.equip")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                sender.sendMessage(
                    ChatColor.RED + "You must specify a Item ID or Name. or specify 'none' to remove all equipment.");
            }

            else {

                if (ThisNPC.getEntity().getType() == org.bukkit.entity.EntityType.ENDERMAN ||
                    ThisNPC.getEntity().getType() == org.bukkit.entity.EntityType.PLAYER) {
                    if ("none".equalsIgnoreCase(args[1])) {
                        //remove equipment
                        equip(ThisNPC, Material.AIR);
                        inst.UpdateWeapon();
                        sender.sendMessage(ChatColor.YELLOW + ThisNPC.getName() + "'s equipment cleared.");
                    }
                    else {
                        final Material mat = getMaterial(args[1]);
                        if (mat != null) {
                            final ItemStack is = new ItemStack(mat);
                            if (equip(ThisNPC, is)) {
                                inst.UpdateWeapon();
                                sender.sendMessage(ChatColor.GREEN + " equipped " + is.getType().toString() + " on " +
                                                   ThisNPC.getName());
                            }
                            else { sender.sendMessage(ChatColor.RED + " Could not equip: invalid mob type?"); }
                        }
                        else { sender.sendMessage(ChatColor.RED + " Could not equip: unknown item name"); }
                    }
                }
                else { sender.sendMessage(ChatColor.RED + " Could not equip: must be Player or Enderman type"); }
            }

            return true;
        }
        else if ("warning".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.warning")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length >= 2) {
                StringBuilder arg = new StringBuilder();
                for (i = 1; i < args.length; i++) {
                    arg.append(" ").append(args[i]);
                }
                arg = new StringBuilder(arg.toString().trim());

                final String str = arg.toString().replaceAll("\"$", "").replaceAll("^\"", "").replaceAll("'$", "")
                                      .replaceAll("^'", "");
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " warning message set to " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', str) + "."); // Talk to the player.
                inst.setWarningMessage(str);
            }
            else {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Warning Message is: " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', inst.getWarningMessage()));
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry warning 'The Text to use'");
            }
            return true;
        }
        else if ("greeting".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.greeting")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length >= 2) {

                StringBuilder arg = new StringBuilder();
                for (i = 1; i < args.length; i++) {
                    arg.append(" ").append(args[i]);
                }
                arg = new StringBuilder(arg.toString().trim());

                final String str = arg.toString().replaceAll("\"$", "").replaceAll("^\"", "").replaceAll("'$", "")
                                      .replaceAll("^'", "");
                sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Greeting message set to " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', str) + "."); // Talk to the player.
                inst.setGreetingMessage(str);
            }
            else {
                sender.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Greeting Message is: " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', inst.getGreetingMessage()));
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry greeting 'The Text to use'");
            }
            return true;
        }

        else if ("info".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.info")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            sender.sendMessage(
                ChatColor.GOLD + "------- Sentry Info for (" + ThisNPC.getId() + ") " + ThisNPC.getName() + "------");
            sender.sendMessage(ChatColor.GREEN + inst.getStats());
            sender.sendMessage(
                ChatColor.GREEN + "Invincible: " + inst.isInvincible() + " Retaliate: " + inst.isRetaliate());
            sender.sendMessage(
                ChatColor.GREEN + "Drops Items: " + inst.isDropInventory() + " Critical Hits: " + inst.isLuckyHits());
            sender.sendMessage(
                ChatColor.GREEN + "Kills Drop Items: " + inst.doesKillsDropInventory() + " Respawn Delay: " +
                inst.getRespawnDelaySeconds() + "s");
            sender.sendMessage(ChatColor.BLUE + "Status: " + inst.getSentryStatus());
            if (inst.getMeleeTarget() == null) {
                if (inst.getProjectileTarget() == null) { sender.sendMessage(ChatColor.BLUE + "Target: Nothing"); }
                else { sender.sendMessage(ChatColor.BLUE + "Target: " + inst.getProjectileTarget().toString()); }
            }
            else { sender.sendMessage(ChatColor.BLUE + "Target: " + inst.getMeleeTarget().toString()); }

            if (inst.getGuardTarget() == null) { sender.sendMessage(ChatColor.BLUE + "Guarding: My Surroundings"); }
            else { sender.sendMessage(ChatColor.BLUE + "Guarding: " + inst.getGuardTarget()); }

            return true;
        }

        else if ("target".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.target")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.GOLD +
                                   "Usage: /sentry target add [entity:Name] or [player:Name] or [group:Name] or [entity:monster] or [entity:player]");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target remove [target]");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target clear");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target list");
                return true;
            }

            else {

                StringBuilder arg = new StringBuilder();
                for (i = 2; i < args.length; i++) {
                    arg.append(" ").append(args[i]);
                }
                arg = new StringBuilder(arg.toString().trim());

                if ("add".equals(args[1]) && arg.length() > 0 && arg.toString().split(":").length > 1) {

                    if (!inst.containsTarget(arg.toString().toUpperCase())) {
                        inst.getValidTargets().add(arg.toString().toUpperCase());
                    }
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Target added. Now targeting " +
                                       inst.getValidTargets().toString());
                    return true;
                }

                else if ("remove".equals(args[1]) && arg.length() > 0 && arg.toString().split(":").length > 1) {

                    inst.getValidTargets().remove(arg.toString().toUpperCase());
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Targets removed. Now targeting " +
                                       inst.getValidTargets().toString());
                    return true;
                }

                else if ("clear".equals(args[1])) {

                    inst.getValidTargets().clear();
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Targets cleared.");
                    return true;
                }
                else if ("list".equals(args[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Targets: " + inst.getValidTargets().toString());
                    return true;
                }

                else {
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target list");
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target clear");
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target add type:name");
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry target remove type:name");
                    sender.sendMessage(ChatColor.GOLD +
                                       "type:name can be any of the following: entity:MobName entity:monster entity:player entity:all player:PlayerName group:GroupName town:TownName nation:NationName faction:FactionName");

                    return true;
                }
            }
        }

        else if ("ignore".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("sentry.ignore")) {
                sender.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore list");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore clear");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore add type:name");
                sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore remove type:name");
                sender.sendMessage(ChatColor.GOLD +
                                   "type:name can be any of the following: entity:MobName entity:monster entity:player entity:all player:PlayerName group:GroupName town:TownName nation:NationName faction:FactionName");

                return true;
            }

            else {

                StringBuilder arg = new StringBuilder();
                for (i = 2; i < args.length; i++) {
                    arg.append(" ").append(args[i]);
                }
                arg = new StringBuilder(arg.toString().trim());

                if ("add".equals(args[1]) && arg.length() > 0 && arg.toString().split(":").length > 1) {
                    if (!inst.containsIgnore(arg.toString().toUpperCase())) {
                        inst.getIgnoreTargets().add(arg.toString().toUpperCase());
                    }
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore added. Now ignoring " +
                                       inst.getIgnoreTargets().toString());
                    return true;
                }

                else if ("remove".equals(args[1]) && arg.length() > 0 && arg.toString().split(":").length > 1) {

                    inst.getIgnoreTargets().remove(arg.toString().toUpperCase());
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore removed. Now ignoring " +
                                       inst.getIgnoreTargets().toString());
                    return true;
                }

                else if ("clear".equals(args[1])) {

                    inst.getIgnoreTargets().clear();
                    inst.processTargets();
                    inst.clearTarget();
                    sender.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore cleared.");
                    return true;
                }
                else if ("list".equals(args[1])) {

                    sender.sendMessage(ChatColor.GREEN + "Ignores: " + inst.getIgnoreTargets().toString());
                    return true;
                }

                else {

                    sender.sendMessage(ChatColor.GOLD +
                                       "Usage: /sentry ignore add [ENTITY:Name] or [PLAYER:Name] or [GROUP:Name] or [ENTITY:MONSTER]");
                    sender.sendMessage(ChatColor.GOLD +
                                       "Usage: /sentry ignore remove [ENTITY:Name] or [PLAYER:Name] or [GROUP:Name] or [ENTITY:MONSTER]");
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore clear");
                    sender.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore list");
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, " v" + getDescription().getVersion() + " disabled.");
        Bukkit.getServer().getScheduler().cancelTasks(this);

    }
    @Override
    public void onEnable() {
        LOG_PREFIX = "[" + getName() + "] [DEBUG] ";
        if (getServer().getPluginManager().getPlugin("Citizens") == null ||
            !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SentryTrait.class).withName("sentry"));

        Sentry.instance = this;

        this.getServer().getPluginManager().registerEvents(new SentryListener(this), this);

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            //Unloaded chunk arrow cleanup
            while (Sentry.this.arrows.size() > 200) {
                final Projectile projectile = Sentry.this.arrows.remove();
                if (projectile != null) {
                    projectile.remove();
                    //	x++;
                }
            }
        }, 40, 20 * 120);

        reloadPluginConfig();
    }
    private void reloadPluginConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        loadMap("ArmorBuffs", this.ArmorBuffs);
        loadMap("StrengthBuffs", this.StrengthBuffs);
        loadMap("SpeedBuffs", this.SpeedBuffs);
        loadPotions(this.WeaponEffects);
        loadItemList("Helmets", this.Helmets);
        loadItemList("Chestplates", this.Chestplates);
        loadItemList("Leggings", this.Leggings);
        loadItemList("Boots", this.Boots);
        this.archer = getMaterial(getConfig().getString("AttackTypes.Archer", null));
        this.pyro1 = getMaterial(getConfig().getString("AttackTypes.Pyro1", null));
        this.pyro2 = getMaterial(getConfig().getString("AttackTypes.Pyro2", null));
        this.pyro3 = getMaterial(getConfig().getString("AttackTypes.Pyro3", null));
        this.bombardier = getMaterial(getConfig().getString("AttackTypes.Bombardier", null));
        this.sc1 = getMaterial(getConfig().getString("AttackTypes.StormCaller1", null));
        this.sc2 = getMaterial(getConfig().getString("AttackTypes.StormCaller2", null));
        this.witchdoctor = getMaterial(getConfig().getString("AttackTypes.WitchDoctor", null));
        this.magi = getMaterial(getConfig().getString("AttackTypes.IceMagi", null));
        this.sc3 = getMaterial(getConfig().getString("AttackTypes.StormCaller3", null));
        this.warlock1 = getMaterial(getConfig().getString("AttackTypes.Warlock1", null));
        this.warlock2 = getMaterial(getConfig().getString("AttackTypes.Warlock2", null));
        this.warlock3 = getMaterial(getConfig().getString("AttackTypes.Warlock3", null));
        this.DieLikePlayers = getConfig().getBoolean("Server.DieLikePlayers", false);
        this.bodyguardsObeyProtection = getConfig().getBoolean("Server.bodyguardsObeyProtection", true);
        this.ignoreListInvincibility = getConfig().getBoolean("Server.ignoreListInvincibility", true);
        this.logicTicks = getConfig().getInt("Server.logicTicks", 10);
        this.sentryEXP = getConfig().getInt("Server.ExpValue", 5);
        this.missMessage = getConfig().getString("GlobalTexts.Miss", null);
        this.hitMessage = getConfig().getString("GlobalTexts.Hit", null);
        this.blockMessage = getConfig().getString("GlobalTexts.Block", null);
        this.critical1Message = getConfig().getString("GlobalTexts.Crit1", null);
        this.criticalMessage = getConfig().getString("GlobalTexts.Crit2", null);
        this.critical3Message = getConfig().getString("GlobalTexts.Crit3", null);
        this.glanceMessage = getConfig().getString("GlobalTexts.Glance", null);
        this.missChance = getConfig().getInt("HitChances.Miss", 0);
        this.glanceChance = getConfig().getInt("HitChances.Glance", 0);
        this.critical1Chance = getConfig().getInt("HitChances.Crit1", 0);
        this.critical2Chance = getConfig().getInt("HitChances.Crit2", 0);
        this.critical3Chance = getConfig().getInt("HitChances.Crit3", 0);

    }
    private boolean setupPermissions() {
        try {

            if (getServer().getPluginManager().getPlugin("Vault") == null ||
                !getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
                return false;
            }

            final RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(Permission.class);

            if (permissionProvider != null) {
                this.perms = permissionProvider.getProvider();
            }

            return (this.perms != null);

        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static boolean tryParseInt(final String value) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(value);
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }
}
