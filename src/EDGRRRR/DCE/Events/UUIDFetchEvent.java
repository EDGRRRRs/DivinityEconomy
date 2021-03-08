package edgrrrr.dce.events;

import edgrrrr.dce.DCEPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * A test class for creating events
 * This just reads and returns the UUID of a player.
 */
public class UUIDFetchEvent implements Listener {
    private final DCEPlugin app;

    public UUIDFetchEvent(DCEPlugin app) {
        this.app = app;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DCEPlugin.CONSOLE.debug("UUID (" + player.getName() + "): " + player.getUniqueId().toString());
    }
}
