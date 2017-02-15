package fr.evercraft.everworldguard.listeners.player;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag.State;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.subject.EUserSubject;

public class PlayerListener {
	
	private EverWorldGuard plugin;

	public PlayerListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Auth event) {
		this.plugin.getService().getSubjectList().get(event.getProfile().getUniqueId());
	}
	
	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Join event) {
		EUserSubject player = this.plugin.getService().getSubjectList().registerPlayer(event.getTargetEntity().getUniqueId());
		player.initialize(event.getTargetEntity());
	}

	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Disconnect event) {
		this.plugin.getService().getSubjectList().removePlayer(event.getTargetEntity().getUniqueId());
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
	
	@Listener(order=Order.FIRST)
	public void onPlayerHeal(HealEntityEvent event) {
		if (event.isCancelled()) return;
		
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		if (event.getTargetEntity() instanceof Player && event.getBaseHealAmount() > event.getFinalHealAmount()) {
			Player player = (Player) event.getTargetEntity();
			
			if (world.getRegions(player.getLocation().getPosition()).getFlag(player, Flags.INVINCIBILITY).equals(State.ALLOW)) {
				event.setCancelled(true);
			}
		}
	}
	
	@Listener
	public void onPlayerDamage(DamageEntityEvent event) {
		if (event.isCancelled()) return;
		
		WorldWorldGuard world = this.plugin.getService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		if (event.getTargetEntity() instanceof Player) {
			Player player = (Player) event.getTargetEntity();
			
			Optional<EntityDamageSource> optDamageSource = event.getCause().first(EntityDamageSource.class);
			if (optDamageSource.isPresent() && optDamageSource.get().getSource() instanceof Player) {
				
				if (world.getRegions(player.getLocation().getPosition()).getFlag(player, Flags.PVP).equals(State.DENY)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
