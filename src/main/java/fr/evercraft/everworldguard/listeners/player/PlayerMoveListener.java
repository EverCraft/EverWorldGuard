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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.MoveType;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.service.subject.EUserSubject;

public class PlayerMoveListener {
	
	private EverWorldGuard plugin;

	public PlayerMoveListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.POST)
	public void onRespawnPlayer(RespawnPlayerEvent event) {
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(event.getOriginalPlayer().getUniqueId());
		if (!optSubject.isPresent()) return;
		EUserSubject subject = optSubject.get();
		
		//subject.canMoveTo(event.getTargetEntity(), event.getToTransform().getLocation(), MoveType.RESPAWN, event.getCause());
		//subject.moveTo(event.getTargetEntity(), event.getToTransform().getLocation(), MoveType.RESPAWN, event.getCause());
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveEntityFirst(MoveEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (!(event.getTargetEntity() instanceof Player)) return;		
		EPlayer player = this.plugin.getEServer().getEPlayer(event.getTargetEntity().getUniqueId()).orElse(null);
		if (player == null) return;
		
		Location<World> location = player.canMoveTo(player, event.getToTransform().getLocation(), MoveType.MOVE, event.getCause()).orElse(null);
		if (location != null) {
			Transform<World> transform = event.getFromTransform()
					.setLocation(location)
					.addTranslation(Vector3d.from(0.5, 0, 0.5));
			event.setToTransform(transform);
			
			Entity vehicle = player.getVehicle().orElse(null);
			if (vehicle != null) {
				
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
			}
			
			event.setCancelled(true);
			player.setTransform(transform.addTranslation(Vector3d.from(0, 1, 0)));
		}
	}
	
	@Listener(beforeModifications=true)
	public void onMoveEntityPost(MoveEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (!(event.getTargetEntity() instanceof Player)) return;
		Player player = (Player) event.getTargetEntity();
		
		Optional<EUserSubject> optSubject = this.plugin.getService().getSubject(player.getUniqueId());
		if (!optSubject.isPresent()) return;
		optSubject.get().moveTo(player, event.getToTransform().getLocation(), MoveType.MOVE);
	}
}
