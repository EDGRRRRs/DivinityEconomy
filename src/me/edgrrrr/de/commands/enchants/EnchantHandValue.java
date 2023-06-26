package me.edgrrrr.de.commands.enchants;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandEnchant;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.player.PlayerManager;
import me.edgrrrr.de.response.MultiValueResponse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for valuing enchants
 */
public class EnchantHandValue extends DivinityCommandEnchant {

    /**
     * Constructor
     *
     * @param app
     */
    public EnchantHandValue(DEPlugin app) {
        super(app, "ehandvalue", false, Setting.COMMAND_E_VALUE_ENABLE_BOOLEAN);
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

        // If sell all enchants is true
        // Then use MultiValueResponse and use getSellValue of entire item
        // get the item the user is holding.
        // ensure it is not null
        ItemStack heldItem = PlayerManager.getHeldItem(sender);
        if (heldItem == null) {
            this.getMain().getConsole().usage(sender, CommandResponse.InvalidItemHeld.message, this.help.getUsages());
            return true;
        }

        // Ensure item is enchanted
        if (!this.getMain().getEnchMan().isEnchanted(heldItem)) {
            this.getMain().getConsole().usage(sender, CommandResponse.InvalidItemHeld.message, this.help.getUsages());
            return true;
        }

        MultiValueResponse multiValueResponse1 = this.getMain().getEnchMan().getBulkBuyValue(heldItem);
        if (multiValueResponse1.isFailure()) {
            this.getMain().getConsole().warn(sender, "Couldn't determine buy value of %d Enchants(%s) because %s", multiValueResponse1.getTotalQuantity(), multiValueResponse1, multiValueResponse1.errorMessage);
        } else {
            this.getMain().getConsole().info(sender, "Buy: %d enchants costs %s", multiValueResponse1.getTotalQuantity(), this.getMain().getConsole().formatMoney(multiValueResponse1.getTotalValue()));
            for (String enchant : multiValueResponse1.getItemIds()) {
                this.getMain().getConsole().info(sender, "  -Buy: %d %s costs %s", multiValueResponse1.quantities.get(enchant), enchant, this.getMain().getConsole().formatMoney(multiValueResponse1.values.get(enchant)));
            }
        }

        MultiValueResponse multiValueResponse2 = this.getMain().getEnchMan().getBulkSellValue(heldItem);
        if (multiValueResponse2.isFailure()) {
            this.getMain().getConsole().warn(sender, "Couldn't determine sell value of %d Enchants(%s) because %s", multiValueResponse2.getTotalQuantity(), multiValueResponse2, multiValueResponse2.errorMessage);
        } else {
            this.getMain().getConsole().info(sender, "Sell: %d enchants costs %s", multiValueResponse2.getTotalQuantity(), this.getMain().getConsole().formatMoney(multiValueResponse2.getTotalValue()));
            for (String enchant : multiValueResponse2.getItemIds()) {
                this.getMain().getConsole().info(sender, "  -Sell: %d %s costs %s", multiValueResponse2.quantities.get(enchant), enchant, this.getMain().getConsole().formatMoney(multiValueResponse2.values.get(enchant)));
            }
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
