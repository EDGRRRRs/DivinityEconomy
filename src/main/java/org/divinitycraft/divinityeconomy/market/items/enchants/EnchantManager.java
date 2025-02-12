package org.divinitycraft.divinityeconomy.market.items.enchants;

import org.divinitycraft.divinityeconomy.DEPlugin;
import org.divinitycraft.divinityeconomy.config.Setting;
import org.divinitycraft.divinityeconomy.lang.LangEntry;
import org.divinitycraft.divinityeconomy.market.MarketableToken;
import org.divinitycraft.divinityeconomy.market.items.ItemManager;
import org.divinitycraft.divinityeconomy.response.Response;
import org.divinitycraft.divinityeconomy.response.ValueResponse;
import org.divinitycraft.divinityeconomy.utils.ArrayUtils;
import org.divinitycraft.divinityeconomy.utils.Converter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Enchant manager
 * Used for managing prices and quantity of enchants
 */
public class EnchantManager extends ItemManager {
    public static final String enchantFile = "enchants.yml";
    public static final String aliasFile = "enchantAliases.yml";
    private boolean allowUnsafe;

    /**
     * Constructor
     * Note that this does not automatically load the enchants.
     * Use #loadEnchants() to read the enchants into memory.
     *
     * @param main The main class
     */
    public EnchantManager(DEPlugin main) {
        super(main, enchantFile, aliasFile, new ConcurrentHashMap<>());
    }

    @Override
    public void init() {
        this.allowUnsafe = this.getConfMan().getBoolean(Setting.MARKET_ENCHANTS_ALLOW_UNSAFE_BOOLEAN);
        this.saveMessagesDisabled = this.getConfMan().getBoolean(Setting.IGNORE_SAVE_MESSAGE_BOOLEAN);
        this.buyScale = this.getConfMan().getDouble(Setting.MARKET_ENCHANTS_BUY_TAX_FLOAT);
        this.sellScale = this.getConfMan().getDouble(Setting.MARKET_ENCHANTS_SELL_TAX_FLOAT);
        this.baseQuantity = this.getConfMan().getInt(Setting.MARKET_ENCHANTS_BASE_QUANTITY_INTEGER);
        this.dynamicPricing = this.getConfMan().getBoolean(Setting.MARKET_ENCHANTS_DYN_PRICING_BOOLEAN);
        this.wholeMarketInflation = this.getConfMan().getBoolean(Setting.MARKET_ENCHANTS_WHOLE_MARKET_INF_BOOLEAN);
        this.maxItemValue = this.getConfMan().getDouble(Setting.MARKET_MAX_ITEM_VALUE_DOUBLE);
        if (this.maxItemValue < 0) {
            this.maxItemValue = Double.MAX_VALUE;
        }
        this.minItemValue = this.getConfMan().getDouble(Setting.MARKET_MIN_ITEM_VALUE_DOUBLE);
        if (this.minItemValue < 0) {
            this.minItemValue = Double.MIN_VALUE;
        }
        int timer = Converter.getTicks(this.getConfMan().getInt(Setting.MARKET_SAVE_TIMER_INTEGER));
        this.saveTimer = new BukkitRunnable() {
            @Override
            public void run() {
                saveItems();
            }
        };
        this.saveTimer.runTaskTimerAsynchronously(getMain(), timer, timer);
        this.loadItems();
        this.loadAliases();
    }

    @Override
    public void deinit() {
        this.saveTimer.cancel();
        this.saveItems();
    }

    /**
     * Returns the enchantment from the alias
     */
    public MarketableEnchant getEnchant(String alias) {
        return (MarketableEnchant) this.getItem(alias);
    }


    /**
     * Returns the enchantment from the enchantment
     */
    public MarketableEnchant getEnchant(Enchantment enchantment) {
        return (MarketableEnchant) this.getItem(enchantment.getKey().getKey());
    }


    /**
     * Returns if the enchantment given is supported by the itemstack given.
     */
    public boolean supportsEnchant(ItemStack itemStack, Enchantment enchantment) {
        return (
            allowUnsafe ||
            (itemStack.getType() == Material.BOOK) ||
            (itemStack.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta && !enchantmentStorageMeta.hasConflictingStoredEnchant(enchantment)) ||
            enchantment.canEnchantItem(itemStack)
        );
    }

