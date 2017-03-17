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

import java.util.Optional;
import java.util.stream.Stream;

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

import com.flowpowered.math.vector.Vector3d;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagBuild extends StateFlag {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;

	public FlagBuild(EverWorldGuard plugin) {
		super("BUILD");
		
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_BUILD.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	/*
	 * ChangeBlockEvent.Pre
	 */
	
	public void onChangeBlockPre(WorldWorldGuard world, ChangeBlockEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> piston = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (piston.isPresent() && event.getCause().containsNamed(NamedCause.PISTON_EXTEND)) {
			this.onChangeBlockPrePiston(world, event, piston.get());
		} else {
			this.onChangeBlockPreOthers(world, event);
		}
	}
	
	// Il faut vérifer le block d'arrivé
	private void onChangeBlockPrePiston(WorldWorldGuard world, ChangeBlockEvent.Pre event, LocatableBlock block) {
		Vector3d direction = block.get(Keys.DIRECTION).orElse(Direction.NONE).asOffset();
		Stream<Vector3d> positions = Stream.concat(
				event.getLocations().stream().map(location -> location.getPosition()),
				event.getLocations().stream().map(location -> location.getPosition().add(direction)))
				.distinct();
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			
			if (positions.anyMatch(position -> world.getRegions(position).getFlag(player, Flags.BUILD).equals(State.DENY))) {
				event.setCancelled(true);
			}
		} else {
			if (positions.anyMatch(position -> world.getRegions(position).getFlagDefault(Flags.BUILD).equals(State.DENY))) {
				event.setCancelled(true);
			}
		}
	}
	
	// Vérification des pistons...
	private void onChangeBlockPreOthers(WorldWorldGuard world, ChangeBlockEvent.Pre event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			if (event.getLocations().stream().anyMatch(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.DENY))) {
				event.setCancelled(true);
			}
		} else {
			if (event.getLocations().stream().anyMatch(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.DENY))) {
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(WorldWorldGuard world, ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;		
		
		Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
		if (falling.isPresent()) {
			this.onChangeBlockPlaceFalling(world, event, falling.get());
		} else {
			this.onChangeBlockPlaceNoFalling(world, event);
		}
	}
	
	// Drop l'item au sol pour le bloc avec gravité
	private void onChangeBlockPlaceFalling(WorldWorldGuard world, ChangeBlockEvent.Place event, FallingBlock falling) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW))
				.forEach(transaction -> transaction.getFinal().getLocation().ifPresent(location -> {
					Entity entity = location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
					entity.offer(Keys.REPRESENTED_ITEM, ItemStack.builder().fromBlockSnapshot(transaction.getFinal()).build().createSnapshot());
					entity.setCreator(player.getUniqueId());
					
					location.getExtent().spawnEntity(entity, Cause.source(
						EntitySpawnCause.builder().entity(entity).type(SpawnTypes.PLUGIN).build())
						.from(event.getCause())
						.named(UtilsCause.PLACE_EVENT, event).build());
				}));
		} else {			
			event.filter(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.ALLOW))
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
	private void onChangeBlockPlaceNoFalling(WorldWorldGuard world, ChangeBlockEvent.Place event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW));
		} else {
			event.filter(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.ALLOW));
		}
	}
	
	/*
	 * ChangeBlockEvent.Break
	 */
	
	public void onChangeBlockBreak(WorldWorldGuard world, ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockBreakPlayer(world, event, optPlayer.get());
		} else {
			this.onChangeBlockBreakNatural(world, event);
		}
	}
	
	private void onChangeBlockBreakPlayer(WorldWorldGuard world, ChangeBlockEvent.Break event, Player player) {		
		if (!event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW)).isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			}
		}
	}
	
	private void onChangeBlockBreakNatural(WorldWorldGuard world, ChangeBlockEvent.Break event) {		
		if (!event.filter(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.ALLOW)).isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			}
		}
	}
}
