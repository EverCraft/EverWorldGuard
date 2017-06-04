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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.flag.CatalogTypeFlag;
import fr.evercraft.everapi.services.worldguard.flag.value.EntryFlagValue;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagItemDrop extends CatalogTypeFlag<ItemType> {

	private final EverWorldGuard plugin;

	public FlagItemDrop(EverWorldGuard plugin) {
		super("ITEM_DROP");
		
		this.plugin = plugin;
		this.reload();
	}

	@Override
	protected Map<String, Set<ItemType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), ItemType.class);
	}

	@Override
	public String getDescription() {
		return EWMessages.FLAG_ITEM_DROP_DESCRIPTION.getString();
	}

	public boolean sendMessage(Player player, Location<World> location, ItemType type) {
		Vector3i position = location.getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_ITEM_DROP_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<item>", type.getTranslation()));
	}

	/*
	 * SpawnEntityEvent
	 */
	
	public void onSpawnEntity(SpawnEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<SpawnCause> optSpawn = event.getCause().get(NamedCause.SOURCE, SpawnCause.class);
		if (!optSpawn.isPresent()) return;
		SpawnCause spawn = optSpawn.get();
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onSpawnEntityPlayer(this.plugin.getProtectionService(), event, spawn, optPlayer.get());
		} else {
			this.onSpawnEntityNatural(this.plugin.getProtectionService(), event, spawn);
		}
	}
	
	private void onSpawnEntityPlayer(EProtectionService service, SpawnEntityEvent event, SpawnCause spawn, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		List<? extends Entity> filter = event.filterEntities(entity -> {
			if (!(entity instanceof Item)) return true;
			ItemType type = ((Item) entity).getItemType();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateWorld(entity.getWorld()).getRegions(entity.getLocation().getPosition()).getFlag(player, this).containsValue(type)) {
				return false;
			}
			return true;
		});
		if (filter.isEmpty()) return;
		
		if (spawn instanceof BlockSpawnCause) {
			this.onSpawnEntityDispense((BlockSpawnCause) spawn, filter);
		} else if (spawn instanceof EntitySpawnCause && ((EntitySpawnCause) spawn).getEntity().equals(player)) {			
			this.sendMessage(player, filter.get(0).getLocation(), ((Item) filter.get(0)).getItemType());
		}
	}
	
	private void onSpawnEntityNatural(EProtectionService service, SpawnEntityEvent event, SpawnCause spawn) {		
		List<? extends Entity> filter = event.filterEntities(entity -> {
			if (!(entity instanceof Item)) return true;
			ItemType type = ((Item) entity).getItemType();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateEWorld(entity.getWorld()).getRegions(entity.getLocation().getPosition()).getFlagDefault(this).containsValue(type)) {
				return false;
			}
			return true;
		});
		if (filter.isEmpty()) return;
		
		if (spawn instanceof BlockSpawnCause) {
			this.onSpawnEntityDispense((BlockSpawnCause) spawn, filter);
		}
	}
	
	// Remet les items dans le dispenser
	private void onSpawnEntityDispense(BlockSpawnCause spawn, List<? extends Entity> filter) {
		Optional<Location<World>> location = spawn.getBlockSnapshot().getLocation();
		if (!location.isPresent() || !location.get().getTileEntity().isPresent()) return;
		
		TileEntity tile = location.get().getTileEntity().get();
		if (!(tile instanceof TileEntityCarrier)) return;
		
		Inventory inventory = ((TileEntityCarrier) tile).getInventory();
		filter.forEach(entity -> {
			Optional<ItemStackSnapshot> item = entity.get(Keys.REPRESENTED_ITEM);
			if (!item.isPresent()) return;
			
			inventory.offer(ItemStack.builder()
				.fromSnapshot(item.get())
				.build());
		});
	}

	// Uniquement pour redonner les items dans l'inventaire du joueur
	public void onDropItemPre(DropItemEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		EntryFlagValue<ItemType> flag = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld()).getRegions(player.getLocation().getPosition()).getFlag(player, this);
		
		List<ItemStackSnapshot> items = new ArrayList<ItemStackSnapshot>();
		event.getDroppedItems().removeIf(item -> {
			if (this.getDefault().containsValue(item.getType()) && !flag.containsValue(item.getType())) {
				items.add(item);
				return true;
			}
			return false;
		});
		
		
		// TODO Si l'inventaire est ouvert l'item n'est pas redonné
		/*
		// TODO Bug : player.isViewingInventory() retourne toujours True
		if (player.isViewingInventory()) {
			CarriedInventory<? extends Carrier> inventory = player.getInventory();
			items.forEach(item -> inventory.offer(
				ItemStack.builder()
					.fromSnapshot(item)
					.build()));
		}*/
		
		if (!items.isEmpty()) {
			this.sendMessage(player, player.getLocation(), items.get(0).getType());
		}
		
		// Bug : https://github.com/SpongePowered/SpongeCommon/issues/1363
		if (event.getDroppedItems().isEmpty()) {
			event.setCancelled(true);
		}
	}
}