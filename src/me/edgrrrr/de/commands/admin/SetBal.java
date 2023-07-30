package me.edgrrrr.de.commands.admin;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommand;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.utils.Converter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * A command for setting the balance of a player
 */
public class SetBal extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public SetBal(DEPlugin app) {
        super(app, "setbal", true, Setting.COMMAND_SET_BALANCE_ENABLE_BOOLEAN);
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
        // command <amount> - applies amount to self
        // command <player> <amount> - applies amount to player
        OfflinePlayer receiver;
        double amount;

        switch (args.length) {
            case 1:
                // use case #1
                receiver = sender;
                amount = Converter.getDouble(args[0]);
                break;

            case 2:
                // use case #2
                amount = Converter.getDouble(args[1]);
                receiver = this.getMain().getPlayMan().getPlayer(args[0], false);
                break;

            default:
                // Incorrect number of args
                this.getMain().getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // Ensure to player exists
        if (receiver == null) {
            this.getMain().getConsole().send(sender, CommandResponse.InvalidPlayerName.defaultLogLevel, CommandResponse.InvalidPlayerName.message);
            return true;
        }

        double startingBalance = this.getMain().getEconMan().getBalance(receiver);
        EconomyResponse response = this.getMain().getEconMan().setCash(receiver, amount);

        // Response messages
        if (response.transactionSuccess()) {
            // Handles console, player and mail
            this.getMain().getConsole().logBalance(sender, receiver, startingBalance, response.balance, String.format("%s set your balance", sender.getName()));

        } else {
            // Handles console, player and mail
            this.getMain().getConsole().logFailedBalance(sender, receiver, response.errorMessage);
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
        // Use case scenarios
        // command <amount> - applies amount to self
        // command <player> <amount> - applies amount to player
        OfflinePlayer receiver;
        double amount;

        switch (args.length) {
            case 2:
                // use case #2
                amount = Converter.getDouble(args[1]);
                receiver = this.getMain().getPlayMan().getPlayer(args[0], false);
                break;

            default:
                // Incorrect number of args
                this.getMain().getConsole().usage(CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // Ensure to player exists
        if (receiver == null) {
            this.getMain().getConsole().send(CommandResponse.InvalidPlayerName.defaultLogLevel, CommandResponse.InvalidPlayerName.message);
            return true;
        }

        double startingBalance = this.getMain().getEconMan().getBalance(receiver);
        EconomyResponse response = this.getMain().getEconMan().setCash(receiver, amount);

        // Response messages
        if (response.transactionSuccess()) {
            // Handles console, player and mail
            this.getMain().getConsole().logBalance(null, receiver, startingBalance, response.balance, "CONSOLE set your balance");

        } else {
            // Handles console, player and mail
            this.getMain().getConsole().logFailedBalance(null, receiver, response.errorMessage);
        }
        return true;
    }
}

