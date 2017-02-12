package fr.evercraft.everworldguard.listeners.player;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag.State;
import fr.evercraft.everworldguard.EverWorldGuard;

public class PlayerListener {
	
	private EverWorldGuard plugin;

	public PlayerListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockBreak(ChangeBlockEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockPlayer(event, optPlayer.get());
		} else {
			this.onChangeBlockNatural(event);
		}
	}
	
	public void onChangeBlockPlayer(ChangeBlockEvent event, Player player) {
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetWorld());
		 
		event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW));
	}
	
	public void onChangeBlockNatural(ChangeBlockEvent event) {
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetWorld());
		 
		event.filter(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.ALLOW));
	}
}
