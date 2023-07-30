package me.edgrrrr.de.economy.events;

import me.edgrrrr.de.console.Console;
import me.edgrrrr.de.economy.DivinityEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final DivinityEconomy divinityEconomy;

    public PlayerJoin(DivinityEconomy divinityEconomy) {
        this.divinityEconomy = divinityEconomy;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!this.divinityEconomy.hasAccount(player)) {
            this.divinityEconomy.createPlayerAccount(player);
            Console.get().debug("Player '%s' (%s) did not previously have an account, one has been created for them.", player.getName(), player.getUniqueId());
        }
    }
}
