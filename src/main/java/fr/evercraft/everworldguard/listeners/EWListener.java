package fr.evercraft.everworldguard.listeners;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.command.select.EWSelect;

public class EWListener {
	private EverWorldGuard plugin;

	public EWListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerInteractEntity(InteractItemEvent.Primary event, @First Player player_sponge) {
		Optional<EPlayer> optPlayer = this.plugin.getEverAPI().getEServer().getEPlayer(player_sponge); 
		if (!optPlayer.isPresent()) {
			return;
		}
		
		EPlayer player = optPlayer.get();
		Optional<ItemStack> optItemInHand = player.getItemInHand(event.getHandType());
		Optional<Vector3d> optPosition = event.getInteractionPoint();
		
		if (optItemInHand.isPresent() && optPosition.isPresent()) {
			ItemStack itemInHand = optItemInHand.get();
			
			if (itemInHand.getItem().equals(ItemTypes.WOODEN_AXE)) {
				Vector3i position = optPosition.get().toInt();

				if (EWSelect.eventPos1(player, position)) {
					event.setCancelled(true);
				}
			}
		} else {
			
		}
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerInteractEntity(InteractItemEvent.Secondary event, @First Player player_sponge) {
		Optional<EPlayer> optPlayer = this.plugin.getEverAPI().getEServer().getEPlayer(player_sponge); 
		if (!optPlayer.isPresent()) {
			return;
		}
		
		EPlayer player = optPlayer.get();
		Optional<ItemStack> optItemInHand = player.getItemInHand(event.getHandType());
		Optional<Vector3d> optPosition = event.getInteractionPoint();
		
		if (optItemInHand.isPresent() && optPosition.isPresent()) {
			ItemStack itemInHand = optItemInHand.get();

			if (itemInHand.getItem().equals(ItemTypes.WOODEN_AXE)) {
				Vector3i position = optPosition.get().toInt();
				
				if (EWSelect.eventPos2(player, position)) {
					event.setCancelled(true);
				}
			}
		} else {
			
		}
	}
}
