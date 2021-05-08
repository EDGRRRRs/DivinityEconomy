package edgrrrr.dce.commands.admin;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import edgrrrr.dce.commands.DivinityCommand;
import edgrrrr.dce.enchants.EnchantData;
import edgrrrr.dce.math.Math;
import org.bukkit.entity.Player;

/**
 * A command for setting the value of an item
 */
public class ESetValue extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public ESetValue(DCEPlugin app) {
        super(app, "esetvalue", true, Setting.COMMAND_E_SET_VALUE_ENABLE_BOOLEAN);
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
        EnchantData enchantData = null;
        double value = -1;
        switch (args.length) {
            case 2:
                enchantData = this.app.getEnchantmentManager().getEnchant(args[0]);
                value = Math.getDouble(args[1]);
                break;

            default:
                this.app.getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                break;
        }

        // Ensure material exists
        if (enchantData == null) {
            this.app.getConsole().send(sender, CommandResponse.InvalidEnchantName.defaultLogLevel, String.format(CommandResponse.InvalidItemName.message, args[0]));
            return true;
        }

        if (value < 0) {
            this.app.getConsole().send(sender, CommandResponse.InvalidAmountGiven.defaultLogLevel, String.format(CommandResponse.InvalidAmountGiven.message, value, 0));
            return true;
        }

        int previousStock = enchantData.getQuantity();
        double previousValue = this.app.getEnchantmentManager().getUserPrice(enchantData.getQuantity());
        this.app.getEnchantmentManager().setPrice(enchantData, value);
        this.app.getConsole().send(sender, CommandResponse.StockValueChanged.defaultLogLevel, String.format(CommandResponse.StockValueChanged.message, previousValue, previousStock, value, enchantData.getQuantity()));

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
