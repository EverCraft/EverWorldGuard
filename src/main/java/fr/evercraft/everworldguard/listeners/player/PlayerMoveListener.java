package fr.evercraft.everworldguard.listeners.player;

import java.util.Optional;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import fr.evercraft.everapi.services.worldguard.MoveType;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.service.subject.EUserSubject;

public class PlayerMoveListener {
	
	private EverWorldGuard plugin;

	public PlayerMoveListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.PRE)
	public void onRespawnPlayerPre(RespawnPlayerEvent event) {
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(event.getOriginalPlayer().getUniqueId());
		if (!optSubject.isPresent()) return;
		EUserSubject subject = optSubject.get();
		
		subject.moveToPre(event.getTargetEntity(), event.getToTransform().getLocation(), MoveType.RESPAWN, event.getCause());
	}
	
	@Listener(order=Order.BEFORE_POST, beforeModifications=true)
	public void onRespawnPlayerPost(RespawnPlayerEvent event) {
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(event.getOriginalPlayer().getUniqueId());
		if (!optSubject.isPresent()) return;
		EUserSubject subject = optSubject.get();
		
		subject.moveToPost(event.getTargetEntity(), event.getToTransform().getLocation(), MoveType.RESPAWN, event.getCause());
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveEntityFirst(MoveEntityEvent event, @Getter("getTargetEntity") Player player_sponge) {
		if (event.isCancelled()) return;
			
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(player_sponge.getUniqueId());
		if (!optSubject.isPresent()) return;
		
		Location<World> location = optSubject.get().moveToPre(player_sponge, event.getToTransform().getLocation(), MoveType.MOVE, event.getCause()).orElse(null);
		if (location != null) {			
			Transform<World> transform = event.getFromTransform()
					.setLocation(location.add(Vector3d.from(0.5, 0, 0.5)));
			event.setToTransform(transform);
			
			Entity vehicle = player_sponge.getVehicle().orElse(null);
			if (vehicle != null) {
				player_sponge.sendMessage(Text.of("vehicle"));
				while (vehicle != null) {
					vehicle.clearPassengers();
					vehicle.setVelocity(Vector3d.ZERO);
					
					if (vehicle instanceof Living) {
						vehicle.setTransform(transform);
					} else {
						vehicle.setTransform(transform.addTranslation(Vector3d.from(0, 1, 0)));
					}
					
					vehicle = vehicle.getVehicle().orElse(null);
				}
				event.setCancelled(true);
				player_sponge.setTransform(transform.addTranslation(Vector3d.from(0, 1, 0)));
			}
		}
	}
	
	@Listener(order=Order.BEFORE_POST, beforeModifications=true)
	public void onMoveEntityPost(MoveEntityEvent event, @Getter("getTargetEntity") Player player_sponge) {
		if (event.isCancelled()) return;
		
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(player_sponge.getUniqueId());
		if (!optSubject.isPresent()) return;
		
		optSubject.get().moveToPost(player_sponge, event.getToTransform().getLocation(), MoveType.MOVE, event.getCause());
	}
}
