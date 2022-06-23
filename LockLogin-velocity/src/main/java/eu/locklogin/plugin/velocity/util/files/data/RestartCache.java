package eu.locklogin.plugin.velocity.util.files.data;

/*
 * Private GSA code
 *
 * The use of this code
 * without GSA team authorization
 * will be a violation of
 * terms of use determined
 * in <a href="http://karmaconfigs.cf/license/"> here </a>
 * or (fallback domain) <a href="https://karmaconfigs.github.io/page/license"> here </a>
 */

import com.velocitypowered.api.proxy.Player;
import eu.locklogin.api.account.ClientSession;
import eu.locklogin.api.common.JarManager;
import eu.locklogin.plugin.velocity.util.player.User;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.KarmaElement;
import ml.karmaconfigs.api.common.karma.file.element.KarmaObject;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.api.common.utils.string.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static eu.locklogin.plugin.velocity.LockLogin.*;

public final class RestartCache {

    private final KarmaMain cache = new KarmaMain(plugin, "plugin.cache", "plugin", "updater", "cache");

    /**
     * Store the sessions into the cache file
     */
    public void storeUserData() {
        if (!cache.exists())
            cache.create();

        Map<UUID, ClientSession> sessions = User.getSessionMap();
        String sessions_serialized = StringUtils.serialize(sessions);

        if (sessions_serialized != null) {
            cache.set("sessions", new KarmaObject(sessions_serialized));
        } else {
            console.send(properties.getProperty("plugin_error_cache_save", "Failed to save cache object {0} ( {1} )"), Level.GRAVE, "sessions", "sessions are null");
        }

        cache.save();
    }

    /**
     * Load the stored sessions
     */
    public void loadUserData() {
        if (cache.exists()) {
            KarmaElement sessions_serialized = cache.get("sessions");

            if (sessions_serialized != null && sessions_serialized.isString()) {
                Map<UUID, ClientSession> sessions = StringUtils.loadUnsafe(sessions_serialized.getObjet().getString());
                Map<UUID, ClientSession> fixedSessions = new HashMap<>();
                if (sessions != null) {
                    //Remove offline player sessions to avoid security issues
                    for (UUID id : sessions.keySet()) {
                        ClientSession session = sessions.getOrDefault(id, null);
                        if (session != null) {
                            Optional<Player> player = plugin.getServer().getPlayer(id);
                            if (player.isPresent() && player.get().isActive()) {
                                fixedSessions.put(id, session);
                            }
                        }
                    }

                    try {
                        JarManager.changeField(User.class, "sessions", fixedSessions);
                    } catch (Throwable ex) {
                        console.send(properties.getProperty("plugin_error_cache_load", "Failed to load cache object {0} ( {1} )"), Level.GRAVE, "sessions", ex.fillInStackTrace());
                    }
                } else {
                    console.send(properties.getProperty("plugin_error_cache_load", "Failed to load cache object {0} ( {1} )"), Level.GRAVE, "sessions", "session map is null");
                }
            }
        }
    }

    /**
     * Remove the cache file
     */
    public void remove() {
        try {
            cache.delete();
        } catch (Throwable ignored) {
        }
    }
}
