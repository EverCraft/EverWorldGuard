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
package fr.evercraft.everworldguard.protection.flags;

import java.util.Iterator;
import java.util.Optional;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagInventoryDrop extends StateFlag {

	private final EverWorldGuard plugin;

	public FlagInventoryDrop(EverWorldGuard plugin) {
		super("INVENTORY_DROP");
		
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INVENTORY_DROP_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	/*
	 * DropItemEvent.Pre
	 */
	
	// Permet de conserver l'inventaire
	public void onDropItemPre(DropItemEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		
		Player player = optPlayer.get();
		if (player.get(Keys.HEALTH).orElse(0.0) > 0.0) return;
		
		if (this.plugin.getProtectionService().getOrCreateWorld(player.getWorld()).getRegions(player.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
	
	/*
	 * RespawnPlayerEvent
	 */

	// Permet de redonner l'inventaire
	public void onRespawnPlayer(RespawnPlayerEvent event) {
		if (this.plugin.getProtectionService().getOrCreateWorld(event.getOriginalPlayer().getWorld())
				.getRegions(event.getOriginalPlayer().getLocation().getPosition()).getFlag(event.getOriginalPlayer(), this).equals(State.ALLOW)) return;
		
		Iterator<Inventory> originalInventory = event.getOriginalPlayer().getInventory().query(PlayerInventory.class).slots().iterator();
		Iterator<Inventory> targetInventory = event.getTargetEntity().getInventory().query(PlayerInventory.class).slots().iterator();
		
		while (originalInventory.hasNext() && targetInventory.hasNext()) {
			Inventory originalSlot = originalInventory.next();
			Inventory targetSlot = targetInventory.next();
			
			Optional<ItemStack> item = originalSlot.poll();
			if (item.isPresent()) {
				targetSlot.offer(item.get());
			}
		}
	}
}
