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
package fr.evercraft.everworldguard.listeners.player;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
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
	public void onPlayerHeal(HealEntityEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().INVINCIBILITY.onPlayerHeal(world, event);
	}
	
	@Listener
	public void onPlayerDamage(DamageEntityEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().PVP.onPlayerDamage(world, event);
	}
}
