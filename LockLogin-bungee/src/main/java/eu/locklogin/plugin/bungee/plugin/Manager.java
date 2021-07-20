package eu.locklogin.plugin.bungee.plugin;

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
import eu.locklogin.api.account.ClientSession;
import eu.locklogin.api.common.security.client.ClientData;
import eu.locklogin.api.common.security.client.ProxyCheck;
import eu.locklogin.api.common.session.Session;
import eu.locklogin.api.common.session.SessionCheck;
import eu.locklogin.api.common.session.SessionDataContainer;
import eu.locklogin.api.common.session.SessionKeeper;
import eu.locklogin.api.common.utils.DataType;
import eu.locklogin.api.common.utils.filter.ConsoleFilter;
import eu.locklogin.api.common.utils.filter.PluginFilter;
import eu.locklogin.api.common.utils.other.ASCIIArtGenerator;
import eu.locklogin.api.common.utils.plugin.ServerDataStorage;
import eu.locklogin.api.common.web.AlertSystem;
import eu.locklogin.api.common.web.STFetcher;
import eu.locklogin.api.common.web.VersionDownloader;
import eu.locklogin.api.file.PluginConfiguration;
import eu.locklogin.api.file.PluginMessages;
import eu.locklogin.api.file.ProxyConfiguration;
import eu.locklogin.api.module.LoadRule;
import eu.locklogin.api.module.plugin.api.event.user.UserHookEvent;
import eu.locklogin.api.module.plugin.api.event.user.UserUnHookEvent;
import eu.locklogin.api.module.plugin.javamodule.ModulePlugin;
import eu.locklogin.api.util.platform.CurrentPlatform;
import eu.locklogin.plugin.bungee.LockLogin;
import eu.locklogin.plugin.bungee.Main;
import eu.locklogin.plugin.bungee.command.util.SystemCommand;
import eu.locklogin.plugin.bungee.listener.ChatListener;
import eu.locklogin.plugin.bungee.listener.JoinListener;
import eu.locklogin.plugin.bungee.listener.MessageListener;
import eu.locklogin.plugin.bungee.listener.QuitListener;
import eu.locklogin.plugin.bungee.permissibles.PluginPermission;
import eu.locklogin.plugin.bungee.plugin.sender.DataSender;
import eu.locklogin.plugin.bungee.util.ServerLifeChecker;
import eu.locklogin.plugin.bungee.util.files.Config;
import eu.locklogin.plugin.bungee.util.files.Message;
import eu.locklogin.plugin.bungee.util.files.Proxy;
import eu.locklogin.plugin.bungee.util.files.client.PlayerFile;
import eu.locklogin.plugin.bungee.util.files.data.RestartCache;
import eu.locklogin.plugin.bungee.util.files.data.lock.LockedAccount;
import eu.locklogin.plugin.bungee.util.player.User;
import ml.karmaconfigs.api.common.Console;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.timer.SourceSecondsTimer;
import ml.karmaconfigs.api.common.timer.scheduler.SimpleScheduler;
import ml.karmaconfigs.api.common.utils.FileUtilities;
import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.api.common.version.VersionCheckType;
import ml.karmaconfigs.api.common.version.VersionUpdater;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static eu.locklogin.plugin.bungee.LockLogin.*;
import static eu.locklogin.plugin.bungee.plugin.sender.DataSender.*;
import static ml.karmaconfigs.api.common.Console.Colors.YELLOW_BRIGHT;

public final class Manager {

    private static int changelog_requests = 0;
    private static int updater_id = 0;
    private static int alert_id = 0;

