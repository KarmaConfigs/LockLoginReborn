package eu.locklogin.api.module.plugin.api.event.plugin;

/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */

import eu.locklogin.api.account.AccountManager;
import eu.locklogin.api.module.plugin.api.event.util.Event;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when a migration is started
 * between two accounts
 * <p>
 * So if a module has a custom migration method, it can be
 * handled by it using this event.
 */
public final class MigrationRequestEvent extends Event {

    private final AccountManager source;
    private final AccountManager target;
    private final Object eventObj;

    private boolean handled = false;
    private String handleReason = "";

    /**
     * Initialize event
     *
     * @param sourceAccount the source account
     * @param targetAccount the target account
     * @param event         the event in where this event is fired
     */
    public MigrationRequestEvent(final AccountManager sourceAccount, final AccountManager targetAccount, final Object event) {
        source = sourceAccount;
        target = targetAccount;
        eventObj = event;
    }

    /**
     * Get the source account
     *
     * @return the source account
     */
    public AccountManager getSource() {
        return source;
    }

    /**
     * Get the target account
     *
     * @return the target account
     */
    public AccountManager getTarget() {
        return target;
    }

    /**
     * Get if the event is handleable or not
     *
     * @return if the event is handleable
     */
    @Override
    public boolean isHandleable() {
        return false;
    }

    /**
     * Check if the event has been handled
     *
     * @return if the event has been handled
     */
    @Override
    public boolean isHandled() {
        return isHandleable() && handled;
    }

    /**
     * Get the reason of why the event has been
     * marked as handled
     *
     * @return the event handle reason
     */
    @Override
    public String getHandleReason() {
        return handleReason;
    }

    /**
     * Set the event handle status
     *
     * @param status the handle status
     * @param reason the handle reason
     */
    public void setHandled(final boolean status, final String reason) {
        handled = status;
        handleReason = reason;
    }

    /**
     * Get the event instance
     *
     * @return the event instance
     */
    @Override
    public @Nullable Object getEvent() {
        return eventObj;
    }
}
