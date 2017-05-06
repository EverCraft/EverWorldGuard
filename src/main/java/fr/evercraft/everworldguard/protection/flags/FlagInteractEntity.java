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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.EntityTemplateFlag;
import fr.evercraft.everapi.services.worldguard.flag.value.EntityPatternFlagValue;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteractEntity extends EntityTemplateFlag {
	
	private static final String ALL = "ALL";
	
	private final EverWorldGuard plugin;
	private final Map<String, Set<EntityTemplate>> groups;
	private EntityPatternFlagValue<EntityTemplate, Entity> defaults;
	
	public FlagInteractEntity(EverWorldGuard plugin) {
		super("INTERACT_ENTITY");
		
		this.plugin = plugin;
		
		this.groups = new ConcurrentHashMap<String, Set<EntityTemplate>>();
		this.defaults = new EntityPatternFlagValue<EntityTemplate, Entity>();
		
		this.reload();
	}
	
	public void reload() {
		this.groups.clear();
		this.groups.putAll(this.plugin.getProtectionService().getConfigFlags().getInteractEntity());
		
		Set<String> keys = this.groups.keySet();
		Set<EntityTemplate> values = new HashSet<EntityTemplate>();
		this.groups.values().forEach(value -> values.addAll(value));
		this.defaults = new EntityPatternFlagValue<EntityTemplate, Entity>(keys, values);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT_ENTITY_DESCRIPTION.getString();
	}

	@Override
	public EntityPatternFlagValue<EntityTemplate, Entity> getDefault() {
		return this.defaults;
	}
	
	/*
	 * Suggest
	 */

	@Override
	public Collection<String> getSuggestAdd(CommandSource source, List<String> args) {
		return Stream.concat(
				this.groups.keySet().stream(),
				Stream.of(ALL))
			.filter(suggest -> !args.stream().anyMatch(arg -> arg.equalsIgnoreCase(suggest)))
			.collect(Collectors.toList());
	}
	
	@Override
	public String serialize(EntityPatternFlagValue<EntityTemplate, Entity> value) {
		return String.join(",", value.getKeys());
	}

	@Override
	public EntityPatternFlagValue<EntityTemplate, Entity> deserialize(String value) throws IllegalArgumentException {
		if (value.equalsIgnoreCase(ALL)) return this.defaults;
		if (value.isEmpty()) return new EntityPatternFlagValue<EntityTemplate, Entity>();
		
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
		return new EntityPatternFlagValue<EntityTemplate, Entity>(keys, values);
	}
	
	@Override
	public Text getValueFormat(EntityPatternFlagValue<EntityTemplate, Entity> value) {
		if (value.getKeys().isEmpty()) {
			return EAMessages.FLAG_ENTITYTEMPLATE_EMPTY.getText();
		}
		
		List<Text> groups = new ArrayList<Text>();
		for (String group : value.getKeys()) {
			List<Text> entities = new ArrayList<Text>();
			for (EntityTemplate entity : this.groups.get(group)) {
				entities.add(EAMessages.FLAG_ENTITYTEMPLATE_HOVER.getFormat().toText("<entity>", entity.getId()));
			}
			groups.add(EAMessages.FLAG_ENTITYTEMPLATE_GROUP.getFormat().toText("<group>", group).toBuilder()
				.onHover(TextActions.showText(Text.joinWith(Text.of("\n"), entities)))
				.build());
		}
		
		return Text.joinWith(EAMessages.FLAG_ENTITYTEMPLATE_JOIN.getText(), groups);
	}
	
	/*
	 * InteractEntity
	 */

	public void onInteractEntity(WorldWorldGuard world, InteractEntityEvent event) {
		if (event.isCancelled()) return;
		if (!this.getDefault().contains(event.getTargetEntity())) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onInteractEntityPlayer(world, event, optPlayer.get());
		} else {
			this.onInteractEntityNatural(world, event);
		}
	}
	
	public void onInteractEntityPlayer(WorldWorldGuard world, InteractEntityEvent event, Player player) {
		if (!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, this).contains(event.getTargetEntity(), player)) {
			event.setCancelled(true);
		}
	}
	
	public void onInteractEntityNatural(WorldWorldGuard world, InteractEntityEvent event) {
		if (!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlagDefault(this).contains(event.getTargetEntity())) {
			event.setCancelled(true);
		}
	}
}
