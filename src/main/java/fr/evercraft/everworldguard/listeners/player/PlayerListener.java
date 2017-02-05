package fr.evercraft.everworldguard.listeners.player;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;

import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag.State;
import fr.evercraft.everworldguard.EverWorldGuard;

public class PlayerListener {
	
	private EverWorldGuard plugin;

	public PlayerListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockBreak(ChangeBlockEvent.Break event, @First Player player_sponge) {
		Optional<EPlayer> optPlayer = this.plugin.getEServer().getEPlayer(player_sponge);
		if (optPlayer.isPresent()) {
			return;
		}
		EPlayer player = optPlayer.get();
		
		if (player.getRegions().getFlag(player, Flags.BUILD).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockPlace(ChangeBlockEvent.Place event, @First Player player_sponge) {
		Optional<EPlayer> optPlayer = this.plugin.getEServer().getEPlayer(player_sponge);
		if (optPlayer.isPresent()) {
			return;
		}
		EPlayer player = optPlayer.get();
		
		if (player.getRegions().getFlag(player, Flags.BUILD).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}

}
