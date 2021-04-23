package edgrrrr.dce.commands.market;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import edgrrrr.dce.commands.DivinityCommand;
import edgrrrr.dce.materials.MaterialData;
import edgrrrr.dce.materials.MaterialPotionData;
import org.bukkit.entity.Player;

/**
 * A command for getting information about a material
 */
public class Info extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public Info(DCEPlugin app) {
        super(app, "information", true, Setting.COMMAND_INFO_ENABLE_BOOLEAN);
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
        switch (args.length) {
            case 1:
                materialName = args[0];
                break;

            default:
                this.app.getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        MaterialData material = this.app.getMaterialManager().getMaterial(materialName);
        if (material == null) {
            this.app.getConsole().usage(sender, "Unknown Item: " + materialName, this.help.getUsages());
        } else {
            this.app.getConsole().info(sender, "==[Information for " + material.getCleanName() + "]==");
            this.app.getConsole().info(sender, "ID: " + material.getMaterialID());
            this.app.getConsole().info(sender, "Type: " + material.getType());
            this.app.getConsole().info(sender, "Current Quantity: " + material.getQuantity());
            this.app.getConsole().info(sender, "Is Banned: " + !(material.getAllowed()));
            if (material.getEntityName() != null)
                this.app.getConsole().info(sender, "Entity Name: " + material.getEntityName());
            MaterialPotionData pData = material.getPotionData();
            if (pData != null) {
                this.app.getConsole().info(sender, "Potion type: " + pData.getType());
                this.app.getConsole().info(sender, "Upgraded potion: " + pData.getUpgraded());
                this.app.getConsole().info(sender, "Extended potion: " + pData.getExtended());
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
        return this.onPlayerCommand(null, args);
    }
}
