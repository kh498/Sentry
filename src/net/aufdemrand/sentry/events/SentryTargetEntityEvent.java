package net.aufdemrand.sentry.events;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Call forceTarget(true) to force the attacking of this target, note that
 * <ul>
 * <li>The target <i>might</i> not be the closest target out there</li>
 * <li>It will only ignore the ignore list. The entity will still have to pass checks such as LOS and night vision</li>
 * </ul>
 * Cancel the event to ignore this target
 *
 * @author kh498
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class SentryTargetEntityEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity target;
    private final NPC npc;
    private final boolean isRetaliating;
    private boolean isCancelled = false;

    public SentryTargetEntityEvent(final NPC npc, final LivingEntity target, final boolean isRetaliating) {
        this.npc = npc;
        this.target = target;
        this.isRetaliating = isRetaliating;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    public LivingEntity getTarget() {
        return this.target;
    }
    public NPC getNpc() {
        return this.npc;
    }
    public boolean isRetaliating() {
        return this.isRetaliating;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }
    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.isCancelled = cancel;
    }
}