    /**
     * Returnst the type of token
     *
     * @return String
     */
    @Override
    public String getType() {
        return "ENCHANTMENT";
    }


    /**
     * Returns the number of enchants, each enchant being worth one enchant, not the stock of the enchants.
     */
    public int getEnchantCount() {
        return this.itemMap.size();
    }

    /**
     * Reduces an enchant level on an itemstack by levels amount
     * If the level is 5 and you remove 4, the level is set to 1.
     * If the level is 5 and you remove 5, the enchant is removed.
     *
     * @param itemStack   - The itemstack to remove the enchant from
     * @param enchantment - The enchantment to remove
     * @param levels      - The levels to remove
     */
    public void removeEnchantLevelsFromItem(ItemStack itemStack, Enchantment enchantment, int levels) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        // If item can store enchants
        if (itemMeta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            int currentLevel = enchantmentStorageMeta.getStoredEnchantLevel(enchantment);
            enchantmentStorageMeta.removeStoredEnchant(enchantment);
            int levelsLeft = currentLevel - levels;
            if (levelsLeft > 0) {
                enchantmentStorageMeta.addStoredEnchant(enchantment, levelsLeft, true);
            } else {
                if (itemStack.getType() == Material.ENCHANTED_BOOK) {
                    itemStack.setType(Material.BOOK);
                }
            }

            itemStack.setItemMeta(enchantmentStorageMeta);
        }

