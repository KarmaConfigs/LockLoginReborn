package ml.karmaconfigs.locklogin.api.modules.event.user;

import ml.karmaconfigs.locklogin.api.modules.client.Player;
import ml.karmaconfigs.locklogin.api.modules.event.util.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fire when a player is hook
 * after a plugin reload or if the plugin has
 * been loaded by a third party plugin loader.
 * <p>
 * This event is also fired on the first plugin start
 */
public final class UserHookEvent extends Event {

    private final Player player;
    private final Object eventObj;
    private boolean handled = false;

    /**
     * Initialize the event
     *
     * @param playerObject the player object
     * @param event        the event in where this event is fired
     */
    public UserHookEvent(final Player playerObject, final Object event) {
        player = playerObject;
        eventObj = event;
    }

    /**
     * Check if the event has been handled
     *
     * @return if the event has been handled
     */
    @Override
    public final boolean isHandled() {
        return handled;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     */
    @Override
    public final void setHandled(boolean status) {
        handled = true;
    }

    /**
     * Get the player
     *
     * @return the player
     */
    public final Player getPlayer() {
        return player;
    }

    /**
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public final @Nullable Object getEvent() {
        return eventObj;
    }
}