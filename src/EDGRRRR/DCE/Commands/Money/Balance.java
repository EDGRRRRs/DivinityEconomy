package edgrrrr.dce.commands.money;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import edgrrrr.dce.commands.DivinityCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * A command for getting player's balances
 */
public class Balance extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public Balance(DCEPlugin app) {
        super(app, "balance", true, Setting.COMMAND_BALANCE_ENABLE_BOOLEAN);
    }

    /**
     * For handling a player calling this command
     *
     * @param sender
     * @param args
     * @return
     */
    @Override
    public boolean onPlayerCommand(Player sender, String[] args) {
        // Use case scenarios
        // command - returns the callers balance.
        // command <username> - returns the usernames balance.
        OfflinePlayer receiverPlayer = null;

        switch (args.length) {
            case 1:
                // Get online player
                receiverPlayer = this.app.getServer().getPlayer(args[0]);
                // If they aren't online or don't exist. Do the dirty offline call.
                if (receiverPlayer == null) {
                    receiverPlayer = this.app.getPlayerManager().getOfflinePlayer(args[0], false);
                }
                break;

            default:
                // any number of args.. just return their own.
                receiverPlayer = sender;
                break;
        }

        if (receiverPlayer == null) {
            this.app.getConsole().usage(sender, Message.InvalidPlayerNameResponse.message, this.help.getUsages());
            return true;
        }

        double balance = this.app.getEconomyManager().getBalance(receiverPlayer);
        if (!(sender == receiverPlayer)) {
            this.app.getConsole().info(sender, String.format(Message.BalanceResponseOther.message, receiverPlayer.getName(), balance));
        } else {
            this.app.getConsole().info(sender, String.format(Message.BalanceResponse.message, balance));
        }
        return true;
    }

    /**
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public boolean onConsoleCommand(String[] args) {
        OfflinePlayer player;
        switch (args.length) {
            case 1:
                player = this.app.getServer().getPlayer(args[0]);
                if (player == null) {
                    this.app.getPlayerManager().getOfflinePlayer(args[0], false);
                }
                break;

            default:
                this.app.getConsole().send(Message.InvalidNumberOfArguments.defaultLogLevel, Message.InvalidNumberOfArguments.message);
                return true;
        }

        if (player == null) {
            this.app.getConsole().send(Message.InvalidPlayerNameResponse.defaultLogLevel, Message.InvalidPlayerNameResponse.message);
            return true;
        }

        this.app.getConsole().info(String.format(Message.BalanceResponseOther.message, player.getName(), this.app.getEconomyManager().getBalance(player)));
        return true;
    }
}
