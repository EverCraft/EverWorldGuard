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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.SetFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteract extends SetFlag<BlockType> {
	
	public final BlockType[] INVENTORYS = {
			BlockTypes.CHEST,
			BlockTypes.TRAPPED_CHEST,
			BlockTypes.ENDER_CHEST,
			BlockTypes.FURNACE,
			BlockTypes.DROPPER,
			BlockTypes.DISPENSER,
			BlockTypes.HOPPER,
			BlockTypes.CRAFTING_TABLE,
			BlockTypes.ENCHANTING_TABLE,
			BlockTypes.ANVIL,
			BlockTypes.BLACK_SHULKER_BOX,
			BlockTypes.BLUE_SHULKER_BOX,
			BlockTypes.BROWN_SHULKER_BOX,
			BlockTypes.CYAN_SHULKER_BOX,
			BlockTypes.GRAY_SHULKER_BOX,
			BlockTypes.GREEN_SHULKER_BOX,
			BlockTypes.LIGHT_BLUE_SHULKER_BOX,
			BlockTypes.LIME_SHULKER_BOX,
			BlockTypes.MAGENTA_SHULKER_BOX,
			BlockTypes.ORANGE_SHULKER_BOX,
			BlockTypes.PINK_SHULKER_BOX,
			BlockTypes.PURPLE_SHULKER_BOX,
			BlockTypes.RED_SHULKER_BOX,
			BlockTypes.SILVER_SHULKER_BOX,
			BlockTypes.WHITE_SHULKER_BOX,
			BlockTypes.YELLOW_SHULKER_BOX,
			BlockTypes.JUKEBOX,
			BlockTypes.BREWING_STAND,
			BlockTypes.CAULDRON,
			BlockTypes.END_PORTAL
		};
	
	public final BlockType[] REDSTONES = {
			BlockTypes.LEVER,
			BlockTypes.STONE_PRESSURE_PLATE,
			BlockTypes.WOODEN_PRESSURE_PLATE,
			BlockTypes.STONE_BUTTON,
			BlockTypes.WOODEN_BUTTON,
			BlockTypes.TRIPWIRE_HOOK,
			BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE,
			BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE,
			BlockTypes.DAYLIGHT_DETECTOR,
			BlockTypes.DAYLIGHT_DETECTOR_INVERTED,
			BlockTypes.HOPPER,
			BlockTypes.POWERED_REPEATER,
			BlockTypes.UNPOWERED_REPEATER,
			BlockTypes.POWERED_COMPARATOR,
			BlockTypes.UNPOWERED_COMPARATOR,
			BlockTypes.NOTEBLOCK,
		};
	
	public final BlockType[] DOORS = {
			BlockTypes.ACACIA_DOOR,
			BlockTypes.BIRCH_DOOR,
			BlockTypes.DARK_OAK_DOOR,
			BlockTypes.IRON_DOOR,
			BlockTypes.JUNGLE_DOOR,
			BlockTypes.SPRUCE_DOOR,
			BlockTypes.WOODEN_DOOR,
			BlockTypes.ACACIA_FENCE_GATE,
			BlockTypes.BIRCH_FENCE_GATE,
			BlockTypes.DARK_OAK_FENCE_GATE,
			BlockTypes.FENCE_GATE,
			BlockTypes.JUNGLE_FENCE_GATE,
			BlockTypes.SPRUCE_FENCE_GATE,
			BlockTypes.TRAPDOOR,
			BlockTypes.IRON_TRAPDOOR
		};
	
	private final EverWorldGuard plugin;
	private final Set<BlockType> defaults; 
	
	public FlagInteract(EverWorldGuard plugin) {
		super("INTERACT");
		
		this.plugin = plugin;
		
		this.defaults = new HashSet<BlockType>();
		Collections.addAll(this.defaults, INVENTORYS);
		Collections.addAll(this.defaults, REDSTONES);
		Collections.addAll(this.defaults, DOORS);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT.getString();
	}

	@Override
	public Set<BlockType> getDefault() {
		return this.defaults;
	}

	@Override
	public Collection<String> getSuggestAdd(List<String> args) {
		return this.plugin.getGame().getRegistry().getAllOf(BlockType.class).stream()
				.map(type -> type.getId())
				.filter(suggest -> !args.stream().anyMatch(arg -> arg.equalsIgnoreCase(suggest)))
				.collect(Collectors.toList());
	}

	@Override
	public String subSerialize(BlockType value) {
		return value.getId();
	}

	@Override
	public BlockType subDeserialize(String value) throws IllegalArgumentException {
		return this.plugin.getGame().getRegistry()
				.getType(BlockType.class, value)
				.orElseThrow(() -> new IllegalArgumentException());
	}

	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockPlayer(event, optPlayer.get());
		} else {
			this.onChangeBlockNatural(event);
		}
	}
	
	public void onChangeBlockPlayer(ChangeBlockEvent.Modify event, Player player) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());	
		
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			if (this.getDefault().contains(type) && world.getRegions(transaction.getOriginal().getPosition()).getFlag(player, Flags.INTERACT).contains(transaction.getOriginal().getState().getType())) {
				//this.plugin.getEServer().broadcast("Player.Modify : Flags : " + transaction.getOriginal().getState().getType().getId());
				transaction.setValid(false);
			} else {
				this.plugin.getEServer().broadcast("Player.Modify : No : " + transaction.getOriginal().getState().getType().getId());
			}
		});
	}
	
	public void onChangeBlockNatural(ChangeBlockEvent.Modify event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		event.getTransactions().forEach(transaction -> {
			BlockType type = transaction.getOriginal().getState().getType();
			if (this.getDefault().contains(type) && !world.getRegions(transaction.getOriginal().getPosition()).getFlagDefault(Flags.INTERACT).contains(type)) {
				this.plugin.getEServer().broadcast("Natural.Modify : Flags : " + transaction.getOriginal().getState().getType().getId());
				transaction.setValid(false);
			} else {
				//this.plugin.getEServer().broadcast("Natural.Modify : No : " + transaction.getOriginal().getState().getType().getId());
			}
		});
	}
}
