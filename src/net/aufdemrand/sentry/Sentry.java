package net.aufdemrand.sentry;

import com.palmergames.bukkit.towny.object.TownBlock;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class Sentry extends JavaPlugin {
    public static Sentry instance;
    //FactionsSuport
    static boolean FactionsActive = false;
    public boolean debug = false;
    public List<Material> Helmets = new LinkedList<Material>(java.util.Arrays.asList(Material.LEATHER_HELMET,
                                                                                     Material.CHAINMAIL_HELMET,
                                                                                     Material.IRON_HELMET,
                                                                                     Material.DIAMOND_HELMET,
                                                                                     Material.GOLD_HELMET,
                                                                                     Material.JACK_O_LANTERN,
                                                                                     Material.PUMPKIN));
    public List<Material> Chestplates = new LinkedList<Material>(java.util.Arrays.asList(Material.LEATHER_CHESTPLATE,
                                                                                         Material.CHAINMAIL_CHESTPLATE,
                                                                                         Material.IRON_CHESTPLATE,
                                                                                         Material.DIAMOND_CHESTPLATE,
                                                                                         Material.GOLD_CHESTPLATE));
    public List<Material> Leggings = new LinkedList<Material>(java.util.Arrays.asList(Material.LEATHER_LEGGINGS,
                                                                                      Material.CHAINMAIL_LEGGINGS,
                                                                                      Material.IRON_LEGGINGS,
                                                                                      Material.DIAMOND_LEGGINGS,
                                                                                      Material.GOLD_LEGGINGS));
    public List<Material> Boots = new LinkedList<Material>(java.util.Arrays
                                                               .asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
                                                                       Material.IRON_BOOTS, Material.DIAMOND_BOOTS,
                                                                       Material.GOLD_BOOTS));
    public Map<Material, List<PotionEffect>> WeaponEffects = new HashMap<Material, List<PotionEffect>>();
    public Map<Material, Double> SpeedBuffs = new HashMap<Material, Double>();
    public Map<Material, Double> StrengthBuffs = new HashMap<Material, Double>();
    public Map<Material, Double> ArmorBuffs = new HashMap<Material, Double>();
    public Queue<Projectile> arrows = new LinkedList<Projectile>();
    public int SentryEXP = 5;
    public int LogicTicks = 10;
    public int Crit1Chance;
    public int Crit2Chance;
    public int Crit3Chance;
    public int MissChance;
    public int GlanceChance;
    public String Crit1Message = "";
    public String Crit2Message = "";
    public String Crit3Message = "";
    public String GlanceMessage = "";
    public String HitMessage = "";
    public String MissMessage = "";
    public String BlockMessage = "";
    public boolean BodyguardsObeyProtection = true;
    public boolean IgnoreListInvincibility = true;
    public boolean GroupsChecked = false;
    public net.milkbowl.vault.permission.Permission perms = null;
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
    //Denizen Hook
    public boolean DieLikePlayers = false;
    //SimpleClans sSuport
    boolean ClansActive = false;
    //TownySupport
    boolean TownyActive = false;
    //War sSuport
    boolean WarActive = false;
    boolean DenizenActive = false;
    public final static Sentry getInstance() {
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
                    getLogger().log(Level.INFO, "Registered sucessfully with Vault: " + Gr.length +
                                                " groups found. The GROUP: target will function");
                }

            } catch (final Exception e) {
                getLogger().log(Level.WARNING, "Error getting groups. the GROUP target will not function.");
                this.perms = null;
            }
        }

        this.GroupsChecked = true;

    }
    /**
     * Equip a npc an item
     * <p>
     * If the item is a piece of armour the npc will equip it at the correct slot
     *
     * @param npc      The NPC to equip
     * @param material the material of the item
     *
     * @return true if the item was equiped succsessfully
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
     * @return true if the item was equiped succsessfully
     */
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
     * @return true if the item was equiped succsessfully
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
    public String getClan(final Player player) {
        if (this.ClansActive == false) { return null; }
        try {
            final net.sacredlabyrinth.phaed.simpleclans.Clan c =
                net.sacredlabyrinth.phaed.simpleclans.SimpleClans.getInstance().getClanManager()
                                                                 .getClanByPlayerName(player.getName());
            if (c != null) { return c.getName(); }
        } catch (final Exception e) {
            getLogger().info("Error getting Clan " + e.getMessage());
            return null;
        }
        return null;
    }
    private Material getMaterial(final String str) {
        return str == null ? null : Material.getMaterial(str.toUpperCase());
    }
    public String getNationNameForLocation(final Location l) {
        if (this.TownyActive == false) { return null; }
        try {
            final TownBlock tb = com.palmergames.bukkit.towny.object.TownyUniverse.getTownBlock(l);
            if (tb != null) {
                if (tb.getTown().hasNation()) { return tb.getTown().getNation().getName(); }
            }
        } catch (final Exception e) {
            return null;
        }
        return null;
    }
    private PotionEffect getpotion(final String S) {
        if (S == null) { return null; }
        final String[] args = S.trim().split(":");

        PotionEffectType type = null;

        int dur = 10;
        int amp = 1;

        type = PotionEffectType.getByName((args[0]));

//		if (type == null)
//		{
//			try
//			{
//				type = PotionEffectType.getByName (args[0])//getById (Integer.parseInt (args[0]));
//			} catch (Exception e)
//			{
//			}
//		}

        if (type == null) { return null; }

        if (args.length > 1) {
            try {
                dur = Integer.parseInt(args[1]);
            } catch (final Exception e) {
            }
        }

        if (args.length > 2) {
            try {
                amp = Integer.parseInt(args[2]);
            } catch (final Exception e) {
            }
        }

        return new PotionEffect(type, dur, amp);
    }
    public String[] getResidentTownyInfo(final Player player) {
        final String[] info = {null, null};

        if (this.TownyActive == false) { return info; }

        final com.palmergames.bukkit.towny.object.Resident resident;
        try {
            resident = com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().getResident(player.getName());
            if (resident.hasTown()) {
                info[1] = resident.getTown().getName();
                if (resident.getTown().hasNation()) {
                    info[0] = resident.getTown().getNation().getName();
                }

            }
        } catch (final Exception e) {
            return info;
        }

        return info;
    }
    public SentryInstance getSentry(final Entity ent) {
        if (ent == null) { return null; }
        if (!(ent instanceof org.bukkit.entity.LivingEntity)) { return null; }
        final NPC npc = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(ent);
        if (npc != null && npc.hasTrait(SentryTrait.class)) {
            return npc.getTrait(SentryTrait.class).getInstance();
        }

        return null;
    }
    public SentryInstance getSentry(final NPC npc) {
        if (npc != null && npc.hasTrait(SentryTrait.class)) {
            return npc.getTrait(SentryTrait.class).getInstance();
        }
        return null;
    }
    public String getWarTeam(final Player player) {
        if (this.WarActive == false) { return null; }
        try {
            final com.tommytony.war.Team t = com.tommytony.war.Team.getTeamByPlayerName(player.getName());
            if (t != null) { return t.getName(); }
        } catch (final Exception e) {
            getLogger().info("Error getting Team " + e.getMessage());
            return null;
        }
        return null;
    }
    public String getMCTeamName(final Player player) {
        final Team t = getServer().getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if (t != null) {
            return t.getName();
        }
        return null;
    }
    boolean isNationEnemy(final String Nation1, final String Nation2) {
        if (this.TownyActive == false) { return false; }
        if (Nation1.equalsIgnoreCase(Nation2)) { return false; }
        try {

            if (!com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().hasNation(Nation1) ||
                !com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().hasNation(Nation2)) { return false; }

            final com.palmergames.bukkit.towny.object.Nation theNation1 =
                com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().getNation(Nation1);
            final com.palmergames.bukkit.towny.object.Nation theNation2 =
                com.palmergames.bukkit.towny.object.TownyUniverse.getDataSource().getNation(Nation2);

            if (theNation1.hasEnemy(theNation2) || theNation2.hasEnemy(theNation1)) { return true; }

        } catch (final Exception e) {
            return false;
        }

        return false;
    }
    public void loaditemlist(final String key, final List<Material> list) {
        final List<String> strs = getConfig().getStringList(key);

        if (strs.size() > 0) { list.clear(); }

        for (final String s : getConfig().getStringList(key)) {
//			Material item = ;
            list.add(getMaterial(s.trim()));
        }

    }
    private void loadmap(final String node, final Map<Material, Double> map) {
        map.clear();
        for (final String s : getConfig().getStringList(node)) {
            final String[] args = s.trim().split(" ");
            if (args.length != 2) { continue; }

            double val = 0;

            try {
                val = Double.parseDouble(args[1]);
            } catch (final Exception e) {
            }

            final Material item = getMaterial(args[0]);

            if (item != null && val != 0 && !map.containsKey(item)) {
                map.put(item, val);
            }
        }
    }
    private void loadpots(final String node, final Map<Material, List<PotionEffect>> map) {
        map.clear();
        for (final String s : getConfig().getStringList(node)) {
            final String[] args = s.trim().split(" ");

            if (args.length < 2) { continue; }

            final Material item = getMaterial(args[0]);

            final List<PotionEffect> list = new ArrayList<PotionEffect>();

            for (int i = 1; i < args.length; i++) {
                final PotionEffect val = getpotion(args[i]);
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

        final CommandSender player = sender;

        int npcid = -1;
        int i = 0;

        //did player specify a id?
        if (tryParseInt(inargs[0])) {
            npcid = Integer.parseInt(inargs[0]);
            i = 1;
        }

        final String[] args = new String[inargs.length - i];

        for (int j = i; j < inargs.length; j++) {
            args[j - i] = inargs[j];
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Use /sentry help for command reference.");
            return true;
        }

        Boolean set = null;
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("true")) { set = true; }
            else if (args[1].equalsIgnoreCase("false")) { set = false; }
        }

        if (args[0].equalsIgnoreCase("help")) {

            player.sendMessage(ChatColor.GOLD + "------- Sentry Commands -------");
            player.sendMessage(ChatColor.GOLD +
                               "You can use /sentry (id) [command] [args] to perform any of these commands on a sentry without having it selected.");
            player.sendMessage(ChatColor.GOLD + "");
            player.sendMessage(ChatColor.GOLD + "/sentry reload");
            player.sendMessage(ChatColor.YELLOW + " reload the config.yml");
            player.sendMessage(ChatColor.GOLD + "/sentry target [add|remove] [target]");
            player.sendMessage(ChatColor.YELLOW + " Adds or removes a target to attack.");
            player.sendMessage(ChatColor.GOLD + "/sentry target [list|clear]");
            player.sendMessage(ChatColor.YELLOW + " View or clear the target list..");
            player.sendMessage(ChatColor.GOLD + "/sentry ignore [add|remove] [target]");
            player.sendMessage(ChatColor.YELLOW + " Adds or removes a target to ignore.");
            player.sendMessage(ChatColor.GOLD + "/sentry ignore [list|clear]");
            player.sendMessage(ChatColor.YELLOW + " View or clear the ignore list..");
            player.sendMessage(ChatColor.GOLD + "/sentry info");
            player.sendMessage(ChatColor.YELLOW + " View all Sentry attributes");
            player.sendMessage(ChatColor.GOLD + "/sentry equip [item|none]");
            player.sendMessage(ChatColor.YELLOW + " Equip an item on the Sentry, or remove all equipment.");
            player.sendMessage(ChatColor.GOLD + "/sentry speed [0-1.5]");
            player.sendMessage(ChatColor.YELLOW + " Sets speed of the Sentry when attacking.");
            player.sendMessage(ChatColor.GOLD + "/sentry health [1-2000000]");
            player.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Health .");
            player.sendMessage(ChatColor.GOLD + "/sentry armor [0-2000000]");
            player.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Armor.");
            player.sendMessage(ChatColor.GOLD + "/sentry strength [0-2000000]");
            player.sendMessage(ChatColor.YELLOW + " Sets the Sentry's Strength.");
            player.sendMessage(ChatColor.GOLD + "/sentry attackrate [0.0-30.0]");
            player.sendMessage(ChatColor.YELLOW + " Sets the time between the Sentry's projectile attacks.");
            player.sendMessage(ChatColor.GOLD + "/sentry healrate [0.0-300.0]");
            player.sendMessage(ChatColor.YELLOW + " Sets the frequency the sentry will heal 1 point. 0 to disable.");
            player.sendMessage(ChatColor.GOLD + "/sentry range [1-100]");
            player.sendMessage(ChatColor.YELLOW + " Sets the Sentry's detection range.");
            player.sendMessage(ChatColor.GOLD + "/sentry warningrange [0-50]");
            player.sendMessage(
                ChatColor.YELLOW + " Sets the range, beyond the detection range, that the Sentry will warn targets.");
            player.sendMessage(ChatColor.GOLD + "/sentry respawn [-1-2000000]");
            player.sendMessage(ChatColor.YELLOW + " Sets the number of seconds after death the Sentry will respawn.");
            player.sendMessage(ChatColor.GOLD + "/sentry follow [0-32]");
            player
                .sendMessage(ChatColor.YELLOW + " Sets the number of block away a bodyguard will follow. Default is 4");
            player.sendMessage(ChatColor.GOLD + "/sentry invincible");
            player.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to take no damage or knockback.");
            player.sendMessage(ChatColor.GOLD + "/sentry retaliate");
            player.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to always attack an attacker.");
            player.sendMessage(ChatColor.GOLD + "/sentry criticals");
            player.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to take critical hits and misses");
            player.sendMessage(ChatColor.GOLD + "/sentry drops");
            player.sendMessage(ChatColor.YELLOW + " Toggle the Sentry to drop equipped items on death");
            player.sendMessage(ChatColor.GOLD + "/sentry killdrops");
            player.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry's victims drop items and exp");
            player.sendMessage(ChatColor.GOLD + "/sentry mount");
            player.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry rides a mount");
            player.sendMessage(ChatColor.GOLD + "/sentry targetable");
            player.sendMessage(ChatColor.YELLOW + " Toggle whether or not the sentry is attacked by hostile mobs");
            player.sendMessage(ChatColor.GOLD + "/sentry spawn");
            player.sendMessage(ChatColor.YELLOW + " Set the sentry to respawn at its current location");
            player.sendMessage(ChatColor.GOLD + "/sentry warning 'The Test to use'");
            player.sendMessage(
                ChatColor.YELLOW + " Change the warning text. <NPC> and <PLAYER> can be used as placeholders");
            player.sendMessage(ChatColor.GOLD + "/sentry greeting 'The text to use'");
            player.sendMessage(
                ChatColor.YELLOW + " Change the greeting text. <NPC> and <PLAYER> can be used as placeholders");
            return true;
        }
        else if (args[0].equalsIgnoreCase("debug")) {

            this.debug = !this.debug;

            player.sendMessage(ChatColor.GREEN + "Debug now: " + this.debug);
            return true;
        }
        else if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("sentry.reload")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            this.reloadMyConfig();
            player.sendMessage(ChatColor.GREEN + "reloaded Sentry/config.yml");
            return true;
        }
        NPC ThisNPC;

        if (npcid == -1) {

            ThisNPC = ((Citizens) this.getServer().getPluginManager().getPlugin("Citizens")).getNPCSelector()
                                                                                            .getSelected(sender);

            if (ThisNPC != null) {
                // Gets NPC Selected
                npcid = ThisNPC.getId();
            }

            else {
                player.sendMessage(ChatColor.RED + "You must have a NPC selected to use this command");
                return true;
            }
        }

        ThisNPC = CitizensAPI.getNPCRegistry().getById(npcid);

        if (ThisNPC == null) {
            player.sendMessage(ChatColor.RED + "NPC with id " + npcid + " not found");
            return true;
        }

        if (!ThisNPC.hasTrait(SentryTrait.class)) {
            player.sendMessage(ChatColor.RED + "That command must be performed on a Sentry!");
            return true;
        }

        if (sender instanceof Player && !CitizensAPI.getNPCRegistry().isNPC((Entity) sender)) {

            if (ThisNPC.getTrait(Owner.class).getOwner().equalsIgnoreCase(player.getName())) {
                //OK!
            }
            else {
                //not player is owner
                if (!((Player) sender).hasPermission("citizens.admin")) {
                    //no c2 admin.
                    player.sendMessage(ChatColor.RED + "You must be the owner of this Sentry to execute commands.");
                    return true;
                }
                else {
                    //has citizens.admin
                    if (!ThisNPC.getTrait(Owner.class).getOwner().equalsIgnoreCase("server")) {
                        //not server-owned NPC
                        player.sendMessage(ChatColor.RED +
                                           "You, or the server, must be the owner of this Sentry to execute commands.");
                        return true;
                    }
                }
            }
        }

        // Commands

        final SentryInstance inst = ThisNPC.getTrait(SentryTrait.class).getInstance();

        if (args[0].equalsIgnoreCase("spawn")) {
            if (!player.hasPermission("sentry.spawn")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (ThisNPC.getEntity() == null) {
                player.sendMessage(ChatColor.RED + "Cannot set spawn while " + ThisNPC.getName() + " is dead.");
                return true;
            }
            inst.setSpawn(ThisNPC.getEntity().getLocation());
            player.sendMessage(
                ChatColor.GREEN + ThisNPC.getName() + " will respawn at its present location."); // Talk to the player.
            return true;

        }
        //		if (args[0].equalsIgnoreCase("derp")) {
        //			org.bukkit.inventory.PlayerInventory inv = ((Player)sender).getInventory();
        //
        //			for (org.bukkit.inventory.ItemStack ii:inv.getContents()){
        //				if (ii ==null) {
        //					player.sendMessage("item null");
        //					continue;
        //				}
        //				player.sendMessage(ii.getTypeId() + ":" + ii.getData());  // Talk to the player.
        //
        //			}
        //
        //			org.bukkit.inventory.ItemStack is = new org.bukkit.inventory.ItemStack(358,1,(short)0,(byte)2);
        //			player.sendMessage(is.getData().toString());
        //			//Prints MAP(2), OK!
        //
        //			org.bukkit.inventory.ItemStack is2 = new org.bukkit.inventory.ItemStack(358);
        //			is2.setDurability((short)2);
        //			player.sendMessage(is2.getData().toString());
        //			//Prints MAP(2), OK!
        //
        //			org.bukkit.inventory.ItemStack is3 = new org.bukkit.inventory.ItemStack(358);
        //			is3.setData(new org.bukkit.material.MaterialData(358,(byte)2));
        //			player.sendMessage(is3.getData().toString());
        //			//Prints MAP(0), WHY???
        //
        //
        //			HashMap<Integer, ItemStack> poop = inv.removeItem(is);
        //
        //			return true;
        //
        //		}

        else if (args[0].equalsIgnoreCase("invincible")) {
            if (!player.hasPermission("sentry.options.invincible")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setInvincible(set == null ? !inst.isInvincible() : set);

            if (!inst.isInvincible()) {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " now takes damage.."); // Talk to the player.
            }
            else {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " now INVINCIBLE."); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("retaliate")) {
            if (!player.hasPermission("sentry.options.retaliate")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setRetaliate(set == null ? !inst.isRetaliate() : set);

            if (!inst.isRetaliate()) {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will not retaliate."); // Talk to the player.
            }
            else {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                   " will retalitate against all attackers."); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("criticals")) {
            if (!player.hasPermission("sentry.options.criticals")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setLuckyHits(set == null ? !inst.isLuckyHits() : set);

            if (!inst.isLuckyHits()) {
                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will take normal damage."); // Talk to the player.
            }
            else {
                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will take critical hits."); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("drops")) {
            if (!player.hasPermission("sentry.options.drops")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setDropInventory(set == null ? !inst.isDropInventory() : set);

            if (inst.isDropInventory()) {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will drop items"); // Talk to the player.
            }
            else {
                player
                    .sendMessage(ChatColor.GREEN + ThisNPC.getName() + " will not drop items."); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("killdrops")) {
            if (!player.hasPermission("sentry.options.killdrops")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setKillsDropInventory(set == null ? !inst.doesKillsDropInventory() : set);

            if (inst.doesKillsDropInventory()) {
                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + "'s kills will drop items or exp"); // Talk to the player.
            }
            else {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                   "'s kills will not drop items or exp."); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("targetable")) {
            if (!player.hasPermission("sentry.options.targetable")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            inst.setTargetable(set == null ? !inst.isTargetable() : set);
            ThisNPC.data().set(NPC.TARGETABLE_METADATA, inst.isTargetable());

            if (inst.isTargetable()) {
                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will be targeted by mobs"); // Talk to the player.
            }
            else {
                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " will not be targeted by mobs"); // Talk to the player.
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("mount")) {
            if (!player.hasPermission("sentry.options.mount")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            set = set == null ? !inst.isMounted() : set;

            if (set) {
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " is now Mounted"); // Talk to the player.
                inst.createMount();
                inst.mount();
            }
            else {
                player
                    .sendMessage(ChatColor.GREEN + ThisNPC.getName() + " is no longer Mounted"); // Talk to the player.
                if (inst.isMounted()) { Util.removeMount(inst.getMountID()); }
                inst.setMountID(-1);
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("guard")) {
            if (!player.hasPermission("sentry.guard")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            boolean localonly = false;
            boolean playersonly = false;
            int start = 1;

            if (args.length > 1) {

                if (args[1].equalsIgnoreCase("-p")) {
                    start = 2;
                    playersonly = true;
                }

                if (args[1].equalsIgnoreCase("-l")) {
                    start = 2;
                    localonly = true;
                }

                String arg = "";
                for (i = start; i < args.length; i++) {
                    arg += " " + args[i];
                }
                arg = arg.trim();

                final LivingEntity et = Bukkit.getPlayer(arg);

                boolean ok = false;

                if (!playersonly) {
                    ok = inst.setGuardTarget(et, false);
                }

                if (!localonly) {
                    ok = inst.setGuardTarget(et, true);
                }

                if (ok) {
                    player.sendMessage(
                        ChatColor.GREEN + ThisNPC.getName() + " is now guarding " + arg); // Talk to the player.
                }
                else {
                    player.sendMessage(
                        ChatColor.RED + ThisNPC.getName() + " could not find " + arg + "."); // Talk to the player.
                }

            }

            else {
                if (inst.getGuardTarget() == null) {
                    player.sendMessage(ChatColor.RED + ThisNPC.getName() +
                                       " is already set to guard its immediate area"); // Talk to the player.
                }
                else {
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() +
                                       " is now guarding its immediate area. "); // Talk to the player.
                }
                inst.setGuardTarget(null, false);

            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("follow")) {
            if (!player.hasPermission("sentry.stats.follow")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(
                    ChatColor.GOLD + ThisNPC.getName() + "'s Follow Distance is " + inst.getFollowDistance());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry follow [#]. Default is 4. ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 32) { HPs = 32; }
                if (HPs < 0) { HPs = 0; }

                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " follow distance set to " + HPs +
                                   "."); // Talk to the player.
                inst.setFollowDistance(HPs * HPs);

            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("health")) {
            if (!player.hasPermission("sentry.stats.health")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Health is " + inst.getSentryHealth());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry health [#]  note: Typically players");
                player.sendMessage(ChatColor.GOLD + " have 20 HPs when fully healed");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 1) { HPs = 1; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " health set to " + HPs + "."); // Talk to the player.
                inst.setSentryHealth(HPs);
                inst.setHealth(HPs);
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("armor")) {
            if (!player.hasPermission("sentry.stats.armor")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Armor is " + inst.getArmor());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry armor [#] ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 0) { HPs = 0; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " armor set to " + HPs + "."); // Talk to the player.
                inst.setArmor(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("strength")) {
            if (!player.hasPermission("sentry.stats.strength")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Strength is " + inst.getStrength());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry strength # ");
                player.sendMessage(ChatColor.GOLD + "Note: At Strength 0 the Sentry will do no damamge. ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < 0) { HPs = 0; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " strength set to " + HPs + "."); // Talk to the player.
                inst.setStrength(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("nightvision")) {
            if (!player.hasPermission("sentry.stats.nightvision")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Night Vision is " + inst.getNightVision());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry nightvision [0-16] ");
                player.sendMessage(ChatColor.GOLD + "Usage: 0 = See nothing, 16 = See everything. ");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 16) { HPs = 16; }
                if (HPs < 0) { HPs = 0; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " Night Vision set to " + HPs + "."); // Talk to the player.
                inst.setNightVision(HPs);

            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("respawn")) {
            if (!player.hasPermission("sentry.stats.respawn")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                if (inst.getRespawnDelaySeconds() == 0) {
                    player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + " will not automatically respawn.");
                }
                if (inst.getRespawnDelaySeconds() == -1) {
                    player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + " will be deleted upon death");
                }
                if (inst.getRespawnDelaySeconds() > 0) {
                    player.sendMessage(
                        ChatColor.GOLD + ThisNPC.getName() + " respawns after " + inst.getRespawnDelaySeconds() + "s");
                }

                player.sendMessage(ChatColor.GOLD + "Usage: /sentry respawn [-1 - 2000000] ");
                player.sendMessage(ChatColor.GOLD + "Usage: set to 0 to prevent automatic respawn");
                player.sendMessage(ChatColor.GOLD + "Usage: set to -1 to *permanently* delete the Sentry on death.");
            }
            else {

                int HPs = Integer.valueOf(args[1]);
                if (HPs > 2000000) { HPs = 2000000; }
                if (HPs < -1) { HPs = -1; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " now respawns after " + HPs + "s."); // Talk to the player.
                inst.setRespawnDelaySeconds(HPs);

            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("speed")) {
            if (!player.hasPermission("sentry.stats.speed")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Speed is " + inst.getSentrySpeed());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry speed [0.0 - 2.0]");
            }
            else {

                Float HPs = Float.valueOf(args[1]);
                if (HPs > 2.0) { HPs = 2.0f; }
                if (HPs < 0.0) { HPs = 0f; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " speed set to " + HPs + "."); // Talk to the player.
                inst.setSentrySpeed(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("attackrate")) {
            if (!player.hasPermission("sentry.stats.attackrate")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(
                    ChatColor.GOLD + ThisNPC.getName() + "'s Projectile Attack Rate is " + inst.getAttackRateSeconds() +
                    "s between shots.");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry attackrate [0.0 - 30.0]");
            }
            else {

                Double HPs = Double.valueOf(args[1]);
                if (HPs > 30.0) { HPs = 30.0; }
                if (HPs < 0.0) { HPs = 0.0; }

                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Projectile Attack Rate set to " + HPs +
                                   "."); // Talk to the player.
                inst.setAttackRateSeconds(HPs);
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("healrate")) {
            if (!player.hasPermission("sentry.stats.healrate")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Heal Rate is " + inst.getHealRate() + "s");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry healrate [0.0 - 300.0]");
                player.sendMessage(ChatColor.GOLD + "Usage: Set to 0 to disable healing");
            }
            else {

                Double HPs = Double.valueOf(args[1]);
                if (HPs > 300.0) { HPs = 300.0; }
                if (HPs < 0.0) { HPs = 0.0; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " Heal Rate set to " + HPs + "."); // Talk to the player.
                inst.setHealRate(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("range")) {
            if (!player.hasPermission("sentry.stats.range")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Range is " + inst.getSentryRange());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry range [1 - 100]");
            }

            else {

                Integer HPs = Integer.valueOf(args[1]);
                if (HPs > 100) { HPs = 100; }
                if (HPs < 1) { HPs = 1; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " range set to " + HPs + "."); // Talk to the player.
                inst.setSentryRange(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("warningrange")) {
            if (!player.hasPermission("sentry.stats.warningrange")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                player
                    .sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Warning Range is " + inst.getWarningRange());
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry warningrangee [0 - 50]");
            }

            else {

                Integer HPs = Integer.valueOf(args[1]);
                if (HPs > 50) { HPs = 50; }
                if (HPs < 0) { HPs = 0; }

                player.sendMessage(
                    ChatColor.GREEN + ThisNPC.getName() + " warning range set to " + HPs + "."); // Talk to the player.
                inst.setWarningRange(HPs);

            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("equip")) {
            if (!player.hasPermission("sentry.equip")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length <= 1) {
                player.sendMessage(
                    ChatColor.RED + "You must specify a Item ID or Name. or specify 'none' to remove all equipment.");
            }

            else {

                if (ThisNPC.getEntity().getType() == org.bukkit.entity.EntityType.ENDERMAN ||
                    ThisNPC.getEntity().getType() == org.bukkit.entity.EntityType.PLAYER) {
                    if (args[1].equalsIgnoreCase("none")) {
                        //remove equipment
                        equip(ThisNPC, Material.AIR);
                        inst.UpdateWeapon();
                        player.sendMessage(ChatColor.YELLOW + ThisNPC.getName() + "'s equipment cleared.");
                    }
                    else {
                        final Material mat = getMaterial(args[1]);
                        if (mat != null) {
                            final ItemStack is = new ItemStack(mat);
                            if (equip(ThisNPC, is)) {
                                inst.UpdateWeapon();
                                player.sendMessage(ChatColor.GREEN + " equipped " + is.getType().toString() + " on " +
                                                   ThisNPC.getName());
                            }
                            else { player.sendMessage(ChatColor.RED + " Could not equip: invalid mob type?"); }
                        }
                        else { player.sendMessage(ChatColor.RED + " Could not equip: unknown item name"); }
                    }
                }
                else { player.sendMessage(ChatColor.RED + " Could not equip: must be Player or Enderman type"); }
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("warning")) {
            if (!player.hasPermission("sentry.warning")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            if (args.length >= 2) {
                String arg = "";
                for (i = 1; i < args.length; i++) {
                    arg += " " + args[i];
                }
                arg = arg.trim();

                final String str =
                    arg.replaceAll("\"$", "").replaceAll("^\"", "").replaceAll("'$", "").replaceAll("^'", "");
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " warning message set to " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', str) + "."); // Talk to the player.
                inst.setWarningMessage(str);
            }
            else {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Warning Message is: " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', inst.getWarningMessage()));
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry warning 'The Text to use'");
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("greeting")) {
            if (!player.hasPermission("sentry.greeting")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length >= 2) {

                String arg = "";
                for (i = 1; i < args.length; i++) {
                    arg += " " + args[i];
                }
                arg = arg.trim();

                final String str =
                    arg.replaceAll("\"$", "").replaceAll("^\"", "").replaceAll("'$", "").replaceAll("^'", "");
                player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Greeting message set to " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', str) + "."); // Talk to the player.
                inst.setGreetingMessage(str);
            }
            else {
                player.sendMessage(ChatColor.GOLD + ThisNPC.getName() + "'s Greeting Message is: " + ChatColor.RESET +
                                   ChatColor.translateAlternateColorCodes('&', inst.getGreetingMessage()));
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry greeting 'The Text to use'");
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("info")) {
            if (!player.hasPermission("sentry.info")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }
            player.sendMessage(
                ChatColor.GOLD + "------- Sentry Info for (" + ThisNPC.getId() + ") " + ThisNPC.getName() + "------");
            player.sendMessage(ChatColor.GREEN + inst.getStats());
            player.sendMessage(
                ChatColor.GREEN + "Invincible: " + inst.isInvincible() + " Retaliate: " + inst.isRetaliate());
            player.sendMessage(
                ChatColor.GREEN + "Drops Items: " + inst.isDropInventory() + " Critical Hits: " + inst.isLuckyHits());
            player.sendMessage(
                ChatColor.GREEN + "Kills Drop Items: " + inst.doesKillsDropInventory() + " Respawn Delay: " +
                inst.getRespawnDelaySeconds() + "s");
            player.sendMessage(ChatColor.BLUE + "Status: " + inst.getSentryStatus());
            if (inst.getMeleeTarget() == null) {
                if (inst.getProjectileTarget() == null) { player.sendMessage(ChatColor.BLUE + "Target: Nothing"); }
                else { player.sendMessage(ChatColor.BLUE + "Target: " + inst.getProjectileTarget().toString()); }
            }
            else { player.sendMessage(ChatColor.BLUE + "Target: " + inst.getMeleeTarget().toString()); }

            if (inst.getGuardTarget() == null) { player.sendMessage(ChatColor.BLUE + "Guarding: My Surroundings"); }
            else { player.sendMessage(ChatColor.BLUE + "Guarding: " + inst.getGuardTarget()); }

            return true;
        }

        else if (args[0].equalsIgnoreCase("target")) {
            if (!player.hasPermission("sentry.target")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatColor.GOLD +
                                   "Usage: /sentry target add [entity:Name] or [player:Name] or [group:Name] or [entity:monster] or [entity:player]");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry target remove [target]");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry target clear");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry target list");
                return true;
            }

            else {

                String arg = "";
                for (i = 2; i < args.length; i++) {
                    arg += " " + args[i];
                }
                arg = arg.trim();

                if (arg.equalsIgnoreCase("nationenemies") && inst.myNPC.isSpawned()) {
                    final String natname = getNationNameForLocation(inst.myNPC.getEntity().getLocation());
                    if (natname != null) {
                        arg += ":" + natname;
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Could not get Nation for this NPC's location");
                        return true;
                    }
                }

                if (args[1].equals("add") && arg.length() > 0 && arg.split(":").length > 1) {

                    if (!inst.containsTarget(arg.toUpperCase())) { inst.getValidTargets().add(arg.toUpperCase()); }
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Target added. Now targeting " +
                                       inst.getValidTargets().toString());
                    return true;
                }

                else if (args[1].equals("remove") && arg.length() > 0 && arg.split(":").length > 1) {

                    inst.getValidTargets().remove(arg.toUpperCase());
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Targets removed. Now targeting " +
                                       inst.getValidTargets().toString());
                    return true;
                }

                else if (args[1].equals("clear")) {

                    inst.getValidTargets().clear();
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Targets cleared.");
                    return true;
                }
                else if (args[1].equals("list")) {
                    player.sendMessage(ChatColor.GREEN + "Targets: " + inst.getValidTargets().toString());
                    return true;
                }

                else {
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry target list");
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry target clear");
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry target add type:name");
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry target remove type:name");
                    player.sendMessage(ChatColor.GOLD +
                                       "type:name can be any of the following: entity:MobName entity:monster entity:player entity:all player:PlayerName group:GroupName town:TownName nation:NationName faction:FactionName");

                    return true;
                }
            }
        }

        else if (args[0].equalsIgnoreCase("ignore")) {
            if (!player.hasPermission("sentry.ignore")) {
                player.sendMessage(ChatColor.RED + "You do not have permissions for that command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore list");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore clear");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore add type:name");
                player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore remove type:name");
                player.sendMessage(ChatColor.GOLD +
                                   "type:name can be any of the following: entity:MobName entity:monster entity:player entity:all player:PlayerName group:GroupName town:TownName nation:NationName faction:FactionName");

                return true;
            }

            else {

                String arg = "";
                for (i = 2; i < args.length; i++) {
                    arg += " " + args[i];
                }
                arg = arg.trim();

                if (args[1].equals("add") && arg.length() > 0 && arg.split(":").length > 1) {
                    if (!inst.containsIgnore(arg.toUpperCase())) { inst.getIgnoreTargets().add(arg.toUpperCase()); }
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore added. Now ignoring " +
                                       inst.getIgnoreTargets().toString());
                    return true;
                }

                else if (args[1].equals("remove") && arg.length() > 0 && arg.split(":").length > 1) {

                    inst.getIgnoreTargets().remove(arg.toUpperCase());
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore removed. Now ignoring " +
                                       inst.getIgnoreTargets().toString());
                    return true;
                }

                else if (args[1].equals("clear")) {

                    inst.getIgnoreTargets().clear();
                    inst.processTargets();
                    inst.setTarget(null, false);
                    player.sendMessage(ChatColor.GREEN + ThisNPC.getName() + " Ignore cleared.");
                    return true;
                }
                else if (args[1].equals("list")) {

                    player.sendMessage(ChatColor.GREEN + "Ignores: " + inst.getIgnoreTargets().toString());
                    return true;
                }

                else {

                    player.sendMessage(ChatColor.GOLD +
                                       "Usage: /sentry ignore add [ENTITY:Name] or [PLAYER:Name] or [GROUP:Name] or [ENTITY:MONSTER]");
                    player.sendMessage(ChatColor.GOLD +
                                       "Usage: /sentry ignore remove [ENTITY:Name] or [PLAYER:Name] or [GROUP:Name] or [ENTITY:MONSTER]");
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore clear");
                    player.sendMessage(ChatColor.GOLD + "Usage: /sentry ignore list");
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
        if (getServer().getPluginManager().getPlugin("Citizens") == null ||
            !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {

            if (checkPlugin("Denizen")) {
                final String vers = getServer().getPluginManager().getPlugin("Denizen").getDescription().getVersion();
                if (vers.startsWith("0.7") || vers.startsWith("0.8")) {
                    getLogger().log(Level.WARNING, "Sentry is not compatible with Denizen .7 or .8");
                }
                else if (vers.startsWith("0.9")) {
                    DenizenHook.SentryPlugin = this;
                    DenizenHook.DenizenPlugin = getServer().getPluginManager().getPlugin("Denizen");
                    DenizenHook.setupDenizenHook();
                    this.DenizenActive = true;
                }
                else {
                    getLogger().log(Level.WARNING, "Unknown version of Denizen");
                }
            }
        } catch (final NoClassDefFoundError e) {
            getLogger().log(Level.WARNING, "An error occured attempting to register with Denizen " + e.getMessage());
        } catch (final Exception e) {
            getLogger().log(Level.WARNING, "An error occured attempting to register with Denizen " + e.getMessage());
        }

        if (this.DenizenActive) {
            getLogger().log(Level.INFO, "NPCDeath Triggers and DIE/LIVE command registered sucessfully with Denizen");
        }
        else { getLogger().log(Level.INFO, "Could not register with Denizen"); }

        if (checkPlugin("Towny")) {
            getLogger()
                .log(Level.INFO, "Registered with Towny sucessfully. the TOWN: and NATION: targets will function");
            this.TownyActive = true;
        }
        else { getLogger().log(Level.INFO, "Could not find or register with Towny"); }

        if (checkPlugin("Factions")) {
            getLogger().log(Level.INFO, "Registered with Factions sucessfully. the FACTION: target will function");
            FactionsActive = true;
        }
        else { getLogger().log(Level.INFO, "Could not find or register with Factions."); }

        if (checkPlugin("War")) {
            getLogger().log(Level.INFO, "Registered with War sucessfully. The TEAM: target will function");
            this.WarActive = true;
        }
        else { getLogger().log(Level.INFO, "Could not find or register with War. "); }

        if (checkPlugin("SimpleClans")) {
            getLogger().log(Level.INFO, "Registered with SimpleClans sucessfully. The CLAN: target will function");
            this.ClansActive = true;
        }
        else { getLogger().log(Level.INFO, "Could not find or register with SimpleClans. "); }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SentryTrait.class).withName("sentry"));

        Sentry.instance = this;

        this.getServer().getPluginManager().registerEvents(new SentryListener(this), this);

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                //Unloaded chunk arrow cleanup
                while (Sentry.this.arrows.size() > 200) {
                    final Projectile a = Sentry.this.arrows.remove();
                    if (a != null) {
                        a.remove();
                        //	x++;
                    }
                }
            }
        }, 40, 20 * 120);

        reloadMyConfig();
    }
    private void reloadMyConfig() {
        this.saveDefaultConfig();
        this.reloadConfig();
        loadmap("ArmorBuffs", this.ArmorBuffs);
        loadmap("StrengthBuffs", this.StrengthBuffs);
        loadmap("SpeedBuffs", this.SpeedBuffs);
        loadpots("WeaponEffects", this.WeaponEffects);
        loaditemlist("Helmets", this.Helmets);
        loaditemlist("Chestplates", this.Chestplates);
        loaditemlist("Leggings", this.Leggings);
        loaditemlist("Boots", this.Boots);
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
        this.BodyguardsObeyProtection = getConfig().getBoolean("Server.BodyguardsObeyProtection", true);
        this.IgnoreListInvincibility = getConfig().getBoolean("Server.IgnoreListInvincibility", true);
        this.LogicTicks = getConfig().getInt("Server.LogicTicks", 10);
        this.SentryEXP = getConfig().getInt("Server.ExpValue", 5);
        this.MissMessage = getConfig().getString("GlobalTexts.Miss", null);
        this.HitMessage = getConfig().getString("GlobalTexts.Hit", null);
        this.BlockMessage = getConfig().getString("GlobalTexts.Block", null);
        this.Crit1Message = getConfig().getString("GlobalTexts.Crit1", null);
        this.Crit2Message = getConfig().getString("GlobalTexts.Crit2", null);
        this.Crit3Message = getConfig().getString("GlobalTexts.Crit3", null);
        this.GlanceMessage = getConfig().getString("GlobalTexts.Glance", null);
        this.MissChance = getConfig().getInt("HitChances.Miss", 0);
        this.GlanceChance = getConfig().getInt("HitChances.Glance", 0);
        this.Crit1Chance = getConfig().getInt("HitChances.Crit1", 0);
        this.Crit2Chance = getConfig().getInt("HitChances.Crit2", 0);
        this.Crit3Chance = getConfig().getInt("HitChances.Crit3", 0);

    }
    private boolean setupPermissions() {
        try {

            if (getServer().getPluginManager().getPlugin("Vault") == null ||
                getServer().getPluginManager().getPlugin("Vault").isEnabled() == false) {
                return false;
            }

            final RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

            if (permissionProvider != null) {
                this.perms = permissionProvider.getProvider();
            }

            return (this.perms != null);

        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean tryParseInt(final String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

}
