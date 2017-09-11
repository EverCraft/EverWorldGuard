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
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.Flags;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.CatalogTypeFlag;
import fr.evercraft.everapi.sponge.UtilsBlockType;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteractBlock extends CatalogTypeFlag<BlockType> {
	
	private final EverWorldGuard plugin;
	
	public FlagInteractBlock(EverWorldGuard plugin) {
		super("INTERACT_BLOCK");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<BlockType>> getConfig() {
		return this.plugin.getConfigFlags().get(this.getName(), BlockType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT_BLOCK_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Location<World> location, BlockType type) {
		Vector3i position = location.getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_INTERACT_BLOCK_MESSAGE.sender()
					.replace("{x}", position.getX())
					.replace("{y}", position.getY())
					.replace("{z}", position.getZ())
					.replace("{block}", type.getTranslation()));
	}
	
	/*
	 * InteractBlockEvent.Secondary
	 */

	public void onInteractBlockSecondary(WorldGuardWorld world, InteractBlockEvent.Secondary event, Location<World> location) {
		if (event.isCancelled()) return;
		
		BlockType type = event.getTargetBlock().getState().getType();
		if (!this.getDefault().containsValue(type)) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockPlayer(world, event, location, type, optPlayer.get());
		} else {
			this.onChangeBlockNatural(world, event, location, type);
		}
	}
	
	private void onChangeBlockPlayer(WorldGuardWorld world, InteractBlockEvent.Secondary event, Location<World> location, BlockType type, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		// Fix : Bug d'affichage de la porte
		if (type.equals(BlockTypes.ACACIA_DOOR) || type.equals(BlockTypes.BIRCH_DOOR) || type.equals(BlockTypes.DARK_OAK_DOOR)
				|| type.equals(BlockTypes.JUNGLE_DOOR) || type.equals(BlockTypes.SPRUCE_DOOR) || type.equals(BlockTypes.WOODEN_DOOR)) return;
		
		if (!world.getRegions(location.getPosition()).getFlag(player, location, this).containsValue(type)) {
			event.setUseBlockResult(Tristate.FALSE);
			
			// Message
			this.sendMessage(player, location, type);
		}
	}
	
	private void onChangeBlockNatural(WorldGuardWorld world, InteractBlockEvent.Secondary event, Location<World> location, BlockType type) {
		if (!world.getRegions(location.getPosition()).getFlagDefault(this).containsValue(type)) {
			event.setUseBlockResult(Tristate.FALSE);
		}
	}
	
	/*
	 * CollideBlockEvent
	 */

	public void onCollideBlock(WorldGuardWorld world, CollideBlockEvent event) {
		if (event.isCancelled()) return;
		
		BlockType type = event.getTargetBlock().getType();
		if (!this.getDefault().containsValue(type)) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (optPlayer.isPresent()) {
			this.onCollideBlockPlayer(world, event, type, optPlayer.get());
		} else {
			this.onCollideBlockNatural(world, event, type);
		}
	}
	
	private void onCollideBlockPlayer(WorldGuardWorld world, CollideBlockEvent event, BlockType type, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		Location<World> location = event.getTargetLocation();
		if (!world.getRegions(location.getPosition()).getFlag(player, location, this).containsValue(type)) {
			event.setCancelled(true);
			
			// Message
			if (UtilsBlockType.BLOCK_COLLISION.contains(type)) {
				this.sendMessage(player, event.getTargetLocation(), type);
			}
		}
	}
	
	private void onCollideBlockNatural(WorldGuardWorld world, CollideBlockEvent event, BlockType type) {
		if (!world.getRegions(event.getTargetLocation().getPosition()).getFlagDefault(this).containsValue(type)) {
			event.setCancelled(true);
		}
	}
	
	/*
	 * ChangeBlockEvent.Modify
	 */
	
	public void onChangeBlockModify(ChangeBlockEvent.Modify event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockModifyPlayer(this.plugin.getProtectionService(), event, optPlayer.get());
		} else {
			this.onChangeBlockModifyNatural(this.plugin.getProtectionService(), event);
		}
	}
	
	private void onChangeBlockModifyPlayer(EProtectionService service, ChangeBlockEvent.Modify event, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		List<Transaction<BlockSnapshot>> filter = new ArrayList<Transaction<BlockSnapshot>>();
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			
			// Fix : Bug d'affichage des coffres
			if (type.equals(BlockTypes.CHEST) || type.equals(BlockTypes.TRAPPED_CHEST)) return;
			
			// Fix : Bypass
			if (transaction.getOriginal().get(Keys.POWERED).orElse(false) && !transaction.getFinal().get(Keys.POWERED).orElse(true)) return;
			
			Location<World> location = transaction.getOriginal().getLocation().get();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateEWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, location, this).containsValue(type)) {
				transaction.setValid(false);
				filter.add(transaction);
				
				transaction.setCustom(transaction.getOriginal());
			}
		});
		
		if (!filter.isEmpty()) {
			BlockSnapshot block = filter.get(0).getOriginal();
			// Message
			this.sendMessage(player, block.getLocation().get(), block.getState().getType());
		}
	}
	
	private void onChangeBlockModifyNatural(EProtectionService service, ChangeBlockEvent.Modify event) {
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			
			// Fix : Bug d'affichage des coffres
			if (type.equals(BlockTypes.CHEST) || type.equals(BlockTypes.TRAPPED_CHEST)) return;
			
			Location<World> location = transaction.getOriginal().getLocation().get();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateEWorld(location.getExtent()).getRegions(transaction.getOriginal().getPosition()).getFlagDefault(this).containsValue(type)) {
				event.setCancelled(true);
			}
		});
	}
	
	/*
	 * ChangeBlockEvent.Pre : Désactive les pistons si le bouton est à l'extérieur de la région
	 */
	
	public void onChangeBlockPre(ChangeBlockEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> block = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (!block.isPresent()) return;
		
		BlockType type = block.get().getBlockState().getType();
		if (!type.equals(BlockTypes.PISTON) && !type.equals(BlockTypes.STICKY_PISTON)) return;
		if (!this.getDefault().containsValue(type)) return;
			
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		Location<World> location = block.get().getLocation();
		if (optPlayer.isPresent()) {
			// Bypass
			if (this.plugin.getProtectionService().hasBypass(optPlayer.get())) return;
			
			if (!this.plugin.getProtectionService().getOrCreateEWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(optPlayer.get(), location, this).containsValue(type)) {
				event.setCancelled(true);
			}
		} else {
			if (!this.plugin.getProtectionService().getOrCreateEWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).containsValue(type)) {
				event.setCancelled(true);
			}
		}
	}
	
	
	public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		if (!event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockBreakPlayer(this.plugin.getProtectionService(), event, optPlayer.get());
		} else {
			this.onChangeBlockBreakNatural(this.plugin.getProtectionService(), event);
		}
	}
	
	private void onChangeBlockBreakPlayer(EProtectionService service, ChangeBlockEvent.Break event, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		List<Transaction<BlockSnapshot>> filter = event.getTransactions().stream().filter(transaction -> {
			Location<World> location = transaction.getOriginal().getLocation().get();
			BlockType type = transaction.getOriginal().getState().getType();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateEWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, location, Flags.INTERACT_BLOCK).containsValue(type)) {
				transaction.setValid(false);
				
				return true;
			}
			return false;
		}).collect(Collectors.toList());
		
		if (!filter.isEmpty()) {
			BlockSnapshot block = filter.get(0).getOriginal();
			// Message
			this.sendMessage(player, block.getLocation().get(), block.getState().getType());
		}
	}
	
	private void onChangeBlockBreakNatural(EProtectionService service, ChangeBlockEvent.Break event) {
		event.getTransactions().forEach(transaction -> {
			Location<World> location = transaction.getOriginal().getLocation().get();
			BlockType type = transaction.getOriginal().getState().getType();
			
			if (this.getDefault().containsValue(type) && 
					!service.getOrCreateEWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(Flags.INTERACT_BLOCK).containsValue(type)) {
				transaction.setValid(false);
			}
		});
	}
}
