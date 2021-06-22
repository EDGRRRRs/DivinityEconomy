package edgrrrr.de.commands.mail;

import edgrrrr.configapi.Setting;
import edgrrrr.de.DEPlugin;
import edgrrrr.de.commands.DivinityCommandTC;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * A tab completer for the clear mail command
 */
public class ClearMailTC extends DivinityCommandTC {

    /**
     * Constructor
     *
     * @param
     */
    public ClearMailTC(DEPlugin app) {
        super(app, false, Setting.COMMAND_CLEAR_MAIL_ENABLE_BOOLEAN);
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
        String[] strings;
        switch (args.length) {
            // 1 arg
            // return list of page numbers
            case 1:
                strings = new String[] {
                        "read", "unread", "all"
                };
                break;

            // else
            default:
                strings = new String[0];
                break;
        }

        return Arrays.asList(strings);
    }

    /**
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public List<String> onConsoleTabCompleter(String[] args) {
        return null;
    }
}
