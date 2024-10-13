package org.divinitycraft.divinityeconomy.commands.admin;

import org.divinitycraft.divinityeconomy.DEPlugin;
import org.divinitycraft.divinityeconomy.commands.DivinityCommand;
import org.divinitycraft.divinityeconomy.config.Setting;
import org.divinitycraft.divinityeconomy.lang.LangEntry;
import org.divinitycraft.divinityeconomy.market.items.materials.MarketableMaterial;
import org.divinitycraft.divinityeconomy.utils.Converter;
import org.bukkit.entity.Player;

/**
 * A command for setting the value of an item
 */
public class SetValue extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public SetValue(DEPlugin app) {
        super(app, "setvalue", true, Setting.COMMAND_SET_VALUE_ENABLE_BOOLEAN);
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
        MarketableMaterial marketableMaterial;
        double value;
        switch (args.length) {
            case 2:
                marketableMaterial = getMain().getMarkMan().getItem(args[0]);
                value = Converter.getDouble(args[1]);
                break;

            default:
                getMain().getConsole().usage(sender, LangEntry.GENERIC_InvalidNumberOfArguments.get(getMain()), this.help.getUsages());
                return true;
        }

        // Ensure material exists
        if (marketableMaterial == null) {
            getMain().getConsole().send(sender, LangEntry.MARKET_InvalidItemName.logLevel, String.format(LangEntry.MARKET_InvalidItemName.get(getMain()), args[0]));
            return true;
        }

        if (value < 0) {
            getMain().getConsole().send(sender, LangEntry.GENERIC_InvalidAmountGiven.logLevel, String.format(LangEntry.GENERIC_InvalidAmountGiven.get(getMain()), value, 0));
            return true;
        }

        int previousStock = marketableMaterial.getQuantity();
        double previousValue = marketableMaterial.getManager().getBuyPrice(marketableMaterial.getQuantity());
        marketableMaterial.getManager().setPrice(marketableMaterial, value);
        getMain().getConsole().send(sender, LangEntry.STOCK_ValueChanged.logLevel, String.format(LangEntry.STOCK_ValueChanged.get(getMain()), getMain().getConsole().formatMoney(previousValue), previousStock, getMain().getConsole().formatMoney(value), marketableMaterial.getQuantity()));

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
        return this.onPlayerCommand(null, args);
    }
}
