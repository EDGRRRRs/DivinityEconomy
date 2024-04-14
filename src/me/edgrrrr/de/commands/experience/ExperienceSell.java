package me.edgrrrr.de.commands.experience;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandEnchant;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.lang.LangEntry;
import me.edgrrrr.de.market.exp.ExpManager;
import me.edgrrrr.de.response.ValueResponse;
import me.edgrrrr.de.utils.Converter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

/**
 * A command for selling enchants on held items
 */
public class ExperienceSell extends DivinityCommandEnchant {

    /**
     * Constructor
     *
     * @param app
     */
    public ExperienceSell(DEPlugin app) {
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
    public boolean onPlayerCommand(Player sender, String[] args) {
        int experience = 1;
        // How to use
        switch (args.length) {
            case 0:
                break;

            case 1:
                if (LangEntry.W_max.is(getMain(), args[0])) {
                    experience = ExpManager.getPlayerExp(sender);
                } else {
                    experience = Converter.getInt(args[0]);
                }
                break;

            default:
                getMain().getConsole().usage(sender, LangEntry.GENERIC_InvalidNumberOfArguments.get(getMain()), this.help.getUsages());
                return true;
        }

        experience = Converter.constrainInt(experience, getMain().getExpMan().getMinTradableExp(), getMain().getExpMan().getMaxTradableExp());

        // Ensure item valuation was successful
        ValueResponse valueResponse = getMain().getExpMan().getSellValue(experience, sender);
        if (valueResponse.isFailure()) {
            getMain().getConsole().logFailedSale(sender, experience, LangEntry.W_experience.get(getMain()), valueResponse.getErrorMessage());
            return true;
        }

        double startingBalance = getMain().getEconMan().getBalance(sender);
        EconomyResponse economyResponse = getMain().getEconMan().addCash(sender, valueResponse.getValue());

        if (!economyResponse.transactionSuccess()) {
            getMain().getConsole().logFailedSale(sender, experience, LangEntry.W_experience.get(getMain()), economyResponse.errorMessage);
            getMain().getEconMan().setCash(sender, startingBalance);
        } else {
            getMain().getExpMan().remExperience(sender, experience);
            getMain().getConsole().logSale(sender, experience, economyResponse.amount, LangEntry.W_experience.get(getMain()));
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
        return false;
    }
}
