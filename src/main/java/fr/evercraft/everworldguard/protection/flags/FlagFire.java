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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.fire.FireType;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.CatalogTypeFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagFire extends CatalogTypeFlag<FireType> {

	private final EverWorldGuard plugin;
	
	private final Map<EntityType, Set<FireType>> entities;
	private final Map<BlockType, Set<FireType>> blocks;
	private final Map<ItemType, Set<FireType>> items;
	
	public FlagFire(EverWorldGuard plugin) {
		super("FIRE");
		
		this.plugin = plugin;
		
		this.entities = new ConcurrentHashMap<EntityType, Set<FireType>>();
		this.blocks = new ConcurrentHashMap<BlockType, Set<FireType>>();
		this.items = new ConcurrentHashMap<ItemType, Set<FireType>>();
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<FireType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), FireType.class);
	}
	
	public void reload() {
		super.reload();
		
		this.entities.clear();
		this.blocks.clear();
		this.items.clear();
		
		for (FireType fire : this.defaults.getValues()) {
			fire.getEntities().forEach(entity -> {
				Set<FireType> fires = this.entities.get(entity);
				if (fires == null) {
					fires = new HashSet<FireType>();
					this.entities.put(entity, fires);
				}
				fires.add(fire);
			});
			fire.getBlocks().forEach(block -> {
				Set<FireType> fires = this.entities.get(block);
				if (fires == null) {
					fires = new HashSet<FireType>();
					this.blocks.put(block, fires);
				}
				fires.add(fire);
			});
			fire.getItems().forEach(item -> {
				Set<FireType> fires = this.entities.get(item);
				if (fires == null) {
					fires = new HashSet<FireType>();
					this.items.put(item, fires);
				}
				fires.add(fire);
			});
		}
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_FIRE_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_FIRE_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ()));
	}

	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;
		
		List<Transaction<BlockSnapshot>> transactions = event.getTransactions().stream()
			.filter(transaction -> {
				if (!transaction.isValid()) return false;
				
				BlockType type = transaction.getFinal().getState().getType();
				if (!type.equals(BlockTypes.FIRE)) return false;
				
				return true;
			})
			.collect(Collectors.toList());
		if (transactions.isEmpty()) return;
		
		Optional<Entity> optEntity = event.getCause().get(NamedCause.SOURCE, Entity.class);
		if (optEntity.isPresent()) {
			Entity entity = optEntity.get();
			
			Set<FireType> fires = this.entities.get(entity.getType());
			if (fires == null) return;
			
			Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
			if (optPlayer.isPresent()) {
				this.onChangeBlockPlacePlayer(this.plugin.getProtectionService(), Optional.empty(), transactions, fires, optPlayer.get());
			} else {
				this.onChangeBlockPlaceNatural(this.plugin.getProtectionService(), transactions, fires);
			}
			return;
		}
		
		Optional<BlockSnapshot> optBlock = event.getCause().get(NamedCause.SOURCE, BlockSnapshot.class);
		if (optBlock.isPresent()) {
			BlockSnapshot block = optBlock.get();
			
			Set<FireType> fires = this.blocks.get(block.getState().getType());
			if (fires == null) return;
			
			Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
			if (optPlayer.isPresent()) {
				this.onChangeBlockPlacePlayer(this.plugin.getProtectionService(), Optional.empty(), transactions, fires, optPlayer.get());
			} else {
				this.onChangeBlockPlaceNatural(this.plugin.getProtectionService(), transactions, fires);
			}
			return;
		}
		
		Optional<LocatableBlock> optLocatable = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (optLocatable.isPresent()) {
			LocatableBlock block = optLocatable.get();
			
			Set<FireType> fires = this.blocks.get(block.getBlockState().getType());
			if (fires == null) return;
			
			Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
			if (optPlayer.isPresent()) {
				this.onChangeBlockPlacePlayer(this.plugin.getProtectionService(), Optional.empty(), transactions, fires, optPlayer.get());
			} else {
				this.onChangeBlockPlaceNatural(this.plugin.getProtectionService(), transactions, fires);
			}
			return;
		}
	}

	private void onChangeBlockPlacePlayer(EProtectionService service, Optional<Entity> entity, List<Transaction<BlockSnapshot>> transactions, Set<FireType> fires, Player player) {
		List<Transaction<BlockSnapshot>> result = transactions.stream()
			.filter(transaction -> {
				BlockSnapshot block = transaction.getFinal();
				if (!block.getLocation().isPresent()) return false;
				
				Location<World> location = block.getLocation().get();
				for (FireType fire : service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).getValues()) {
					if (fires.contains(fire)) {
						return false;
					}
				}
				
				transaction.setValid(false);
				return true;
			})
			.collect(Collectors.toList());
			
		if (!result.isEmpty()) {
			Transaction<BlockSnapshot> transaction = result.get(0);
			
			// Vérifie que c'est une action directe
			if(!entity.isPresent() || !entity.get().equals(player)) return;
			
			this.sendMessage(player, transaction.getOriginal().getPosition());
		}
	}
	
	private void onChangeBlockPlaceNatural(EProtectionService service, List<Transaction<BlockSnapshot>> transactions, Set<FireType> fires) {
		transactions.stream()
			.forEach(transaction -> {
				BlockSnapshot block = transaction.getFinal();
				if (!block.getLocation().isPresent()) return;
				
				Location<World> location = block.getLocation().get();				
				for (FireType fire : service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).getValues()) {
					if (fires.contains(fire)) {
						return;
					}
				}
				
				transaction.setValid(false);
			});
	}
	
	/*
	 * InteractItemEvent
	 */
	
	public void onInteractBlockSecondary(WorldWorldGuard world, InteractBlockEvent.Secondary event, Location<World> location) {
		if (event.isCancelled()) return;
				
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
				
		Optional<ItemStack> itemstack = player.getItemInHand(event.getHandType());
		if (!itemstack.isPresent()) return;
				
		Set<FireType> fires = this.items.get(itemstack.get().getItem());
		if (fires == null) return;
				
		Vector3d position = location.getPosition();
		Vector3d position2 = position.add(event.getTargetSide().asOffset());
		
		boolean access = false;
		boolean access2 = false;
		
		for (FireType fire : world.getRegions(position).getFlag(player, this).getValues()) {
			if (fires.contains(fire)) {
				access = true;
				break;
			}
		}
		
		for (FireType fire : world.getRegions(position2).getFlag(player, this).getValues()) {
			if (fires.contains(fire)) {
				access2 = true;
				break;
			}
		}
		
		if (access && access2) return;
		
		event.setUseItemResult(Tristate.FALSE);
		
		// TODO UseItemResult ne fonctionne pas
		event.setCancelled(true);
		
		this.sendMessage(player, position.toInt());
	}
}
