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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.collect.Sets;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.SetFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteractBlock extends SetFlag<BlockType> {
	
	private static final String ALL = "ALL";
	
	private final EverWorldGuard plugin;
	private final Map<String, Set<BlockType>> groups;
	private final Set<BlockType> types;
	
	public FlagInteractBlock(EverWorldGuard plugin) {
		super("INTERACT_BLOCK");
		
		this.plugin = plugin;
		
		this.groups = new ConcurrentHashMap<String, Set<BlockType>>();
		this.types = Sets.newConcurrentHashSet();
		
		this.reload();
	}
	
	public void reload() {
		this.groups.clear();
		this.types.clear();
		
		this.groups.putAll(this.plugin.getProtectionService().getConfigFlags().getInteracts());
		this.groups.forEach((group, blocks) -> this.types.addAll(blocks));
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT_BLOCK.getString();
	}

	@Override
	public Set<BlockType> getDefault() {
		return this.types;
	}

	@Override
	public Collection<String> getSuggestAdd(List<String> args) {
		return Stream.concat(
				this.groups.keySet().stream(),
				Stream.of(ALL))
			.filter(suggest -> !args.stream().anyMatch(arg -> arg.equalsIgnoreCase(suggest)))
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getSuggestRemove(List<String> args) {
		return Stream.concat(
				this.groups.keySet().stream(),
				Stream.of(ALL))
			.filter(suggest -> !args.stream().anyMatch(arg -> arg.equalsIgnoreCase(suggest)))
			.collect(Collectors.toList());
	}
	
	@Override
	public String subSerialize(BlockType value) {
		for (Entry<String, Set<BlockType>> group : this.groups.entrySet()) {
			for (BlockType block : group.getValue()) {
				if (block.equals(value)) {
					return group.getKey();
				}
			}
		}
		return "";
	}

	@Override
	public Set<BlockType> subDeserialize(String value) throws IllegalArgumentException {
		if (value.equalsIgnoreCase(ALL)) return this.types;
		
		Set<BlockType> group = this.groups.get(value.toUpperCase());
		if (group != null) {
			return group;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Listener(order=Order.FIRST)
	public void onChangeBlock(InteractBlockEvent.Secondary event) {
		if (event.isCancelled()) return;
			event.getTargetBlock().getLocation().ifPresent(location -> {
			Optional<Player> optPlayer = event.getCause().first(Player.class);
			if (optPlayer.isPresent()) {
				this.onChangeBlockPlayer(event, location, optPlayer.get());
			} else {
				this.onChangeBlockNatural(event, location);
			}
		});
	}
	
	public void onChangeBlockPlayer(InteractBlockEvent.Secondary event, Location<World> location, Player player) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(location.getExtent());	
		
		BlockType type = event.getTargetBlock().getState().getType();
		if (this.getDefault().contains(type) && !world.getRegions(location.getPosition()).getFlag(player, Flags.INTERACT_BLOCK).contains(type)) {
			/*this.plugin.getEServer().broadcast("InteractBlockEvent : Player : Cancel : " + type.getId());
			this.plugin.getEServer().broadcast("    - UseBlockResult : " + event.getUseBlockResult());
			this.plugin.getEServer().broadcast("    - UseItemResult : " + event.getUseItemResult());
			this.plugin.getEServer().broadcast("    - OriginalUseBlockResult : " + event.getOriginalUseBlockResult());
			this.plugin.getEServer().broadcast("    - OriginalUseItemResult : " + event.getOriginalUseItemResult());*/
			event.setUseBlockResult(Tristate.FALSE);
		} else {
			/*this.plugin.getEServer().broadcast("InteractBlockEvent : Player : No : " + type.getId());
			this.plugin.getEServer().broadcast("    - UseBlockResult : " + event.getUseBlockResult());
			this.plugin.getEServer().broadcast("    - UseItemResult : " + event.getUseItemResult());
			this.plugin.getEServer().broadcast("    - OriginalUseBlockResult : " + event.getOriginalUseBlockResult());
			this.plugin.getEServer().broadcast("    - OriginalUseItemResult : " + event.getOriginalUseItemResult());*/
		}
	}
	
	public void onChangeBlockNatural(InteractBlockEvent.Secondary event, Location<World> location) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(location.getExtent());
		BlockType type = event.getTargetBlock().getState().getType();
		if (this.getDefault().contains(type) && !world.getRegions(location.getPosition()).getFlagDefault(Flags.INTERACT_BLOCK).contains(type)) {
			/*this.plugin.getEServer().broadcast("InteractBlockEvent : Natural : Cancel : " + type.getId());
			this.plugin.getEServer().broadcast("    - UseBlockResult : " + event.getUseBlockResult());
			this.plugin.getEServer().broadcast("    - UseItemResult : " + event.getUseItemResult());
			this.plugin.getEServer().broadcast("    - OriginalUseBlockResult : " + event.getOriginalUseBlockResult());
			this.plugin.getEServer().broadcast("    - OriginalUseItemResult : " + event.getOriginalUseItemResult());*/
			event.setUseBlockResult(Tristate.FALSE);
		} else {
			/*this.plugin.getEServer().broadcast("InteractBlockEvent : Natural : No : " + type.getId());
			this.plugin.getEServer().broadcast("    - UseBlockResult : " + event.getUseBlockResult());
			this.plugin.getEServer().broadcast("    - UseItemResult : " + event.getUseItemResult());
			this.plugin.getEServer().broadcast("    - OriginalUseBlockResult : " + event.getOriginalUseBlockResult());
			this.plugin.getEServer().broadcast("    - OriginalUseItemResult : " + event.getOriginalUseItemResult());*/
		}
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockModifyPlayer(event, optPlayer.get());
		} else {
			this.onChangeBlockModifyNatural(event);
		}
	}
	
	public void onChangeBlockModifyPlayer(ChangeBlockEvent.Modify event, Player player) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());	
		
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			if (this.getDefault().contains(type) && !world.getRegions(transaction.getOriginal().getPosition()).getFlag(player, Flags.INTERACT_BLOCK).contains(type)) {
				//this.plugin.getEServer().broadcast("ChangeBlockEvent.Modify : Player : Flags : " + type.getId());
				event.setCancelled(true);
			} else {
				//this.plugin.getEServer().broadcast("ChangeBlockEvent.Modify : Player : No : " + type.getId());
			}
		});
	}
	
	public void onChangeBlockModifyNatural(ChangeBlockEvent.Modify event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			if (this.getDefault().contains(type) && !world.getRegions(transaction.getOriginal().getPosition()).getFlagDefault(Flags.INTERACT_BLOCK).contains(type)) {
				//this.plugin.getEServer().broadcast("ChangeBlockEvent.Modify : Natural : Flags : " + type.getId());
				event.setCancelled(true);
			} else {
				//this.plugin.getEServer().broadcast("ChangeBlockEvent.Modify : Natural : No : " + type.getId());
			}
		});
	}
}
