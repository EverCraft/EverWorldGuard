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
package fr.evercraft.everworldguard.protection.flag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWFlagConfig extends EConfig<EverWorldGuard> {

	public EWFlagConfig(final EverWorldGuard plugin) {
		super(plugin, "flags");
	}
	
	@Override
	public void loadDefault() {
		this.loadInteract();
	}
	
	public void loadInteract() {
		Map<String, List<String>> interact = new HashMap<String, List<String>>();
		
		interact.put("GROUP_INVENTORY", Arrays.asList(
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
				BlockTypes.END_PORTAL)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		interact.put("GROUP_REDSTONE", Arrays.asList(
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
				BlockTypes.NOTEBLOCK)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		interact.put("GROUP_DOOR", Arrays.asList(
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
				BlockTypes.IRON_TRAPDOOR)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		addDefault("interact", interact);
	}
	
	public Map<String, Set<BlockType>> getInteracts() {
		Map<String, Set<BlockType>> groups = new HashMap<String, Set<BlockType>>();
		this.get("interact").getChildrenMap().forEach((group, list) -> {
			Set<BlockType> blocks = new HashSet<BlockType>();
			list.getChildrenList().forEach(block_config -> {
				Optional<BlockType> block = this.plugin.getGame().getRegistry().getType(BlockType.class, block_config.getString(""));
				if (block.isPresent()) {
					blocks.add(block.get());
				} else {
					this.plugin.getLogger().warn("[Flag][Config][Interact] Error : BlockType '" + block_config.getString("") + "'");
				}
			});
			groups.put(group.toString().toUpperCase(), blocks);
		});
		return groups;
	}
}
