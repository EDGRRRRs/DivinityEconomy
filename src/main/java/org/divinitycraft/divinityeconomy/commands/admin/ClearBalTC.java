package org.divinitycraft.divinityeconomy.commands.admin;

import org.divinitycraft.divinityeconomy.DEPlugin;
import org.divinitycraft.divinityeconomy.commands.DivinityCommandTC;
import org.divinitycraft.divinityeconomy.config.Setting;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Tab completer for the clearbal command.
 */
public class ClearBalTC extends DivinityCommandTC {

    /**
     * Constructor
     *
     * @param app
     */
    public ClearBalTC(DEPlugin app) {
        super(app, "clearbal", true, Setting.COMMAND_CLEAR_BALANCE_ENABLE_BOOLEAN);
    }

    /**
     * For handling a player calling this command
     *
     * @param sender
     * @param args
     * @return
     */
    @Override
    public List<String> onPlayerTabCompleter(Player sender, String[] args) {
        String[] playerNames;
        switch (args.length) {
            // 1 args
            // return names of players starting with arg
            case 1:
                playerNames = getMain().getPlayMan().getPlayerNames(args[0]);
                break;

            default:
                playerNames = new String[0];
                break;
        }

        return Arrays.asList(playerNames);
    }

    /**
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public List<String> onConsoleTabCompleter(String[] args) {
        return this.onPlayerTabCompleter(null, args);
    }
}
