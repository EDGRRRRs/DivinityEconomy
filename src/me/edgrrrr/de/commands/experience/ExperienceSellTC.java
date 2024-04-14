package me.edgrrrr.de.commands.experience;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandEnchantTC;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.lang.LangEntry;
import me.edgrrrr.de.market.exp.ExpManager;
import me.edgrrrr.de.utils.Converter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.edgrrrr.de.utils.Converter.constrainInt;

/**
 * A tab completer for the enchant hand sell command
 */
public class ExperienceSellTC extends DivinityCommandEnchantTC {

    /**
     * Constructor
     *
     * @param app
     */
    public ExperienceSellTC(DEPlugin app) {
        super(app, "xpsell", false, Setting.COMMAND_EXP_SELL_ENABLE_BOOLEAN);
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
        String[] strings = switch (args.length) {
            default -> new String[0];
            case 1 -> {
                // Create value array
                ArrayList<String> values = new ArrayList<>();

                // Get exp of player
                int exp = ExpManager.getPlayerExp(sender);

                // Add max args and player experience
                if (exp > 0) {
                    values.add(String.valueOf(exp));
                    LangEntry.W_max.addLang(getMain(), values);
                }

                // Add powers of 10
                int i = 1;
                while (i < getMain().getExpMan().getMaxTradableExp()) {
                    values.add(String.valueOf(i));
                    i *= 10;
                }

                // Return
                yield values.toArray(new String[0]);
            }
            case 2 -> {
                // Resolve amount to buy
                int exp;

                // max argument
                if (LangEntry.W_max.is(getMain(), args[0])) {
                    exp = ExpManager.getPlayerExp(sender);
                }

                // default
                else {
                    exp = Converter.getInt(args[0]);
                }

                // constrain and return
                yield new String[]{getMain().getExpMan().getSellValueString(constrainInt(exp, getMain().getExpMan().getMinTradableExp(), getMain().getExpMan().getMaxTradableExp()), sender)};
            }
        };

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