    public static void initialize() {
        int size = 10;
        String character = "*";
        try {
            size = Integer.parseInt(properties.getProperty("ascii_art_size", "10"));
            character = properties.getProperty("ascii_art_character", "*").substring(0, 1);
        } catch (Throwable ignored) {
        }

        System.out.println();
        artGen.print(YELLOW_BRIGHT, "LockLogin", size, ASCIIArtGenerator.ASCIIArtFont.ART_FONT_SANS_SERIF, character);
        Console.send("&eversion:&6 {0}", versionID.getVersionID());
        Console.send("&eSpecial thanks: &7" + STFetcher.getDonors());

        ProxyCheck.scan();

        PlayerFile.migrateV1();
        PlayerFile.migrateV2();
        PlayerFile.migrateV3();

        setupFiles();
        registerCommands();
        registerListeners();

        Console.send(" ");
        Console.send("&e-----------------------");

        if (!CurrentPlatform.isValidAccountManager()) {
            CurrentPlatform.setAccountsManager(PlayerFile.class);
            Console.send(plugin, "Loaded native player account manager", Level.INFO);
        } else {
            Console.send(plugin, "Loaded custom player account manager", Level.INFO);
        }
        if (!CurrentPlatform.isValidSessionManager()) {
            CurrentPlatform.setSessionManager(Session.class);
            Console.send(plugin, "Loaded native player session manager", Level.INFO);
        } else {
            Console.send(plugin, "Loaded custom player session manager", Level.INFO);
        }

        loadCache();

        plugin.getProxy().registerChannel(DataSender.CHANNEL_PLAYER);
        plugin.getProxy().registerChannel(DataSender.PLUGIN_CHANNEL);
        plugin.getProxy().registerChannel(DataSender.ACCESS_CHANNEL);

        AccountManager manager = CurrentPlatform.getAccountManager(null);
        if (manager != null) {
            Set<AccountManager> accounts = manager.getAccounts();
            Set<AccountManager> nonLocked = new HashSet<>();
            for (AccountManager account : accounts) {
                LockedAccount locked = new LockedAccount(account.getUUID());
                if (!locked.getData().isLocked())
                    nonLocked.add(account);
            }

            SessionDataContainer.setRegistered(nonLocked.size());

            SessionDataContainer.onDataChange(data -> {
                try {
                    Collection<ServerInfo> servers = plugin.getProxy().getServers().values();

                    switch (data.getDataType()) {
                        case LOGIN:
                            for (ServerInfo server : servers) {
                                DataSender.send(server, DataSender.getBuilder(DataType.LOGGED, DataSender.PLUGIN_CHANNEL, null).addIntData(SessionDataContainer.getLogged()).build());
                            }
                            break;
                        case REGISTER:
                            for (ServerInfo server : servers) {
                                DataSender.send(server, DataSender.getBuilder(DataType.REGISTERED, DataSender.PLUGIN_CHANNEL, null).addIntData(SessionDataContainer.getRegistered()).build());
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                }
            });
        }

        PluginConfiguration config = CurrentPlatform.getConfiguration();
        performVersionCheck();
        if (config.getUpdaterOptions().isEnabled()) {
            scheduleVersionCheck();
        }
        registerMetrics();

        scheduleAlertSystem();
        initPlayers();

        CurrentPlatform.setPrefix(config.getModulePrefix());

        ServerLifeChecker checker = new ServerLifeChecker();
        checker.startCheck();

        File[] moduleFiles = LockLogin.getLoader().getDataFolder().listFiles();
        if (moduleFiles != null) {
            List<File> files = Arrays.asList(moduleFiles);
            Iterator<File> iterator = files.iterator();
            do {
                File file = iterator.next();
                if (file.isFile()) {
                    LockLogin.getLoader().loadModule(file, LoadRule.POSTPLUGIN);
                }
            } while (iterator.hasNext());
        }
    }

    public static void terminate() {
        try {
            Console.send(plugin, "Finalizing console filter, please wait", Level.INFO);
            Logger coreLogger = (Logger) LogManager.getRootLogger();

            Iterator<Filter> filters = coreLogger.getFilters();
            if (filters != null) {
                while (filters.hasNext()) {
                    Filter filter = filters.next();
                    if (filter.getClass().isAnnotationPresent(PluginFilter.class))
                        filter.stop();
                }
            }
        } catch (Throwable ignored) {
        }

        int size = 10;
        String character = "*";
        try {
            size = Integer.parseInt(properties.getProperty("ascii_art_size", "10"));
            character = properties.getProperty("ascii_art_character", "*").substring(0, 1);
        } catch (Throwable ignored) {
        }

        System.out.println();
        artGen.print(ml.karmaconfigs.api.common.Console.Colors.RED_BRIGHT, "LockLogin", size, ASCIIArtGenerator.ASCIIArtFont.ART_FONT_SANS_SERIF, character);
        Console.send("&eversion:&6 {0}", versionID.getVersionID());
        Console.send(" ");
        Console.send("&e-----------------------");

        endPlayers();
    }

    /**
     * Register plugin commands
     */
    protected static void registerCommands() {
        Set<String> unregistered = new LinkedHashSet<>();
        Set<String> registered = new HashSet<>();

        for (Class<?> clazz : SystemCommand.manager.recognizedClasses()) {
            if (clazz.isAnnotationPresent(SystemCommand.class)) {
                try {
                    String command = SystemCommand.manager.getDeclaredCommand(clazz);

                    if (command != null && !command.replaceAll("\\s", "").isEmpty()) {
                        Object instance = clazz.getDeclaredConstructor(String.class).newInstance(command);

                        if (instance instanceof Command) {
                            Command executor = (Command) instance;
                            plugin.getProxy().getPluginManager().registerCommand(plugin, executor);
                            registered.add("/" + command);
                        } else {
                            unregistered.add(command);
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!unregistered.isEmpty()) {
            Console.send(plugin, properties.getProperty("command_register_problem", "Failed to register command(s): {0}"), Level.GRAVE, setToString(unregistered));
            Console.send(plugin, properties.getProperty("plugin_error_disabling", "Disabling plugin due an internal error"), Level.INFO);
        } else {
            Console.send(plugin, properties.getProperty("plugin_filter_initialize", "Initializing console filter to protect user data"), Level.INFO);

            try {
                ConsoleFilter filter = new ConsoleFilter(registered);

                Logger coreLogger = (Logger) LogManager.getRootLogger();
                coreLogger.addFilter(filter);
            } catch (Throwable ex) {
                Console.send(plugin, "LockLogin tried to hook into console filter, but as expected, BungeeCord or this BungeeCord fork doesn't has a valid logger, please do not report the commands are being shown in console", Level.GRAVE);
            }
        }
    }

    /**
     * Setup the plugin files
     */
    protected static void setupFiles() {
        Set<String> failed = new LinkedHashSet<>();

        File cfg = new File(plugin.getDataFolder(), "config.yml");
        File proxy = new File(plugin.getDataFolder(), "proxy.yml");

        FileCopy config_copy = new FileCopy(plugin, "cfg/config.yml");
        FileCopy proxy_copy = new FileCopy(plugin, "cfg/proxy.yml");
        try {
            config_copy.copy(cfg);
        } catch (Throwable ex) {
            failed.add("config.yml");
        }
        try {
            proxy_copy.copy(proxy);
        } catch (Throwable ex) {
            failed.add("proxy.yml");
        }

        Config config = new Config();
        Proxy proxy_cfg = new Proxy();
        CurrentPlatform.setConfigManager(config);
        CurrentPlatform.setProxyManager(proxy_cfg);

        String country = config.getLang().country(config.getLangName());
        File msg_file = new File(plugin.getDataFolder() + File.separator + "lang" + File.separator + "v2", "messages_" + country + ".yml");

        InputStream internal = Main.class.getResourceAsStream("/lang/messages_" + country + ".yml");
        //Check if the file exists inside the plugin as an official language
        if (internal != null) {
            if (!msg_file.exists()) {
                FileCopy copy = new FileCopy(plugin, "lang/messages_" + country + ".yml");

                try {
                    copy.copy(msg_file);
                } catch (Throwable ex) {
                    failed.add(msg_file.getName());
                }
            }
        } else {
            if (!msg_file.exists()) {
                failed.add(msg_file.getName());
                Console.send(plugin, "Could not find community message pack named {0} in lang_v2 folder, using messages english as default", Level.GRAVE, msg_file.getName());

                msg_file = new File(plugin.getDataFolder() + File.separator + "lang" + File.separator + "v2", "messages_en.yml");

                if (!msg_file.exists()) {
                    FileCopy copy = new FileCopy(plugin, "lang/messages_en.yml");

                    try {
                        copy.copy(msg_file);
                    } catch (Throwable ex) {
                        failed.add(msg_file.getName());
                    }
                }
            } else {
                Console.send(plugin, "Detected community language pack, please make sure this pack is updated to avoid translation errors", Level.WARNING);
            }
        }

        if (!failed.isEmpty()) {
            Console.send(plugin, properties.getProperty("file_register_problem", "Failed to setup/check file(s): {0}. The plugin will use defaults, you can try to create files later by running /locklogin reload"), Level.WARNING, setToString(failed));
        }

        Message messages = new Message();

        Config.manager.checkValues();
        CurrentPlatform.setPluginMessages(messages);
    }

    /**
     * Register plugin metrics
     */
    protected static void registerMetrics() {
        PluginConfiguration config = CurrentPlatform.getConfiguration();
        Metrics metrics = new Metrics(plugin, 6512);

        metrics.addCustomChart(new SimplePie("used_locale", () -> config.getLang().friendlyName(config.getLangName())));
        metrics.addCustomChart(new SimplePie("clear_chat", () -> String.valueOf(config.clearChat())
                .replace("true", "Clear chat")
                .replace("false", "Don't clear chat")));
        metrics.addCustomChart(new SimplePie("sessions_enabled", () -> String.valueOf(config.enableSessions())
                .replace("true", "Sessions enabled")
                .replace("false", "Sessions disabled")));
    }

    /**
     * Register the plugin listeners
     */
    protected static void registerListeners() {
        Listener onJoin = new JoinListener();
        Listener onQuit = new QuitListener();
        Listener onChat = new ChatListener();
        Listener onMessage = new MessageListener();

        plugin.getProxy().getPluginManager().registerListener(plugin, onJoin);
        plugin.getProxy().getPluginManager().registerListener(plugin, onQuit);
        plugin.getProxy().getPluginManager().registerListener(plugin, onChat);
        plugin.getProxy().getPluginManager().registerListener(plugin, onMessage);
    }

    /**
     * Load the plugin cache if exists
     */
    protected static void loadCache() {
        RestartCache cache = new RestartCache();
        cache.loadBungeeKey();
        cache.loadUserData();

        cache.remove();
    }

    /**
     * Perform a version check
     */
    protected static void performVersionCheck() {
        VersionUpdater updater = VersionUpdater.createNewBuilder(plugin).withVersionType(VersionCheckType.RESOLVABLE_ID).withVersionResolver(versionID).build();
        updater.fetch(true).whenComplete((fetch, trouble) -> {
            if (trouble == null) {
                if (!fetch.isUpdated()) {
                    if (changelog_requests <= 0) {
                        changelog_requests = 3;

                        Console.send(plugin, "LockLogin is outdated! Current version is {0} but latest is {1}", Level.INFO, versionID.getVersionID(), fetch.getLatest());
                        for (String line : fetch.getChangelog())
                            Console.send(line);

                        PluginMessages messages = CurrentPlatform.getMessages();
                        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                            User user = new User(player);
                            if (user.hasPermission(PluginPermission.applyUpdates())) {
                                user.send(messages.prefix() + "&dNew LockLogin version available, current is " + versionID.getVersionID() + ", but latest is " + fetch.getLatest());
                                user.send(messages.prefix() + "&dRun /locklogin changelog to view the list of changes");
                            }
                        }

                        if (VersionDownloader.downloadUpdates()) {
                            if (!VersionDownloader.isDownloading()) {
                                VersionDownloader downloader = new VersionDownloader(fetch);
                                downloader.download().whenComplete((file, error) -> {
                                    if (error != null) {
                                        logger.scheduleLog(Level.GRAVE, error);
                                        logger.scheduleLog(Level.INFO, "Failed to download latest LockLogin instance");
                                        Console.send(plugin, properties.getProperty("updater_download_fail", "Failed to download latest LockLogin update ( {0} )"), Level.INFO, error.fillInStackTrace());

                                        try {
                                            Files.deleteIfExists(file.toPath());
                                        } catch (Throwable ignored) {
                                        }
                                    } else {
                                        Console.send(plugin, properties.getProperty("updater_downloaded", "Downloaded latest version plugin instance, to apply the updates run /locklogin applyUpdates"), Level.INFO);

                                        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                                            User user = new User(player);
                                            if (user.hasPermission(PluginPermission.applyUpdates())) {
                                                user.send(messages.prefix() + properties.getProperty("updater_downloaded", "Downloaded latest version plugin instance, to apply the updates run /locklogin applyUpdates"));
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            Console.send(plugin, "LockLogin auto download is disabled, you must download latest LockLogin version from {0}", Level.GRAVE, fetch.getUpdateURL());

                            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                                User user = new User(player);
                                if (user.hasPermission(PluginPermission.applyUpdates())) {
                                    user.send(messages.prefix() + "&dFollow console instructions to update");
                                }
                            }
                        }
                    } else {
                        changelog_requests--;
                    }
                }
            } else {
                logger.scheduleLog(Level.GRAVE, trouble);
                logger.scheduleLog(Level.INFO, "Failed to check for updates");
            }
        });
    }

    /**
     * Schedule the version check process
     */
    protected static void scheduleVersionCheck() {
        PluginConfiguration config = CurrentPlatform.getConfiguration();

        SimpleScheduler timer = new SourceSecondsTimer(plugin, config.getUpdaterOptions().getInterval(), true).multiThreading(true).endAction(Manager::performVersionCheck);
        if (config.getUpdaterOptions().isEnabled()) {
            timer.start();
        } else {
            performVersionCheck();
        }

        updater_id = timer.getId();

    }

    /**
     * Schedule the alert system
     */
    protected static void scheduleAlertSystem() {
        SimpleScheduler timer = new SourceSecondsTimer(plugin, 30, true).multiThreading(true).endAction(() -> {
            AlertSystem system = new AlertSystem();
            system.checkAlerts();

            if (system.available())
                Console.send(system.getMessage());
        });
        timer.start();

        alert_id = timer.getId();

    }

    /**
     * Initialize already connected players
     *
     * This is util after plugin updates or
     * plugin load using third-party loaders
     */
    protected static void initPlayers() {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            PluginConfiguration config = CurrentPlatform.getConfiguration();
            PluginMessages messages = CurrentPlatform.getMessages();

            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                plugin.getProxy().getScheduler().schedule(plugin, () -> {
                    InetSocketAddress ip = getSocketIp(player.getSocketAddress());
                    User user = new User(player);
                    if (ip != null) {
                        ClientData client = new ClientData(ip.getAddress());

                        if (!client.isVerified())
                            client.setVerified(true);

                        if (!client.canAssign(config.accountsPerIP(), player.getName(), player.getUniqueId())) {
                            user.kick(StringUtils.toColor(messages.maxIP()));
                            return;
                        }
                    }

                    Server server = player.getServer();
                    if (server != null) {
                        ServerInfo info = server.getInfo();
                        ProxyConfiguration proxy = CurrentPlatform.getProxyConfiguration();

                        if (ServerDataStorage.needsRegister(info.getName()) || ServerDataStorage.needsProxyKnowledge(info.getName())) {
                            if (ServerDataStorage.needsRegister(info.getName()))
                                DataSender.send(info, DataSender.getBuilder(DataType.KEY, ACCESS_CHANNEL, player).addTextData(proxy.proxyKey()).addTextData(info.getName()).addBoolData(proxy.multiBungee()).build());

                            if (ServerDataStorage.needsProxyKnowledge(info.getName())) {
                                DataSender.send(info, DataSender.getBuilder(DataType.REGISTER, ACCESS_CHANNEL, player).addTextData(proxy.proxyKey()).addTextData(info.getName()).build());
                            }
                        }
                    }

                    DataSender.send(player, DataSender.getBuilder(DataType.MESSAGES, PLUGIN_CHANNEL, player).addTextData(CurrentPlatform.getMessages().toString()).build());
                    DataSender.send(player, DataSender.getBuilder(DataType.CONFIG, PLUGIN_CHANNEL, player).addTextData(Config.manager.getConfiguration()).build());
                    CurrentPlatform.requestDataContainerUpdate();

                    DataSender.MessageData validation = getBuilder(DataType.VALIDATION, DataSender.CHANNEL_PLAYER, player).build();
                    DataSender.send(player, validation);

                    ProxyCheck proxy = new ProxyCheck(ip);
                    if (proxy.isProxy()) {
                        user.kick(messages.ipProxyError());
                        return;
                    }

                    user.applySessionEffects();

                    if (config.clearChat()) {
                        for (int i = 0; i < 150; i++)
                            plugin.getProxy().getScheduler().runAsync(plugin, () -> player.sendMessage(TextComponent.fromLegacyText("")));
                    }

                    ClientSession session = user.getSession();
                    session.validate();

                    if (!config.captchaOptions().isEnabled())
                        session.setCaptchaLogged(true);

                    SimpleScheduler tmp_timer = null;
                    if (!session.isCaptchaLogged()) {
                        tmp_timer = new SourceSecondsTimer(plugin, 1, true);
                        tmp_timer.secondChangeAction((second) -> player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(StringUtils.toColor(messages.captcha(session.getCaptcha()))))).start();
                    }

                    MessageData join = DataSender.getBuilder(DataType.JOIN, CHANNEL_PLAYER, player)
                            .addBoolData(session.isLogged())
                            .addBoolData(session.is2FALogged())
                            .addBoolData(session.isPinLogged())
                            .addBoolData(user.isRegistered()).build();
                    DataSender.send(player, join);

                    SimpleScheduler timer = tmp_timer;
                    SessionCheck<ProxiedPlayer> check = user.getChecker().whenComplete(() -> {
                        user.restorePotionEffects();
                        player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));

                        if (timer != null)
                            timer.cancel();
                    });

                    plugin.getProxy().getScheduler().runAsync(plugin, check);

                    user.checkServer(0);

                    UserHookEvent event = new UserHookEvent(fromPlayer(player), null);
                    ModulePlugin.callEvent(event);
                }, 2, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * Finalize connected players sessions
     *
     * This is util after plugin updates or
     * plugin unload using third-party loaders
     */
    protected static void endPlayers() {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            InetSocketAddress ip = getSocketIp(player.getSocketAddress());
            User user = new User(player);

            SessionKeeper keeper = new SessionKeeper(fromPlayer(player));
            keeper.store();

            if (ip != null) {
                ClientData data = new ClientData(ip.getAddress());
                data.removeClient(ClientData.getNameByID(player.getUniqueId()));

                ClientSession session = user.getSession();
                session.invalidate();
                session.setLogged(false);
                session.setPinLogged(false);
                session.set2FALogged(false);

                DataSender.send(player, DataSender.getBuilder(DataType.QUIT, DataSender.CHANNEL_PLAYER, player).build());
            }

            UserUnHookEvent event = new UserUnHookEvent(fromPlayer(player), null);
            ModulePlugin.callEvent(event);
        }
    }

    /**
     * Restart the version checker
     */
    public static void restartVersionChecker() {
        try {
            SimpleScheduler timer = new SourceSecondsTimer(plugin, updater_id);
            timer.restart();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Restart the alert system timer
     */
    public static void restartAlertSystem() {
        try {
            SimpleScheduler timer = new SourceSecondsTimer(plugin, alert_id);
            timer.restart();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Convert a set of strings into a single string
     *
     * @param set the set to convert
     * @return the converted set
     */
    private static String setToString(final Set<String> set) {
        StringBuilder builder = new StringBuilder();
        for (String str : set) {
            builder.append(str.replace(",", "comma")).append(", ");
        }

        return StringUtils.replaceLast(builder.toString(), ", ", "");
    }
}
