package org.divinitycraft.divinityeconomy.commands.market;

import org.divinitycraft.divinityeconomy.Constants;
import org.divinitycraft.divinityeconomy.DEPlugin;
import org.divinitycraft.divinityeconomy.commands.DivinityCommandMaterials;
import org.divinitycraft.divinityeconomy.config.Setting;
import org.divinitycraft.divinityeconomy.lang.LangEntry;
import org.divinitycraft.divinityeconomy.market.items.materials.MarketableMaterial;
import org.divinitycraft.divinityeconomy.market.items.materials.MaterialValueResponse;
import org.divinitycraft.divinityeconomy.player.PlayerManager;
import org.divinitycraft.divinityeconomy.utils.Converter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for selling items to the market
 */
public class Sell extends DivinityCommandMaterials {

    /**
     * Constructor
     *
     * @param app
     */
    public Sell(DEPlugin app) {
        super(app, "sell", false, Setting.COMMAND_SELL_ITEM_ENABLE_BOOLEAN);
        this.checkItemMarketEnabled = true;
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
        String materialName;
        int amountToSell = 1;
        boolean sellAll = false;

        switch (args.length) {
            // Just material, used default amount of 1
            case 1:
                materialName = args[0];
                break;

            // Material & Amount
            case 2:
                materialName = args[0];
                String arg = args[1];
                if (LangEntry.W_max.is(getMain(), arg)) {
                    sellAll = true;
                } else {
                    amountToSell = Converter.getInt(args[1]);
                }
                break;

            default:
                getMain().getConsole().usage(sender, LangEntry.GENERIC_InvalidNumberOfArguments.get(getMain()), this.help.getUsages());
                return true;
        }

        // Ensure amount is within constraints
        if (amountToSell > Constants.MAX_VALUE_AMOUNT || amountToSell < Constants.MIN_VALUE_AMOUNT) {
            getMain().getConsole().send(sender, LangEntry.GENERIC_InvalidAmountGiven.logLevel, LangEntry.GENERIC_InvalidAmountGiven.get(getMain()));
            return true;
        }


        // Check material given exists
        MarketableMaterial marketableMaterial = getMain().getMarkMan().getItem(materialName);
        if (marketableMaterial == null) {
            getMain().getConsole().send(sender, LangEntry.MARKET_InvalidItemName.logLevel, LangEntry.MARKET_InvalidItemName.get(getMain()), materialName);
            return true;
        }

        // Ensure player has enough of the material to sell.
        int materialCount = marketableMaterial.getMaterialCount(sender);
        if (sellAll) {
            amountToSell = materialCount;
        }

        // Ensure the material is allowed to be bought and sold
        if (!marketableMaterial.getAllowed()) {
            getMain().getConsole().logFailedSale(sender, amountToSell, marketableMaterial.getName(), LangEntry.MARKET_ItemIsBanned.get(getMain(), marketableMaterial.getName()));
            return true;
        }

        // Ensure player inventory has enough
        if (materialCount < amountToSell) {
            getMain().getConsole().logFailedSale(sender, amountToSell, marketableMaterial.getName(), String.format(LangEntry.MARKET_InvalidInventoryStock.get(getMain()), materialCount, amountToSell));
            return true;
        }

        // Get item stacks
        // Remove enchanted items
        // Clone incase need to be refunded
        // Get valuation
        ItemStack[] allStacks = marketableMaterial.getMaterialSlotsToCount(sender, amountToSell);
        MaterialValueResponse response = marketableMaterial.getManager().getSellValue(allStacks);

        // Check for removed items
        if (response.getItemStacks().size() == 0) {
            getMain().getConsole().logFailedSale(sender, response.getQuantity(), marketableMaterial.getName(), LangEntry.MARKET_NothingToSellAfterSkipping.get(getMain()).toLowerCase());
            return true;
        }


        // If response failed
        if (response.isFailure()) {
            getMain().getConsole().logFailedSale(sender, response.getQuantity(), marketableMaterial.getName(), response.getErrorMessage());
            return true;
        }


        // Remove player items
        PlayerManager.removePlayerItems(response.getItemStacksAsArray());


        // Add cash
        EconomyResponse economyResponse = getMain().getEconMan().addCash(sender, response.getValue());
        if (!economyResponse.transactionSuccess()) {
            PlayerManager.addPlayerItems(sender, response.getClonesAsArray());
            // Handles console, player message and mail
            getMain().getConsole().logFailedSale(sender, response.getQuantity(), marketableMaterial.getName(), economyResponse.errorMessage);

        } else {
            marketableMaterial.getManager().editQuantity(marketableMaterial, response.getQuantity());
            // Handles console, player message and mail
            getMain().getConsole().logSale(sender, response.getQuantity(), response.getValue(), marketableMaterial.getName());
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
