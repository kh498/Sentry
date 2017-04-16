package net.aufdemrand.sentry;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class DenizenHook {

    static boolean DenizenActive = false;
    static Plugin DenizenPlugin;
    static Sentry SentryPlugin;

    public static boolean SentryDeath(final Set<Player> _myDamagers, final NPC npc) {
        if (!DenizenActive) { return false; }

        try {
            boolean a = false;
            final boolean b = false;
            boolean c = false;

            final net.aufdemrand.denizen.Denizen d = (Denizen) DenizenPlugin;

            final NpcdeathTrigger npcd = d.getTriggerRegistry().get(NpcdeathTrigger.class);
            final NpcdeathTriggerOwner npcdo = d.getTriggerRegistry().get(NpcdeathTriggerOwner.class);

            if (npc != null) { a = npcd.Die(_myDamagers, npc); }
            if (npc != null) { c = npcdo.Die(npc); }
            return (a || b || c);
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void setupDenizenHook() {

        final DenizenHook me = new DenizenHook();

        me.new NpcdeathTriggerOwner().activate().as("Npcdeathowner");
        me.new NpcdeathTrigger().activate().as("Npcdeath");

        final DieCommand dc = me.new DieCommand();
        final LiveCommand lc = me.new LiveCommand();

        dc.activate().as("die").withOptions("die", 0);
        lc.activate().as("live").withOptions("live", 0);

        DenizenActive = true;
    }

    public static void DenizenAction(final NPC npc, final String action, final org.bukkit.OfflinePlayer player) {
        if (DenizenActive) {
            final dNPC dnpc = dNPC.mirrorCitizensNPC(npc);
            if (dnpc != null) {
                try {
                    dnpc.action(action, dPlayer.mirrorBukkitPlayer(player));
                } catch (final Exception e) {

                }
            }
        }
    }

    private class LiveCommand extends AbstractCommand {

        @Override
        public void execute(final ScriptEntry theEntry) throws CommandExecutionException {
            final LivingEntity ent = (LivingEntity) ((BukkitScriptEntryData) theEntry.entryData).getNPC().getEntity();

            final SentryInstance inst =
                ((BukkitScriptEntryData) theEntry.entryData).getNPC().getCitizen().getTrait(SentryTrait.class)
                                                            .getInstance();

            if (ent != null) {
                if (((BukkitScriptEntryData) theEntry.entryData).getNPC().getCitizen().hasTrait(SentryTrait.class)) {
                    boolean deaggro = false;

                    for (final String arg : theEntry.getArguments()) {
                        if (arg.equalsIgnoreCase("peace")) { deaggro = true; }
                    }

                    String db = "RISE! " + ((BukkitScriptEntryData) theEntry.entryData).getNPC().getName() + "!";
                    if (deaggro) { db += " ..And fight no more!"; }
                    dB.log(db);

                    if (inst != null) {
                        inst.setSentryStatus(net.aufdemrand.sentry.SentryInstance.Status.LOOKING);
                        if (deaggro) { inst.clearTarget(); }
                    }
                }
            }
            else {
                throw new CommandExecutionException("Entity not found");
            }
        }

        @Override
        public void parseArgs(final ScriptEntry arg0) throws InvalidArgumentsException {

        }
    }

    private class DieCommand extends AbstractCommand {

        @Override
        public void execute(final ScriptEntry theEntry) throws CommandExecutionException {
            final LivingEntity ent = (LivingEntity) ((BukkitScriptEntryData) theEntry.entryData).getNPC().getEntity();

            final SentryInstance inst =
                ((BukkitScriptEntryData) theEntry.entryData).getNPC().getCitizen().getTrait(SentryTrait.class)
                                                            .getInstance();

            if (inst != null) {
                dB.log("Goodbye, cruel world... ");
                inst.die(false, org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM);
            }
            else if (ent != null) {
                ent.remove();
            }
            else {
                throw new CommandExecutionException("Entity not found");
            }
        }

        @Override
        public void parseArgs(final ScriptEntry arg0) throws InvalidArgumentsException {
            // TODO Auto-generated method stub

        }
    }

    private class NpcdeathTriggerOwner extends net.aufdemrand.denizen.scripts.triggers.AbstractTrigger {

        @Override
        public void onEnable() {
            // TODO Auto-generated method stub
        }

        public boolean Die(final NPC npc) {

            // Check if NPC has triggers.
            if (!npc.hasTrait(TriggerTrait.class)) { return false; }

            // Check if trigger is enabled.
            if (!npc.getTrait(TriggerTrait.class).isEnabled(this.name)) {
                return false;
            }

            final dNPC theDenizen = dNPC.mirrorCitizensNPC(npc);

            dB.echoDebug(null, DebugElement.Header, "Parsing NPCDeath/Owner Trigger.");

            final String owner = npc.getTrait(net.citizensnpcs.api.trait.trait.Owner.class).getOwner();

            final dPlayer thePlayer = net.aufdemrand.denizen.objects.dPlayer.valueOf(owner);

            if (thePlayer == null) {
                dB.echoDebug(null, DebugElement.Header, "Owner not found!");
                return false;
            }

            final InteractScriptContainer script = theDenizen.getInteractScriptQuietly(thePlayer, this.getClass());

            return parse(theDenizen, thePlayer, script);

        }

    }

    private class NpcdeathTrigger extends net.aufdemrand.denizen.scripts.triggers.AbstractTrigger {

        public boolean Die(final Set<Player> _myDamagers, final NPC npc) {

            // Check if NPC has triggers.
            if (!npc.hasTrait(TriggerTrait.class)) { return false; }

            // Check if trigger is enabled.
            if (!npc.getTrait(TriggerTrait.class).isEnabled(this.name)) {
                return false;
            }

            final dNPC theDenizen = dNPC.mirrorCitizensNPC(npc);

            dB.echoDebug(null, DebugElement.Header, "Parsing NPCDeath/Killers Trigger");

            boolean founone = false;

            for (final Player thePlayer : _myDamagers) {

                if (thePlayer != null && thePlayer.getLocation().distance(npc.getEntity().getLocation()) > 300) {
                    dB.echoDebug(null, DebugElement.Header, thePlayer.getName() + " is to far away.");
                    continue;
                }

                final InteractScriptContainer script =
                    theDenizen.getInteractScriptQuietly(dPlayer.mirrorBukkitPlayer(thePlayer), this.getClass());

                if (parse(theDenizen, dPlayer.mirrorBukkitPlayer(thePlayer), script)) { founone = true; }
            }

            return founone;
        }
        @Override
        public void onEnable() {
            // TODO Auto-generated method stub
        }


    }

}
