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
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.item.ItemTypes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.registers.ChatType;
import fr.evercraft.everapi.registers.IceType;
import fr.evercraft.everapi.registers.SnowType;
import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.entity.EntityTemplates;
import fr.evercraft.everapi.services.fire.FireType;
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
		this.loadFire();
		this.loadSnow();
		this.loadIce();
		this.loadChat();
		this.loadPropagation();
		this.loadPotion();
		this.loadCommand();
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
				BlockTypes.COMMAND_BLOCK,
				BlockTypes.FLOWER_POT)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		addDefault(Arrays.asList("INTERACT_BLOCK"), interact_block);
	}

	public void loadEntity() {
		Map<String, List<String>> interact_entity = new HashMap<String, List<String>>();
		
		interact_entity.put("GROUP_ANIMAL", Stream.concat(
			this.plugin.getGame().getRegistry().getAllOf(EntityType.class).stream()
				.filter(entity -> !EntityTypes.UNKNOWN.equals(entity) && !EntityTypes.WOLF.equals(entity) && Animal.class.isAssignableFrom(entity.getEntityClass()))
				.map(entity -> entity.getId()),
			Arrays.asList(
					EntityTypes.SQUID.getId(),
					EntityTemplates.WOLF_PASSIVE.getId()).stream())
			.collect(Collectors.toList()));
		
		interact_entity.put("GROUP_MONSTER", Stream.concat(
			this.plugin.getGame().getRegistry().getAllOf(EntityType.class).stream()
				.filter(entity -> !EntityTypes.UNKNOWN.equals(entity) && 
						(Monster.class.isAssignableFrom(entity.getEntityClass()) || Golem.class.isAssignableFrom(entity.getEntityClass())))
				.map(entity -> entity.getId()),
			Arrays.asList(
					EntityTypes.GUARDIAN.getId(),
					EntityTemplates.WOLF_ANGRY.getId()).stream())
			.collect(Collectors.toList()));
		
		interact_entity.put("GROUP_INVENTORY", Arrays.asList(
				EntityTypes.ARMOR_STAND.getId(),
				EntityTypes.CHESTED_MINECART.getId(),
				EntityTypes.FURNACE_MINECART.getId(),
				EntityTypes.HOPPER_MINECART.getId()));
		
		interact_entity.put("GROUP_OWNER", Arrays.asList(
				EntityTemplates.WOLF_OWNER.getId(),
				EntityTemplates.HORSE_OWNER.getId(),
				EntityTemplates.MULE_OWNER.getId(),
				EntityTemplates.DONKEY_OWNER.getId(),
				EntityTemplates.OCELOT_OWNER.getId()));
		
		interact_entity.put("GROUP_OTHERS", Arrays.asList(
				EntityTypes.RIDEABLE_MINECART.getId(),
				EntityTypes.MOB_SPAWNER_MINECART.getId(),
				EntityTypes.COMMANDBLOCK_MINECART.getId(),
				EntityTypes.TNT_MINECART.getId(),
				EntityTypes.ENDER_CRYSTAL.getId(),
				EntityTypes.BOAT.getId(),
				EntityTypes.ITEM_FRAME.getId(),
				EntityTypes.PAINTING.getId(),
				EntityTypes.VILLAGER.getId()));
		addDefault(Arrays.asList("INTERACT_ENTITY", "DAMAGE_ENTITY", "ENTITY_DAMAGE", "ENTITY_SPAWNING"), interact_entity);
	}
	
	public void loadBuild() {
		Map<String, List<String>> interact_entity = new HashMap<String, List<String>>();
		interact_entity.put("entities", Arrays.asList(
				EntityTypes.PAINTING.getId(),
				EntityTypes.ITEM_FRAME.getId()));
		addDefault(Arrays.asList("BUILD"), interact_entity);
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
		
		addDefault(Arrays.asList("BLOCK_PLACE", "BLOCK_BREAK"), blocks);
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
		
		addDefault(Arrays.asList("ITEM_PICKUP", "ITEM_DROP"), items);
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
		
		addDefault(Arrays.asList("EXPLOSION", "EXPLOSION_DAMAGE", "EXPLOSION_BLOCK"), interact_entity);
	}
	
	public void loadPropagation() {
		Map<String, List<String>> propagation = new HashMap<String, List<String>>();
		propagation.put("VINE", Arrays.asList(BlockTypes.VINE.getId()));
		propagation.put("GRASS", Arrays.asList(BlockTypes.GRASS.getId()));
		propagation.put("MYCELIUM", Arrays.asList(BlockTypes.MYCELIUM.getId()));
		propagation.put("MUSHROOM", Arrays.asList(
				BlockTypes.RED_MUSHROOM.getId(),
				BlockTypes.BROWN_MUSHROOM.getId()));
		
		addDefault(Arrays.asList("PROPAGATION"), propagation);
	}
	
	public void loadChat() {
		Map<String, List<String>> chats = new HashMap<String, List<String>>();
		
		for (ChatType chat : this.plugin.getGame().getRegistry().getAllOf(ChatType.class)) {
			chats.put(chat.getName(), Arrays.asList(chat.getId()));
		}
		
		addDefault(Arrays.asList("CHAT"), chats);
	}
	
	public void loadFire() {
		Map<String, List<String>> fires = new HashMap<String, List<String>>();
		
		for (FireType fire : this.plugin.getGame().getRegistry().getAllOf(FireType.class)) {
			fires.put(fire.getName(), Arrays.asList(fire.getId()));
		}
		
		addDefault(Arrays.asList("FIRE"), fires);
	}
	
	public void loadSnow() {
		Map<String, List<String>> snows = new HashMap<String, List<String>>();
		
		for (SnowType snow : this.plugin.getGame().getRegistry().getAllOf(SnowType.class)) {
			snows.put(snow.getName(), Arrays.asList(snow.getId()));
		}
		
		addDefault(Arrays.asList("SNOW"), snows);
	}
	
	public void loadIce() {
		Map<String, List<String>> ices = new HashMap<String, List<String>>();
		
		for (IceType ice : this.plugin.getGame().getRegistry().getAllOf(IceType.class)) {
			ices.put(ice.getName(), Arrays.asList(ice.getId()));
		}
		
		addDefault(Arrays.asList("ICE"), ices);
	}
	
	public void loadPotion() {
		Map<String, Object> potions = new HashMap<String, Object>();
		potions.put("GROUP_BONUS", Arrays.asList(
				PotionEffectTypes.ABSORPTION,
				PotionEffectTypes.FIRE_RESISTANCE,
				PotionEffectTypes.GLOWING,
				PotionEffectTypes.HASTE,
				PotionEffectTypes.HEALTH_BOOST,
				PotionEffectTypes.INSTANT_HEALTH,
				PotionEffectTypes.INVISIBILITY,
				PotionEffectTypes.JUMP_BOOST,
				PotionEffectTypes.LUCK,
				PotionEffectTypes.NIGHT_VISION,
				PotionEffectTypes.REGENERATION,
				PotionEffectTypes.RESISTANCE,
				PotionEffectTypes.SATURATION,
				PotionEffectTypes.SPEED,
				PotionEffectTypes.STRENGTH,
				PotionEffectTypes.WATER_BREATHING)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		potions.put("GROUP_MALUS", Arrays.asList(
				PotionEffectTypes.BLINDNESS,
				PotionEffectTypes.HUNGER,
				PotionEffectTypes.INSTANT_DAMAGE,
				PotionEffectTypes.LEVITATION,
				PotionEffectTypes.MINING_FATIGUE,
				PotionEffectTypes.NAUSEA,
				PotionEffectTypes.POISON,
				PotionEffectTypes.SLOWNESS,
				PotionEffectTypes.UNLUCK,
				PotionEffectTypes.WEAKNESS,
				PotionEffectTypes.WITHER)
					.stream().map(block -> block.getId()).collect(Collectors.toList()));
		
		addDefault(Arrays.asList("POTION_SPLASH"), potions);
	}
	
	public void loadCommand() {
		Map<String, Object> commands = new HashMap<String, Object>();
		commands.put("ADD_ALL", Arrays.asList("*"));
		commands.put("REMOVE_ALL", Arrays.asList("-"));
		commands.put("ADD_ESSENTIALS", Arrays.asList(
				"home",
				"spawn",
				"teleport"));
		commands.put("REMOVE_ESSENTIALS", Arrays.asList(
				"-home",
				"-spawn",
				"-teleport"));
		addDefault(Arrays.asList("COMMAND"), commands);
	}
	
	/*
	 * Accesseurs
	 */
	
	public <T extends CatalogType> Map<String, Set<T>> get(final String name, final Class<T> type) {
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
	
	public Map<String, Set<EntityTemplate>> getEntities(final String name) {
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

	public Map<String, Set<String>> getString(final String name) {
		ImmutableMap.Builder<String, Set<String>> groups = ImmutableMap.builder();
		for (Entry<Object, ? extends ConfigurationNode> group : this.getContains(name).getChildrenMap().entrySet()) {
			Set<String> set = new HashSet<String>();
			if (group.getValue().getString("").equalsIgnoreCase("*")) {
				groups.put(group.getKey().toString().toUpperCase(), ImmutableSet.of("*"));
			} else {
				for (ConfigurationNode config : group.getValue().getChildrenList()) {
					set.add(config.getString(""));
				}
				groups.put(group.getKey().toString().toUpperCase(), set);
			}
		}
		return groups.build();
	}
}
