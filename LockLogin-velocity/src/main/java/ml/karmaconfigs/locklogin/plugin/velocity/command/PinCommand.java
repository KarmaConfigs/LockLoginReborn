package ml.karmaconfigs.locklogin.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import ml.karmaconfigs.api.common.Level;
import ml.karmaconfigs.api.velocity.Console;
import ml.karmaconfigs.locklogin.api.account.AccountManager;
import ml.karmaconfigs.locklogin.api.account.ClientSession;
import ml.karmaconfigs.locklogin.api.files.PluginConfiguration;
import ml.karmaconfigs.locklogin.api.encryption.CryptoUtil;
import ml.karmaconfigs.locklogin.api.utils.platform.CurrentPlatform;
import ml.karmaconfigs.locklogin.plugin.common.session.SessionDataContainer;
import ml.karmaconfigs.locklogin.plugin.velocity.command.util.BungeeLikeCommand;
import ml.karmaconfigs.locklogin.plugin.velocity.command.util.SystemCommand;
import ml.karmaconfigs.locklogin.plugin.velocity.plugin.sender.DataSender;
import ml.karmaconfigs.locklogin.plugin.velocity.plugin.sender.DataType;
import ml.karmaconfigs.locklogin.plugin.velocity.util.files.messages.Message;
import ml.karmaconfigs.locklogin.plugin.velocity.util.player.User;

import static ml.karmaconfigs.locklogin.plugin.velocity.LockLogin.plugin;
import static ml.karmaconfigs.locklogin.plugin.velocity.LockLogin.properties;

@SystemCommand(command = "pin")
public class PinCommand extends BungeeLikeCommand {

    /**
     * Initialize the bungee like command
     *
     * @param label the command label
     */
    public PinCommand(String label) {
        super(label);
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSource sender, String[] args) {
        PluginConfiguration config = CurrentPlatform.getConfiguration();
        Message messages = new Message();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = new User(player);
            ClientSession session = user.getSession();
            AccountManager manager = user.getManager();

            if (session.isValid()) {
                if (config.enablePin()) {
                    if (session.isCaptchaLogged() && session.isLogged() && session.isTempLogged()) {
                        if (args.length == 0) {
                            user.send(messages.prefix() + messages.pinUsages());
                        } else {
                            switch (args[0].toLowerCase()) {
                                case "setup":
                                    if (args.length == 2) {
                                        if (manager.getPin().replaceAll("\\s", "").isEmpty()) {
                                            String pin = args[1];

                                            manager.setPin(pin);
                                            user.send(messages.prefix() + messages.pinSet());

                                            session.setPinLogged(false);

                                            DataSender.send(player, DataSender.getBuilder(DataType.PIN, DataSender.CHANNEL_PLAYER).addTextData("open").build());

                                            SessionDataContainer.setLogged(SessionDataContainer.getLogged() - 1);
                                        } else {
                                            user.send(messages.prefix() + messages.alreadyPin());
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.setPin());
                                    }
                                    break;
                                case "remove":
                                    if (args.length == 2) {
                                        if (manager.getPin().replaceAll("\\s", "").isEmpty()) {
                                            user.send(messages.prefix() + messages.noPin());
                                        } else {
                                            String current = args[1];

                                            CryptoUtil util = CryptoUtil.getBuilder().withPassword(current).withToken(manager.getPin()).build();
                                            if (util.validate()) {
                                                manager.setPin(null);
                                                user.send(messages.prefix() + messages.pinReseted());
                                            } else {
                                                user.send(messages.prefix() + messages.incorrectPin());
                                            }
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.resetPin());
                                    }
                                    break;
                                case "change":
                                    if (args.length == 3) {
                                        if (manager.getPin().replaceAll("\\s", "").isEmpty()) {
                                            user.send(messages.prefix() + messages.noPin());
                                        } else {
                                            String current = args[1];
                                            String newPin = args[2];

                                            CryptoUtil util = CryptoUtil.getBuilder().withPassword(current).withToken(manager.getPin()).build();
                                            if (util.validate()) {
                                                manager.setPin(newPin);
                                                user.send(messages.prefix() + messages.pinChanged());
                                            } else {
                                                user.send(messages.prefix() + messages.incorrectPin());
                                            }
                                        }
                                    } else {
                                        user.send(messages.prefix() + messages.changePin());
                                    }
                                    break;
                                default:
                                    user.send(messages.prefix() + messages.pinUsages());
                                    break;
                            }
                        }
                    }
                } else {
                    user.send(messages.prefix() + messages.pinDisabled());
                }
            } else {
                user.send(messages.prefix() + properties.getProperty("session_not_valid", "&5&oYour session is invalid, try leaving and joining the server again"));
            }
        } else {
            Console.send(plugin, properties.getProperty("only_console_pin", "&5&oThe console can't have a pin!"), Level.INFO);
        }
    }
}