        // If item itself is enchanted
        else {
            int currentLevel = itemStack.getEnchantmentLevel(enchantment);
            itemStack.removeEnchantment(enchantment);
            int levelsLeft = currentLevel - levels;
            if (levelsLeft > 0) {
                itemStack.addUnsafeEnchantment(enchantment, levelsLeft);
            }
        }
    }


    /**
     * Removes all enchants from an item
     * @param itemStack
     */
    public void removeEnchantsFromItem(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantmentIntegerMap = getEnchantments(itemStack);
        enchantmentIntegerMap.forEach((enchantment, integer) -> this.removeEnchantLevelsFromItem(itemStack, enchantment, integer));
    }


    /**
     * Adds an enchant to an item
     *
     * @param itemStack   - The itemstack
     * @param enchantment - The enchant
     * @param levels      - The levels
     * @return Response
     */
    public Response addEnchantToItem(ItemStack itemStack, Enchantment enchantment, int levels) {
        // Get new desired level
        int newLevel = levels + itemStack.getEnchantmentLevel(enchantment);

        // Get enchant data from economy
        MarketableEnchant enchantData = (MarketableEnchant) this.getItem(enchantment.getKey().getKey());


        // If enchant data is null, return failure.
        if (enchantData == null) {
            return new Response(EconomyResponse.ResponseType.FAILURE, LangEntry.MARKET_ItemIsNotSupported.get(getMain(), enchantment.getKey().getKey()));
        }

        // If enchant level is greater than maximum, return failure.
        if (enchantData.getMaxLevel() < newLevel) {
            return new Response(EconomyResponse.ResponseType.FAILURE, LangEntry.MARKET_LevelIsGreaterThanMax.get(getMain(), newLevel, enchantData.getMaxLevel()));
        }

        // Check if item is a book
        if (itemStack.getType() == Material.BOOK) {
            itemStack.setType(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) itemStack.getItemMeta();
            esm.addStoredEnchant(enchantment, newLevel, true);
            itemStack.setItemMeta(esm);
        }

        // If item has enchantment storage
        else if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            enchantmentStorageMeta.addStoredEnchant(enchantment, newLevel, true);
            itemStack.setItemMeta(enchantmentStorageMeta);
        }

        // Add enchantment to itemstack
        else {
            itemStack.addUnsafeEnchantment(enchantment, newLevel);
        }

        return new Response(EconomyResponse.ResponseType.SUCCESS, "");
    }

    /**
     * Returns if an item is enchanted or not.
     * Supports un-enchantable items
     *
     * @param itemStack - The itemstack to check
     * @return boolean - Is enchanted / Is not enchanted
     */
    public boolean isEnchanted(ItemStack itemStack) {
        // Get item meta
        ItemMeta itemMeta = itemStack.getItemMeta();

        // If item is has enchantedment storage
        if (itemMeta instanceof EnchantmentStorageMeta) {
            return ((EnchantmentStorageMeta) itemMeta).hasStoredEnchants();
        }

        // Else return itemstack meta.
        return itemMeta.hasEnchants();
    }

    /**
     * Returns the total purchase price of all the enchants on itemstacks
     *
     * @param itemStacks - The itemstacks to check
     * @return MultiValueResponse
     */
    public EnchantValueResponse getBuyValue(ItemStack[] itemStacks) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);


        // Loop through enchants
        for (ItemStack itemStack : itemStacks) {
            // Add response together
            response.addResponse(this.getBuyValue(itemStack, 0));
        }

        return response;
    }

    /**
     * @param itemStack - The item stack to get the value of
     * @param amount - This is ignored
     * @return ValueResponse
     */
    @Override
    public EnchantValueResponse getBuyValue(ItemStack itemStack, int amount) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);

        // Get enchantments
        Map<Enchantment, Integer> enchantments = EnchantManager.getEnchantments(itemStack);

        // Loop through enchantments
        for (Enchantment enchantment : enchantments.keySet()) {
            int level = enchantments.get(enchantment);
            String enchantID = enchantment.getKey().getKey();

            // Get value response and add to response
            response.addResponse(this.getBuyValue(itemStack, enchantID, level));
        }

        return response;
    }

    /**
     * Returns the item given as base class DivinityItem is abstract and cannot be instantiated
     *
     * @param data       - The data to load
     * @param defaultData - The default data to load
     * @param ID        - The ID of the item
     * @return MarketableToken
     */
    @Override
    public MarketableToken loadItem(String ID, ConfigurationSection data, ConfigurationSection defaultData) {
        return new MarketableEnchant(getMain(), this, ID, data, defaultData);
    }

    /**
     * Returns the purchase value of the enchantID provided at the given level.
     *
     * @param enchantID   - The enchantment ID
     * @param levelsToBuy - The enchantment level
     * @param itemStack   - The itemStack to apply to
     * @return EnchantValueResponse - The value of the enchant
     */
    public EnchantValueResponse getBuyValue(ItemStack itemStack, String enchantID, int levelsToBuy) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);


        // Get enchant data
        MarketableEnchant enchantData = (MarketableEnchant) this.getItem(enchantID);


        // Enchant data is null
        if (enchantData == null)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemDoesNotExist.get(getMain(), enchantID));


        // Check exists in store
        if (enchantData.getEnchantment() == null)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemDoesNotExistInTheStore.get(getMain(), enchantID));


        // Get current and new enchantment level
        Map<Enchantment, Integer> enchantments = EnchantManager.getEnchantments(itemStack);
        int itemStackEnchantmentLevel = enchantments.getOrDefault(enchantData.getEnchantment(), 0);
        int newTotalLevel = itemStackEnchantmentLevel + levelsToBuy;
        int enchantAmount = MarketableEnchant.levelsToBooks(itemStackEnchantmentLevel, newTotalLevel);

        // Get value
        double value = this.calculatePrice(enchantAmount, enchantData.getQuantity(), this.buyScale, false);
        response.addToken(enchantData, enchantAmount, value, itemStack);


        // Check enchant is allowed
        if (!(enchantData.getAllowed()))
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsBanned.get(getMain(), enchantData.getName()));


        // Check enchant is supported on item
        if (!(this.supportsEnchant(itemStack, enchantData.getEnchantment()) || this.allowUnsafe))
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsNotSupportedOnThisItem.get(getMain(), enchantData.getName()));


        // Check new level isn't greater than max
        if (enchantData.getMaxLevel() < newTotalLevel)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_LevelWouldBeGreaterThanMax.get(getMain(), newTotalLevel, enchantData.getMaxLevel()));


        // Check store has enough
        if (enchantAmount > enchantData.getQuantity())
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_InvalidStockAmount.get(getMain(), enchantData.getName(), enchantData.getQuantity(), enchantAmount));


        // Check market isn't saturated
        if (value <= 0)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsWorthless.get(getMain(), enchantData.getName()));


        // Return success
        return response;
    }


    /**
     * Use getBulkSellValue(ItemStack)
     *
     * @param itemStacks
     * @return
     */
    @Override
    public EnchantValueResponse getSellValue(ItemStack[] itemStacks) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);


        // Loop through enchants
        for (ItemStack itemStack : itemStacks) {
            // Get response
            EnchantValueResponse thisResponse = this.getSellValue(itemStack, 0);

            // Check failure
            if (thisResponse.isFailure()) continue;

            // Add response together
            response.addResponse(thisResponse);
        }

        return response;
    }


    /**
     * Use getSellValue(ItemStack, int)
     *
     * @param itemStack
     * @param amount
     * @return
     */
    @Override
    public EnchantValueResponse getSellValue(ItemStack itemStack, int amount) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);

        // Get enchantments
        Map<Enchantment, Integer> enchantments = EnchantManager.getEnchantments(itemStack);

        // Loop through enchants
        for (Enchantment enchantment : enchantments.keySet()) {
            // Get value response and add to response
            EnchantValueResponse thisResponse = this.getSellValue(itemStack, enchantment.getKey().getKey(), enchantments.get(enchantment));

            if (thisResponse.isFailure()) continue;

            // Add response together
            response.addResponse(thisResponse);
        }

        return response;
    }

    /**
     * Returns the names and aliases for the itemstack given
     *
     * @param itemStack
     * @return
     */
    @Override
    public Set<String> getItemNames(ItemStack itemStack) {
        // Array list for enchants
        Set<String> itemNames = new HashSet<>();

        // Loop through enchants in storage meta
        // Add enchants to array list
        Map<Enchantment, Integer> enchantments = EnchantManager.getEnchantments(itemStack);
        for (Enchantment enchantment : enchantments.keySet()) {
            itemNames.add(enchantment.getKey().getKey());
        }

        return itemNames;
    }

    public Set<String> getCompatibleEnchants(ItemStack itemStack) {
        // Array list for enchants
        Set<String> itemNames = new HashSet<>();

        // Loop through enchants in storage meta
        // Add enchants to array list - if they are compatible with the item or allowUnsafe is true
        for (MarketableToken token : this.itemMap.values()) {
            MarketableEnchant enchant = (MarketableEnchant) token;

            if (this.supportsEnchant(itemStack, enchant.getEnchantment())) {
                itemNames.addAll(this.revAliasMap.get((enchant.getID().toLowerCase())));
            }
        }
        return itemNames;
    }

    public String[] getCompatibleEnchants(ItemStack itemStack, String startsWith) {
        return this.searchItemNames(this.getCompatibleEnchants(itemStack), startsWith).toArray(new String[0]);
    }

    /**
     * Returns the names and aliases for the itemstack given starting with startswith
     *
     * @param itemStack
     * @param startswith
     * @return
     */
    @Override
    public Set<String> getItemNames(ItemStack itemStack, String startswith) {
        return this.searchItemNames(this.getItemNames(itemStack), startswith);
    }


    /**
     * Returns the value of an enchant on an item.
     *
     * @param itemStack    - The itemstack to check
     * @param enchantID    - The enchant ID to check for
     * @param levelsToSell - The level to value
     * @return ValueResponse
     */
    public EnchantValueResponse getSellValue(ItemStack itemStack, String enchantID, int levelsToSell) {
        // Create response
        EnchantValueResponse response = new EnchantValueResponse(EconomyResponse.ResponseType.SUCCESS, null);


        // Get enchant data
        MarketableEnchant enchantData = (MarketableEnchant) this.getItem(enchantID);

        // No enchant data
        if (enchantData == null)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemDoesNotExist.get(getMain(), enchantID));


        // Get enchantment
        Enchantment enchantment = enchantData.getEnchantment();
        String itemCleanName = this.getMarkMan().getName(itemStack);


        // No enchantment
        if (enchantment == null)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemDoesNotExistInTheStore.get(getMain(), enchantID));


        // Check enchant is allowed
        if (!enchantData.getAllowed())
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsBanned.get(getMain(), enchantData.getName()));


        // Get itemstack enchantments
        Map<Enchantment, Integer> itemStackEnchants = EnchantManager.getEnchantments(itemStack);

        // Check stored enchants contain given enchant
        if (!itemStackEnchants.containsKey(enchantment))
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsNotEnchanted.get(getMain(), itemCleanName, enchantData.getName()));


        // Get enchantment stack level
        int itemStackEnchantLevel = itemStackEnchants.get(enchantment);
        int bookLevel = MarketableEnchant.levelsToBooks(itemStackEnchantLevel - levelsToSell, itemStackEnchantLevel);


        // Check enough levels to sell
        if (itemStackEnchantLevel < levelsToSell)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemNotEnoughLevels.get(getMain(), itemCleanName, itemStackEnchantLevel, levelsToSell));


        // Get value
        double value = this.calculatePrice(bookLevel, enchantData.getQuantity(), this.sellScale, false);
        response.addToken(enchantData, levelsToSell, value, itemStack);


        // Value equal or less than 0, return saturation
        if (value <= 0)
            return (EnchantValueResponse) response.setFailure(LangEntry.MARKET_ItemIsWorthless.get(getMain(), enchantData.getName()));


        return response;
    }


    /**
     * Edits the quantity of an enchant & the total quantity of enchants
     *
     * @param enchantData - The enchant to edit
     * @param levels      - The quantity to edit by, in levels. Can be negative.
     */
    public void editLevelQuantity(MarketableEnchant enchantData, int levels) {
        int books;
        if (levels > 0) {
            books = MarketableEnchant.levelsToBooks(0, levels);
        } else {
            books = -MarketableEnchant.levelsToBooks(0, -levels);
        }
        this.editQuantity(enchantData, books);
    }


    /**
     * Returns the string buy value of an enchant on an item.
     * @param heldItem - The itemstack to check
     * @param enchantID - The enchant ID to check for
     * @param levels - The level to value
     * @return String
     */
    public String getBuyValueString(@Nonnull ItemStack heldItem, @Nonnull String enchantID, int levels) {
        ValueResponse value = this.getBuyValue(heldItem, enchantID, levels);
        if (value.isFailure()) {
            return String.format("Error: %s", value.getErrorMessage());
        }

        return String.format("Value: %s", this.getConsole().formatMoney(value.getValue()));
    }

    /**
     * Returns the string sell value of an enchant on an item.
     * @param heldItem - The itemstack to check
     * @param enchantID - The enchant ID to check for
     * @param levels - The level to value
     * @return
     */
    public String getSellValueString(@Nonnull ItemStack heldItem, @Nonnull String enchantID, int levels) {
        ValueResponse value = this.getSellValue(heldItem, enchantID, levels);
        if (value.isFailure()) {
            return String.format("Error: %s", value.getErrorMessage());
        }

        return String.format("Value: %s", this.getConsole().formatMoney(value.getValue()));
    }

    /**
     * Returns the string bulk sell value of an item.
     * @param heldItem - The itemstack to check
     * @return String
     */
    public String getSellValueString(@Nonnull ItemStack heldItem) {
        EnchantValueResponse evr = this.getSellValue(new ItemStack[]{heldItem});
        if (evr.isFailure()) {
            return String.format("Error: %s", evr.getErrorMessage());
        }

        return String.format("Value: %s", this.getConsole().formatMoney(evr.getValue()));
    }


    /**
     * Returns an array of strings of the levels of an enchant on an item.
     *
     * @param heldItem  - The itemstack to check
     * @param enchantID - The enchant to check for
     * @return String[]
     */
    public String[] getUpgradeValueString(@Nonnull ItemStack heldItem, @Nullable String enchantID) {
        MarketableEnchant enchantData = this.getEnchant(enchantID);
        int maxLevel = 1;
        if (enchantData != null) {
            maxLevel = enchantData.getMaxLevel() - heldItem.getEnchantmentLevel(enchantData.getEnchantment());
        }

        return ArrayUtils.strRange(1, maxLevel);
    }

    public String[] getDowngradeValueString(@Nonnull ItemStack heldItem, @Nullable String enchantID) {
        MarketableEnchant enchantData = this.getEnchant(enchantID);
        int maxLevel = 1;
        if (enchantData != null) {
            maxLevel = heldItem.getEnchantmentLevel(enchantData.getEnchantment());
        }

        return ArrayUtils.strRange(1, maxLevel);
    }


    public static Map<Enchantment, Integer> getEnchantments(@Nonnull ItemStack itemStack) {
        // Get item stack meta
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Item can store enchants, return stored enchants
        if (itemMeta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            return enchantmentStorageMeta.getStoredEnchants();
        }

        // Return direct enchants
        return itemMeta.getEnchants();
    }
}
