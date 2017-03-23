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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.flag.type.EntryFlag;
import fr.evercraft.everapi.services.worldguard.flag.value.EntryFlagValue;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteractEntity extends EntryFlag<String, EntityTemplate> {
	
	private static final String ALL = "ALL";
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;
	private final Map<String, Set<EntityTemplate>> groups;
	private EntryFlagValue<String, EntityTemplate> defaults;
	
	public FlagInteractEntity(EverWorldGuard plugin) {
		super("INTERACT_ENTITY");
		
		this.plugin = plugin;
		
		this.groups = new ConcurrentHashMap<String, Set<EntityTemplate>>();
		this.defaults = new EntryFlagValue<String, EntityTemplate>();
		
		this.reload();
	}
	
	public void reload() {
		this.groups.clear();
		//this.groups.putAll(this.plugin.getProtectionService().getConfigFlags().getInteractBlock());
		
		Set<String> keys = this.groups.keySet();
		Set<EntityTemplate> values = new HashSet<EntityTemplate>();
		this.groups.values().forEach(value -> values.addAll(value));
		this.defaults = new EntryFlagValue<String, EntityTemplate>(keys, values);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT_BLOCK.getString();
	}

	@Override
	public EntryFlagValue<String, EntityTemplate> getDefault() {
		return this.defaults;
	}
	
	/*
	 * Suggest
	 */

	@Override
	public Collection<String> getSuggestAdd(List<String> args) {
		return Stream.concat(
				this.groups.keySet().stream(),
				Stream.of(ALL))
			.filter(suggest -> !args.stream().anyMatch(arg -> arg.equalsIgnoreCase(suggest)))
			.collect(Collectors.toList());
	}
	
	@Override
	public String serialize(EntryFlagValue<String, EntityTemplate> value) {
		return String.join(",", value.getKeys());
	}

	@Override
	public EntryFlagValue<String, EntityTemplate> deserialize(String value) throws IllegalArgumentException {
		if (value.equalsIgnoreCase(ALL)) return this.defaults;
		if (value.isEmpty()) return new EntryFlagValue<String, EntityTemplate>();
		
		Set<String> keys = new HashSet<String>();
		Set<EntityTemplate> values = new HashSet<EntityTemplate>();
		for (String key : value.split(PATTERN_SPLIT)) {
			Set<EntityTemplate> blocks = this.groups.get(key.toUpperCase());
			if (blocks != null) {
				keys.add(key.toUpperCase());
				values.addAll(blocks);
			} else {
				throw new IllegalArgumentException();
			}
		}
		return new EntryFlagValue<String, EntityTemplate>(keys, values);
	}
	
	/*
	 * InteractBlockEvent.Secondary
	 */

	/*
	public void onInteractBlockSecondary(WorldWorldGuard world, InteractBlockEvent.Secondary event, Location<World> location) {
		if (event.isCancelled()) return;
		
		BlockType type = event.getTargetBlock().getState().getType();
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockPlayer(world, event, location, type, optPlayer.get());
		} else {
			this.onChangeBlockNatural(world, event, location, type);
		}
	}
	
	private void onChangeBlockPlayer(WorldWorldGuard world, InteractBlockEvent.Secondary event, Location<World> location, BlockType type, Player player) {		
		if (this.getDefault().containsValue(type) && !world.getRegions(location.getPosition()).getFlag(player, Flags.INTERACT_BLOCK).containsValue(type)) {
			event.setUseBlockResult(Tristate.FALSE);
		}
	}
	
	private void onChangeBlockNatural(WorldWorldGuard world, InteractBlockEvent.Secondary event, Location<World> location, BlockType type) {
		if (this.getDefault().containsValue(type) && !world.getRegions(location.getPosition()).getFlagDefault(Flags.INTERACT_BLOCK).containsValue(type)) {
			event.setUseBlockResult(Tristate.FALSE);
		}
	}*/
}
