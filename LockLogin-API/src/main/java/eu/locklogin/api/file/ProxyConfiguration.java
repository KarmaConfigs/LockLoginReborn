package eu.locklogin.api.file;

import java.util.List;
import java.util.UUID;

/**
 * Proxy configuration
 */
public abstract class ProxyConfiguration {

    /**
     * Get if the proxy has multiple bungeecord
     * instances
     *
     * @return if the server has multiple bungeecord instances
     */
    public abstract boolean multiBungee();

    /**
     * Get if the player should be sent to the
     * servers in lobby servers or auth servers
     *
     * @return if the servers are enabled
     */
    public abstract boolean sendToServers();

    /**
     * Get the proxy key used to register this
     * proxy instance
     *
     * @return the proxy key
     */
    public abstract String proxyKey();

    /**
     * Get the proxy ID
     *
     * @return the proxy server ID
     */
    public abstract UUID getProxyID();

    /**
     * Get the proxy channel address
     *
     * @return the proxy channel address
     */
    public abstract String messageAddress();

    /**
     * Get the proxy channel port
     *
     * @return the proxy channel port
     */
    public abstract int messagePort();

    /**
     * Get all the lobby servers
     *
     * @param <T>      the server type
     * @param instance the server class instance
     * @return all the available lobby servers
     */
    public abstract <T> List<T> lobbyServers(final Class<T> instance);

    /**
     * Get all the auth servers
     *
     * @param <T>      the server type
     * @param instance the server class instance
     * @return all the available auth servers
     */
    public abstract <T> List<T> authServer(final Class<T> instance);
}
