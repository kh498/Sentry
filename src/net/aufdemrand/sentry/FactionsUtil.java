package net.aufdemrand.sentry;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import org.bukkit.entity.Player;

class FactionsUtil {

    static boolean isFactionEnemy(final String faction1, final String faction2) {
        if (!Sentry.FactionsActive) { return false; }
        if (faction1.equalsIgnoreCase(faction2)) { return false; }
        try {

            final Faction f1 = FactionColl.get().getByName(faction1);
            final Faction f2 = FactionColl.get().getByName(faction2);

            return f1.getRelationTo(f2) == com.massivecraft.factions.Rel.ENEMY;

        } catch (final Exception e) {
            return false;
        }
    }

    static String getFactionsTag(final Player player) {
        if (!Sentry.FactionsActive) { return null; }
        try {
            return com.massivecraft.factions.entity.MPlayer.get(player).getFactionName();
        } catch (final Exception e) {
            return null;
        }
    }

}