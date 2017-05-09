package net.aufdemrand.sentry;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
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

class DenizenHook {

    private static boolean DenizenActive = false;
    static Plugin DenizenPlugin;

    static boolean SentryDeath(final Set<Player> _myDamagers, final NPC npc) {
        if (!DenizenActive) { return false; }

        try {
            boolean a = false;
            boolean c = false;

            final net.aufdemrand.denizen.Denizen denizen = (Denizen) DenizenPlugin;

            final NpcDeathTrigger npcDeathTrigger = denizen.getTriggerRegistry().get(NpcDeathTrigger.class);
            final NpcDeathTriggerOwner npcDeathTriggerOwner =
                denizen.getTriggerRegistry().get(NpcDeathTriggerOwner.class);

            if (npc != null) { a = npcDeathTrigger.Die(_myDamagers, npc); }
            if (npc != null) { c = npcDeathTriggerOwner.Die(npc); }
            return (a || c);
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void setupDenizenHook() {

        //noinspection InstantiationOfUtilityClass
        final DenizenHook me = new DenizenHook();

        me.new NpcDeathTriggerOwner().activate().as("Npcdeathowner");
        me.new NpcDeathTrigger().activate().as("Npcdeath");

        final DieCommand dc = me.new DieCommand();
        final LiveCommand lc = me.new LiveCommand();

        dc.activate().as("die").withOptions("die", 0);
        lc.activate().as("live").withOptions("live", 0);

        DenizenActive = true;
    }

    static void DenizenAction(final NPC npc, final String action, final org.bukkit.OfflinePlayer player) {
        if (DenizenActive) {
            final dNPC dnpc = dNPC.mirrorCitizensNPC(npc);
            if (dnpc != null) {
                try {
                    dnpc.action(action, dPlayer.mirrorBukkitPlayer(player));
                } catch (final Exception e) {
                    //ignore
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
                    boolean removeAggression = false;

                    for (final String arg : theEntry.getArguments()) {
                        if (arg.equalsIgnoreCase("peace")) { removeAggression = true; }
                    }

                    String db = "RISE! " + ((BukkitScriptEntryData) theEntry.entryData).getNPC().getName() + "!";
                    if (removeAggression) { db += " ..And fight no more!"; }
                    dB.log(db);

                    if (inst != null) {
                        inst.setSentryStatus(net.aufdemrand.sentry.SentryInstance.Status.LOOKING);
                        if (removeAggression) { inst.clearTarget(); }
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

    private class NpcDeathTriggerOwner extends net.aufdemrand.denizen.scripts.triggers.AbstractTrigger {

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

    private class NpcDeathTrigger extends AbstractTrigger {

        boolean Die(final Set<Player> _myDamagers, final NPC npc) {

            // Check if NPC has triggers.
            if (!npc.hasTrait(TriggerTrait.class)) { return false; }

            // Check if trigger is enabled.
            if (!npc.getTrait(TriggerTrait.class).isEnabled(this.name)) {
                return false;
            }

            final dNPC theDenizen = dNPC.mirrorCitizensNPC(npc);

            dB.echoDebug(null, DebugElement.Header, "Parsing NPCDeath/Killers Trigger");

            boolean foundOne = false;

            for (final Player thePlayer : _myDamagers) {

                if (thePlayer != null && thePlayer.getLocation().distance(npc.getEntity().getLocation()) > 300) {
                    dB.echoDebug(null, DebugElement.Header, thePlayer.getName() + " is to far away.");
                    continue;
                }

                final InteractScriptContainer script =
                    theDenizen.getInteractScriptQuietly(dPlayer.mirrorBukkitPlayer(thePlayer), this.getClass());

                if (parse(theDenizen, dPlayer.mirrorBukkitPlayer(thePlayer), script)) { foundOne = true; }
            }

            return foundOne;
        }
        @Override
        public void onEnable() {
            // TODO Auto-generated method stub
        }


    }

}
