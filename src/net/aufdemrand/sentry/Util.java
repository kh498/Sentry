package net.aufdemrand.sentry;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.LocaleI18n;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SuppressWarnings("WeakerAccess")
public class Util {

    public static Location getFireSource(final LivingEntity from, final LivingEntity to) {

        final Location loco = from.getEyeLocation();
        Vector norman = to.getEyeLocation().subtract(loco).toVector();
        norman = normalizeVector(norman);
        norman.multiply(.5);

        return loco.add(norman);

    }

    public static Location leadLocation(final Location loc, final Vector victor, final double t) {

        return loc.clone().add(victor.clone().multiply(t));

    }

    public static void removeMount(final int npcID) {
        final NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        if (npc != null) {
            if (npc.getEntity() != null) {
                npc.getEntity().setPassenger(null);
            }
            npc.destroy();
        }
    }

    public static boolean CanWarp(final Entity player) {
        if (player instanceof Player) {

            if (player.hasPermission("sentry.bodyguard.*")) {
                //have * perm, which all players do by default.

                if (player.isPermissionSet("sentry.bodyguard." + player.getWorld().getName())) {

                    if (!player.hasPermission("sentry.bodyguard." + player.getWorld().getName())) {
                        //denied this world.
                        return false;
                    }

                }
                else { return true; }

            }

            if (player.hasPermission("sentry.bodyguard." + player.getWorld().getName())) {
                //no * but specifically allowed this world.
                return true;
            }

        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public static String getLocalItemName(final Material Mat) {
        if (Mat == Material.AIR) { return "Hand"; }
        if (Mat.getId() < 256) {
            final Block b = getMCBlock(Mat.getId());
            return b.getName();
        }
        else {
            final Item b = getMCItem(Mat.getId());
            return LocaleI18n.get(b.getName() + ".name");
        }
    }

    public static double hangTime(final double launchAngle, final double v, final double elev, final double g) {

        final double a = v * Math.sin(launchAngle);
        final double b = -2 * g * elev;

        if (Math.pow(a, 2) + b < 0) {
            return 0;
        }

        return (a + Math.sqrt(Math.pow(a, 2) + b)) / g;

    }

    //check for obfuscation change
    public static Item getMCItem(final int id) {
        return Item.getById(id);
    }

    //check for obfuscation change
    public static Block getMCBlock(final int id) {
        return Block.getById(id);
    }

    public static Double launchAngle(final Location from, final Location to, final double v, final double elev,
                                     final double g) {

        final Vector victor = from.clone().subtract(to).toVector();
        final double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));

        final double v2 = Math.pow(v, 2);
        final double v4 = Math.pow(v, 4);

        final double derp = g * (g * Math.pow(dist, 2) + 2 * elev * v2);

        //Check if hittable.
        if (v4 < derp) {
            //target unreachable
            // use this to fire at optimal max angle launchAngle = Math.atan( ( 2*g*elev + v2) / (2*g*elev + 2*v2));
            return null;
        }
        else {
            //calc angle
            return Math.atan((v2 - Math.sqrt(v4 - derp)) / (g * dist));
        }

    }

    public static String format(String input, final NPC npc, final CommandSender player, final Material item,
                                final String amount) {
        if (input == null) { return null; }
        input = input.replace("<NPC>", npc.getName());
        input = input.replace("<PLAYER>", player == null ? "" : player.getName());
        input = input.replace("<ITEM>", Util.getLocalItemName(item));
        input = input.replace("<AMOUNT>", amount);
        input = ChatColor.translateAlternateColorCodes('&', input);
        return input;
    }

    public static Vector normalizeVector(final Vector victor) {
        final double mag =
            Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getY(), 2) + Math.pow(victor.getZ(), 2));
        if (mag != 0) { return victor.multiply(1 / mag); }
        return victor.multiply(0);
    }


}


