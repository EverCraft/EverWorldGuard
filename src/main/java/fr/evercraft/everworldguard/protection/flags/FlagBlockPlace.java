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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.CatalogTypeFlag;
import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagBlockPlace extends CatalogTypeFlag<BlockType> {

	private final EverWorldGuard plugin;
	
	public FlagBlockPlace(EverWorldGuard plugin) {
		super("BLOCK_PLACE");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<BlockType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), BlockType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_BLOCK_PLACE_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Location<World> location, BlockType type) {
		Vector3i position = location.getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_BLOCK_PLACE_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<block>", type.getTranslation()));
	}
	
	/*
	 * ChangeBlockEvent.Pre
	 */
	
	public void onChangeBlockPre(ChangeBlockEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> piston = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (piston.isPresent()) {
			WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(piston.get().getWorld());
			
			// Extend
			if (event.getCause().containsNamed(NamedCause.PISTON_EXTEND)) {
				Vector3d direction = piston.get().get(Keys.DIRECTION).orElse(Direction.NONE).asOffset();
				
				this.onChangeBlockPrePiston(world, event, piston.get(), direction);
				
			// Retract
			} else if (event.getCause().containsNamed(NamedCause.PISTON_RETRACT)) {
				Vector3d direction = piston.get().get(Keys.DIRECTION).orElse(Direction.NONE).getOpposite().asOffset();
				
				this.onChangeBlockPrePiston(world, event, piston.get(), direction);
			}
		}
	}
	
	private void onChangeBlockPrePiston(WorldWorldGuard world, ChangeBlockEvent.Pre event, LocatableBlock block, Vector3d direction) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			
			if (event.getLocations().stream().anyMatch(location -> 
					this.getDefault().containsValue(location.getBlockType()) && 
					!world.getRegions(location.getPosition().add(direction)).getFlag(player, this).containsValue(location.getBlockType()))) {
				event.setCancelled(true);
			}
		} else {
			if (event.getLocations().stream().anyMatch(location -> 
					this.getDefault().containsValue(location.getBlockType()) && 
					!world.getRegions(location.getPosition().add(direction)).getFlagDefault(this).containsValue(location.getBlockType()))) {
				event.setCancelled(true);
			}
		}
	}

	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;		
		
		Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
		if (falling.isPresent()) {
			this.onChangeBlockPlaceFalling(this.plugin.getProtectionService(), event, falling.get());
		} else {
			this.onChangeBlockPlaceNoFalling(this.plugin.getProtectionService(), event);
		}
	}
	
	// Drop l'item au sol pour le bloc avec gravité
	private void onChangeBlockPlaceFalling(EProtectionService service, ChangeBlockEvent.Place event, FallingBlock falling) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		
		// Player
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			event.getTransactions().stream().filter(transaction -> this.onChangeBlockPlace(service, transaction, player))
				.forEach(transaction -> {
					BlockSnapshot block = transaction.getFinal();
					Location<World> location = block.getLocation().get();
					
					Entity entity = location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
					entity.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().fromBlockSnapshot(transaction.getFinal()).build().createSnapshot());
					entity.setCreator(player.getUniqueId());
					
					location.getExtent().spawnEntity(entity, Cause.source(
						EntitySpawnCause.builder().entity(entity).type(SpawnTypes.PLUGIN).build())
						.from(event.getCause())
						.named(UtilsCause.PLACE_EVENT, event).build());
				});
		
		// Natural
		} else {
			event.getTransactions().stream().filter(transaction -> this.onChangeBlockPlace(service, transaction))
				.forEach(transaction -> transaction.getFinal().getLocation().ifPresent(location -> {
					Entity entity = location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
					entity.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().fromBlockSnapshot(transaction.getFinal()).build().createSnapshot());
					
					location.getExtent().spawnEntity(entity, Cause
						.source(EntitySpawnCause.builder().entity(entity).type(SpawnTypes.PLUGIN).build())
						.from(event.getCause())
						.named(UtilsCause.PLACE_EVENT, event).build());
				}));
		}
	}
	
	// Placement de bloc
	private void onChangeBlockPlaceNoFalling(EProtectionService service, ChangeBlockEvent.Place event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		
		// Player
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			
			List<Transaction<BlockSnapshot>> transactions = event.getTransactions().stream()
				.filter(transaction -> this.onChangeBlockPlace(service, transaction, player))
				.collect(Collectors.toList());
			
			if (!transactions.isEmpty()) {
				Transaction<BlockSnapshot> transaction = transactions.get(0);
				this.sendMessage(player, transaction.getOriginal().getLocation().get(), transaction.getFinal().getState().getType());
			}
			
		// Natural
		} else {
			event.getTransactions().stream()
				.forEach(transaction -> this.onChangeBlockPlace(service, transaction));
		}
	}
	
	private boolean onChangeBlockPlace(EProtectionService service, Transaction<BlockSnapshot> transaction, Player player) {
		if (!transaction.isValid()) return false;
		
		BlockSnapshot block = transaction.getFinal();
		if (!block.getLocation().isPresent()) return false;
		
		BlockType type = block.getState().getType();
		if (!this.getDefault().containsValue(type)) return false;
		
		Location<World> location = block.getLocation().get();
		if (!service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).containsValue(type)) {
			transaction.setValid(false);
			return true;
		}
		return false;
	}
	
	private boolean onChangeBlockPlace(EProtectionService service, Transaction<BlockSnapshot> transaction) {
		if (!transaction.isValid()) return false;
		
		BlockSnapshot block = transaction.getFinal();
		if (!block.getLocation().isPresent()) return false;
		
		BlockType type = block.getState().getType();
		if (!this.getDefault().containsValue(type)) return false;
		
		Location<World> location = block.getLocation().get();
		if (!service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).containsValue(type)) {
			transaction.setValid(false);
			return true;
		}
		return false;
	}
}
