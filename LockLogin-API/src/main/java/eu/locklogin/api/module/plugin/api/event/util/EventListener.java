package eu.locklogin.api.module.plugin.api.event.util;

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

import eu.locklogin.api.module.PluginModule;
import eu.locklogin.api.module.plugin.javamodule.ModulePlugin;

/**
 * LockLogin advanced module event handler
 */
public interface EventListener {

    /**
     * Register the event listener
     *
     * @param module the module owner
     */
    default void register(final PluginModule module) {
        ModulePlugin manager = new ModulePlugin(module);
        manager.registerListener(this);
    }

    /**
     * Unregister the event listener
     *
     * @param module the module owner
     */
    default void unregister(final PluginModule module) {
        ModulePlugin manager = new ModulePlugin(module);
        manager.unregisterListener(this);
    }
}
