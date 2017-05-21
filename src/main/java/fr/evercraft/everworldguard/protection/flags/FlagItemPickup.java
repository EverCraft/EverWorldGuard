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

import java.util.Map;
import java.util.Set;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.CatalogTypeFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagItemPickup extends CatalogTypeFlag<ItemType> {

	private final EverWorldGuard plugin;

	public FlagItemPickup(EverWorldGuard plugin) {
		super("ITEM_PICKUP");
		
		this.plugin = plugin;
		this.reload();
	}

	@Override
	protected Map<String, Set<ItemType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), ItemType.class);
	}

	@Override
	public String getDescription() {
		return EWMessages.FLAG_ITEM_PICKUP_DESCRIPTION.getString();
	}

	public boolean sendMessage(Player player, Location<World> location, ItemType type) {
		Vector3i position = location.getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_ITEM_PICKUP_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<item>", type.getTranslation()));
	}

	/*
	 * ChangeInventoryEvent.Pickup
	 */
	
	public void onChangeInventoryPickup(ChangeInventoryEvent.Pickup event, WorldWorldGuard world, Player player) {
		if (event.isCancelled()) return;
		
		ItemType type = event.getTargetEntity().getItemType();
		if (!this.getDefault().containsValue(type)) return;
		
		// Position du joueur
		if (!world.getRegions(player.getLocation().getPosition()).getFlag(player, this).containsValue(type)) {
			event.setCancelled(true);
			this.sendMessage(player, player.getLocation(), type);
			
		// Position de l'item
		} else if (!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, this).containsValue(type)) {
			event.setCancelled(true);
			this.sendMessage(player, player.getLocation(), type);
		}
	}

}