package fr.evercraft.everworldguard.listeners.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;

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
	public void onChangeBlockBreak(ChangeBlockEvent.Break event, @First Player player) {
		if (event.isCancelled()) return;
		
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetWorld());
		 
		event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW));
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockPlace(ChangeBlockEvent.Place event, @First Player player) {
		if (event.isCancelled()) return;
		
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetWorld());
		 
		event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW));
	}

}
