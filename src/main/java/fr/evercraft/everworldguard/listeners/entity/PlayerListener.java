/*
 * This file is part of EverWorldGuard.
 *
 * EverWorldGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverWorldGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverWorldGuard.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.listeners.entity;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.subject.EUserSubject;

public class PlayerListener {
	
	private EverWorldGuard plugin;

	public PlayerListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Auth event) {
		this.plugin.getProtectionService().getSubjectList().get(event.getProfile().getUniqueId());
	}
	
	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Join event) {
		EUserSubject player = this.plugin.getProtectionService().getSubjectList().registerPlayer(event.getTargetEntity().getUniqueId());
		player.initialize(event.getTargetEntity());
	}

	@Listener
	public void onClientConnectionEvent(final ClientConnectionEvent.Disconnect event) {
		this.plugin.getProtectionService().getSubjectList().removePlayer(event.getTargetEntity().getUniqueId());
	}
	
	@Listener(order=Order.FIRST)
	public void onRespawnPlayer(RespawnPlayerEvent event) {
		this.plugin.getManagerFlags().INVENTORY_DROP.onRespawnPlayer(event);
	}
	
	@Listener(order=Order.FIRST)
	public void onDestructEntityDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player player) {
	}
	
	@Listener(order=Order.FIRST)
	public void onMessageChannelChat(MessageChannelEvent.Chat event, @First Player player) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld());
		
		this.plugin.getManagerFlags().CHAT.onMessageChannelChat(event, world, player);
	}
	
	@Listener(order=Order.FIRST)
	public void onSendCommand(SendCommandEvent event) {		
		this.plugin.getManagerFlags().COMMAND.onSendCommand(event);
	}
	
	@Listener
	public void onChangeInventoryPickup(ChangeInventoryEvent.Pickup event, @First Player player) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld());
		
		this.plugin.getManagerFlags().ITEM_PICKUP.onChangeInventoryPickup(event, world, player);
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveEntityTeleport(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player_sponge) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(player_sponge.getWorld());
		
		this.plugin.getManagerFlags().ENDERPEARL.onMoveEntityTeleport(event, world, player_sponge);
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveRegionPreCancelled(MoveRegionEvent.Pre.Cancellable event) {
		this.plugin.getManagerFlags().EXIT.onMoveRegionPreCancellable(event);
		this.plugin.getManagerFlags().ENTRY.onMoveRegionPreCancellable(event);
		
		if (event.isCancelled()) {
			this.plugin.getManagerFlags().EXIT_DENY_MESSAGE.onMoveRegionPreCancelled(event);
			this.plugin.getManagerFlags().ENTRY_DENY_MESSAGE.onMoveRegionPreCancelled(event);
		}
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveRegionPost(MoveRegionEvent.Post event) {
		this.plugin.getManagerFlags().EXIT_MESSAGE.onMoveRegionPost(event);
		this.plugin.getManagerFlags().ENTRY_MESSAGE.onMoveRegionPost(event);
	}
}
