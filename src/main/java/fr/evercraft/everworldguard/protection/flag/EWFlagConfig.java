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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.item.ItemTypes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everworldguard.EverWorldGuard;
import ninja.leaping.configurate.ConfigurationNode;

public class EWFlagConfig extends EConfig<EverWorldGuard> {

	public EWFlagConfig(final EverWorldGuard plugin) {
		super(plugin, "flags");
	}
	
	@Override
	public void loadDefault() {
		this.loadInteractBlock();
		this.loadEntity();
		this.loadBuild();
		this.loadBlock();
		this.loadItem();
		this.loadExplosion();
	}
	
	/*
	 * Load
	 */
	
	public void loadInteractBlock() {
		Map<String, List<String>> interact_block = new HashMap<String, List<String>>();
		
		interact_block.put("GROUP_INVENTORY", Arrays.asList(
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
				BlockTypes.BREWING_STAND)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		interact_block.put("GROUP_REDSTONE", Arrays.asList(
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
				BlockTypes.POWERED_REPEATER,
				BlockTypes.UNPOWERED_REPEATER,
				BlockTypes.POWERED_COMPARATOR,
				BlockTypes.UNPOWERED_COMPARATOR,
				BlockTypes.NOTEBLOCK,
				BlockTypes.DETECTOR_RAIL,
				BlockTypes.PISTON,
				BlockTypes.STICKY_PISTON)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		interact_block.put("GROUP_DOOR", Arrays.asList(
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
		
		interact_block.put("TNT", Arrays.asList(
				BlockTypes.TNT)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		interact_block.put("GROUP_OTHERS", Arrays.asList(
				BlockTypes.BED,
				BlockTypes.END_PORTAL_FRAME,
				BlockTypes.BEACON,
				BlockTypes.MOB_SPAWNER,
				BlockTypes.JUKEBOX,
				BlockTypes.CAULDRON,
				BlockTypes.COMMAND_BLOCK)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		addDefault("INTERACT_BLOCK", interact_block);
	}
	
	public void loadEntity() {
		Map<String, List<String>> interact_entity = new HashMap<String, List<String>>();
		
		interact_entity.put("GROUP_ANIMAL", Stream.concat(
			this.plugin.getGame().getRegistry().getAllOf(EntityType.class).stream()
				.filter(entity -> !EntityTypes.UNKNOWN.equals(entity) && !EntityTypes.WOLF.equals(entity) && Animal.class.isAssignableFrom(entity.getEntityClass()))
				.map(entity -> entity.getId()),
			Arrays.asList(
					EntityTypes.SQUID.getId(),
					"evercraft:wolf_passive").stream())
			.collect(Collectors.toList()));
		
		interact_entity.put("GROUP_MONSTER", Stream.concat(
			this.plugin.getGame().getRegistry().getAllOf(EntityType.class).stream()
				.filter(entity -> !EntityTypes.UNKNOWN.equals(entity) && 
						(Monster.class.isAssignableFrom(entity.getEntityClass()) || Golem.class.isAssignableFrom(entity.getEntityClass())))
				.map(entity -> entity.getId()),
			Arrays.asList(
					EntityTypes.GUARDIAN.getId(),
					"evercraft:wolf_angry").stream())
			.collect(Collectors.toList()));
		
		interact_entity.put("GROUP_INVENTORY", Arrays.asList(
				EntityTypes.ARMOR_STAND.getId(),
				EntityTypes.CHESTED_MINECART.getId(),
				EntityTypes.FURNACE_MINECART.getId(),
				EntityTypes.HOPPER_MINECART.getId()));
		
		interact_entity.put("GROUP_OWNER", Arrays.asList(
				"evercraft:wolf_owner",
				"evercraft:horse_owner",
				"evercraft:mule_owner",
				"evercraft:ocelot_owner"));
		
		interact_entity.put("GROUP_OTHERS", Arrays.asList(
				EntityTypes.RIDEABLE_MINECART.getId(),
				EntityTypes.MOB_SPAWNER_MINECART.getId(),
				EntityTypes.COMMANDBLOCK_MINECART.getId(),
				EntityTypes.TNT_MINECART.getId(),
				EntityTypes.ENDER_CRYSTAL.getId(),
				EntityTypes.BOAT.getId(),
				EntityTypes.HOPPER_MINECART.getId(),
				EntityTypes.ITEM_FRAME.getId(),
				EntityTypes.VILLAGER.getId()));
		addDefault("INTERACT_ENTITY, DAMAGE_ENTITY, ENTITY_DAMAGE, ENTITY_SPAWNING", interact_entity);
	}
	
	public void loadBuild() {
		Map<String, List<String>> interact_entity = new HashMap<String, List<String>>();
		interact_entity.put("entities", Arrays.asList(
				EntityTypes.PAINTING.getId(),
				EntityTypes.ITEM_FRAME.getId()));
		addDefault("BUILD", interact_entity);
	}
	
	public void loadBlock() {
		Map<String, Object> blocks = new HashMap<String, Object>();
		blocks.put("GROUP_TNT", Arrays.asList(
				BlockTypes.TNT.getId()));
		blocks.put("GROUP_PISTON", Arrays.asList(
				BlockTypes.PISTON.getId(),
				BlockTypes.STICKY_PISTON.getId()));
		
		blocks.put("GROUP_LAVA", Arrays.asList(
				BlockTypes.LAVA.getId()));
		blocks.put("GROUP_WATER", Arrays.asList(
				BlockTypes.WATER.getId()));
		
		blocks.put("GROUP_BEDROCK", Arrays.asList(
				BlockTypes.BEDROCK.getId()));
		
		blocks.put("GROUP_OTHERS", "*");
		
		addDefault("BLOCK_PLACE, BLOCK_BREAK", blocks);
	}
	
	public void loadItem() {
		Map<String, Object> items = new HashMap<String, Object>();
		items.put("GROUP_TNT", Arrays.asList(
				ItemTypes.TNT.getId()));
		items.put("GROUP_PISTON", Arrays.asList(
				ItemTypes.PISTON.getId(),
				ItemTypes.STICKY_PISTON.getId()));
		
		items.put("GROUP_SWORD", Arrays.asList(
				ItemTypes.DIAMOND_SWORD.getId(),
				ItemTypes.GOLDEN_SWORD.getId(),
				ItemTypes.IRON_SWORD.getId(),
				ItemTypes.STONE_SWORD.getId(),
				ItemTypes.WOODEN_SWORD.getId()));
		
		items.put("GROUP_BEDROCK", Arrays.asList(
				ItemTypes.BEDROCK.getId()));
		
		items.put("GROUP_OTHERS", "*");
		
		addDefault("ITEM_PICKUP, ITEM_DROP", items);
	}
	
	public void loadExplosion() {
		Map<String, List<String>> interact_entity = new HashMap<String, List<String>>();
		interact_entity.put("CREEPER", Arrays.asList(EntityTypes.CREEPER.getId()));
		interact_entity.put("TNT", Arrays.asList(EntityTypes.PRIMED_TNT.getId()));
		interact_entity.put("GHAST", Arrays.asList(EntityTypes.GHAST.getId()));
		interact_entity.put("ENDER_CRYSTAL", Arrays.asList(EntityTypes.ENDER_CRYSTAL.getId()));
		interact_entity.put("WITHER", Arrays.asList(
				EntityTypes.WITHER.getId(),
				EntityTypes.WITHER_SKULL.getId()));
		
		addDefault("EXPLOSION, EXPLOSION_DAMAGE, EXPLOSION_BLOCK", interact_entity);
	}
	
	/*
	 * Accesseurs
	 */
	
	public <T extends CatalogType> Map<String, Set<T>> get(String name, Class<T> type) {
		ImmutableMap.Builder<String, Set<T>> groups = ImmutableMap.builder();
		Set<T> all = null;
		for (Entry<Object, ? extends ConfigurationNode> group : this.getContains(name).getChildrenMap().entrySet()) {
			if (group.getValue().getString("").equalsIgnoreCase("*")) {
				all = new HashSet<T>();
				groups.put(group.getKey().toString().toUpperCase(), all);
			} else {
				Set<T> set = new HashSet<T>();
				for (ConfigurationNode config : group.getValue().getChildrenList()) {
					if (group.getValue().getString("").equalsIgnoreCase("*")) {
						all = set;
					} else {
						Optional<T> optional = this.plugin.getGame().getRegistry().getType(type, config.getString(""));
						if (optional.isPresent()) {
							set.add(optional.get());
						} else {
							this.plugin.getELogger().warn("[Flag][Config][" + name + "] Error : " + type.getSimpleName() + " '" + config.getString("") + "'");
						}
					}
				}
				groups.put(group.getKey().toString().toUpperCase(), set);
			}
		}
		Map<String, Set<T>> map = groups.build();
		
		if (all != null) {
			all.addAll(this.plugin.getGame().getRegistry().getAllOf(type).stream()
				.filter(element -> {
					for (Set<T> set : map.values()) {
						if (set.contains(element)) {
							return false;
						}
					}
					return true;
				}).collect(Collectors.toSet()));
		}
		return map;
	}
	
	public Map<String, Set<EntityTemplate>> getEntities(String name) {
		ImmutableMap.Builder<String, Set<EntityTemplate>> groups = ImmutableMap.builder();
		this.getContains(name).getChildrenMap().forEach((group, list) -> {
			ImmutableSet.Builder<EntityTemplate> set = ImmutableSet.builder();
			list.getChildrenList().forEach(config -> {
				Optional<EntityTemplate> optional = this.plugin.getEverAPI().getManagerService().getEntity().getForAll(config.getString(""));
				if (optional.isPresent()) {
					set.add(optional.get());
				} else {
					this.plugin.getELogger().warn("[Flag][Config][" + name + "] Error : EntityTemplate '" + config.getString("") + "'");
				}
			});
			groups.put(group.toString().toUpperCase(), set.build());
		});
		return groups.build();
	}
	
	public ConfigurationNode getContains(String name) {
		for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
			if (config.getKey().toString().contains(name)) {
				return config.getValue();
			}
		}
		return this.get(name);
	}
}
