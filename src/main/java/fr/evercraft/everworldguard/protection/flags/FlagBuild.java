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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
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
	
	@Listener(order=Order.FIRST)
	public void onChangeBlockPre(ChangeBlockEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> piston = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (piston.isPresent() && event.getCause().containsNamed(NamedCause.PISTON_EXTEND)) {
			this.onChangeBlockPrePiston(event, piston.get());
		} else {
			this.onChangeBlockPreOthers(event);
		}
	}
	
	// Il faut vérifer le block d'arrivé
	public void onChangeBlockPrePiston(ChangeBlockEvent.Pre event, LocatableBlock block) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		Vector3d direction = block.get(Keys.DIRECTION).orElse(Direction.NONE).asOffset();
		
		Stream<Vector3d> positions = Stream.concat(
				event.getLocations().stream().map(location -> location.getPosition()),
				event.getLocations().stream().map(location -> location.getPosition().add(direction)))
				.distinct();
		
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
	
	public void onChangeBlockPreOthers(ChangeBlockEvent.Pre event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			if (event.getLocations().stream().anyMatch(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.DENY))) {
				this.plugin.getEServer().getBroadcastChannel().send(Text.of("ChangeBlockEvent.Pre Player : "));
				event.setCancelled(true);
			}
		} else {
			if (event.getLocations().stream().anyMatch(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.DENY))) {
				this.plugin.getEServer().getBroadcastChannel().send(Text.of("ChangeBlockEvent.Pre Nature : "));
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * ChangeBlockEvent.Place
	 */
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;		
		
		Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
		if (falling.isPresent()) {
			this.onChangeBlockPlaceFalling(event, falling.get());
		} else {
			this.onChangeBlockPlace(event);
		}
	}
	
	// Drop l'item au sol pour le bloc avec gravité
	public void onChangeBlockPlaceFalling(ChangeBlockEvent.Place event, FallingBlock falling) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());		
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		
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
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		
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
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockBreakPlayer(event, optPlayer.get());
		} else {
			this.onChangeBlockBreakNatural(event);
		}
	}
	
	public void onChangeBlockBreakPlayer(ChangeBlockEvent.Break event, Player player) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());	
		
		if (!event.filter(location -> world.getRegions(location.getPosition()).getFlag(player, Flags.BUILD).equals(State.ALLOW)).isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			}
		}
	}
	
	public void onChangeBlockBreakNatural(ChangeBlockEvent.Break event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		if (!event.filter(location -> world.getRegions(location.getPosition()).getFlagDefault(Flags.BUILD).equals(State.ALLOW)).isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			}
		}
	}
	
	/*
	 * InteractItemEvent.Secondary
	 */
	/*
	@Listener(order=Order.FIRST)
	public void onPlayerInteract(InteractItemEvent.Secondary event, @First Entity entity) {
		// Erreur Sponge : Pas de ChangeBlockEvent pour les BUCKET : https://github.com/SpongePowered/SpongeCommon/issues/1252
		if (event.getItemStack().getType().equals(ItemTypes.BUCKET) || 
				event.getItemStack().getType().equals(ItemTypes.LAVA_BUCKET) ||
				event.getItemStack().getType().equals(ItemTypes.WATER_BUCKET)) {

			event.getInteractionPoint().ifPresent(position -> {
				WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(entity.getWorld());
				
				if (entity instanceof Player) {
					if (world.getRegions(position).getFlag((Player) entity, Flags.BUILD).equals(State.DENY)) {
						event.setCancelled(true);
					}
				} else {
					if (world.getRegions(position).getFlagDefault(Flags.BUILD).equals(State.DENY)) {
						event.setCancelled(true);
					}
				}
			});
		}
	}*/
}
