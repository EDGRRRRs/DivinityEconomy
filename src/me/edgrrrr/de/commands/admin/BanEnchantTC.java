package me.edgrrrr.de.commands.admin;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandTC;
import me.edgrrrr.de.config.Setting;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * A tab completer for the eset value command
 */
public class BanEnchantTC extends DivinityCommandTC {

    /**
     * Constructor
     *
     * @param app
     */
    public BanEnchantTC(DEPlugin app) {
        super(app, "banenchant", true, Setting.COMMAND_BAN_ENCHANT_ENABLE_BOOLEAN);
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
        return this.onConsoleTabCompleter(args);
    }

    /**
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public List<String> onConsoleTabCompleter(String[] args) {
        String[] strings;
        switch (args.length) {
            // Args 1
            // get player names that start with args[0]
            case 1:
                strings = getMain().getEnchMan().getItemNames(args[0]).toArray(new String[0]);
                break;

            case 2:
                strings = new String[]{"true", "false"};
                break;

            default:
                strings = new String[0];
                break;
        }

        return Arrays.asList(strings);
    }
}
