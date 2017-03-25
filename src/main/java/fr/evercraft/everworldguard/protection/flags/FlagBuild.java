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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagBuild extends StateFlag {
	
	private final EverWorldGuard plugin;
	
	private final Set<EntityType> entities;

	public FlagBuild(EverWorldGuard plugin) {
		super("BUILD");
		
		this.plugin = plugin;
		this.entities = Sets.newConcurrentHashSet();
		
		this.reload();
	}
	
	public void reload() {
		this.entities.clear();
		
		this.entities.addAll(this.plugin.getProtectionService().getConfigFlags().getBuild());
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
	
	/*
	 * InteractEntity
	 */

	public void onInteractEntity(WorldWorldGuard world, InteractEntityEvent event) {
		if (event.isCancelled()) return;
		if (!this.entities.contains(event.getTargetEntity().getType())) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onInteractEntityPlayer(world, event, optPlayer.get());
		} else {
			this.onInteractEntityNatural(world, event);
		}
	}
	
	public void onInteractEntityPlayer(WorldWorldGuard world, InteractEntityEvent event, Player player) {
		if (world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, Flags.BUILD).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
	
	public void onInteractEntityNatural(WorldWorldGuard world, InteractEntityEvent event) {
		if (world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlagDefault(Flags.BUILD).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
	
	/*
	 * CollideEntity
	 */
	
	public void onCollideEntity(WorldWorldGuard world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onCollideEntityPlayer(world, event, optPlayer.get());
		} else {
			this.onCollideEntityNatural(world, event);
		}
	}
	
	public void onCollideEntityPlayer(WorldWorldGuard world, CollideEntityEvent event, Player player) {		
		if (event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) {
			event.filterEntities(entity -> {
				if (this.entities.contains(entity) && world.getRegions(entity.getLocation().getPosition()).getFlag(player, Flags.BUILD).equals(State.DENY)) {
					return false;
				}
				return true;
			});
		}
	}
	
	public void onCollideEntityNatural(WorldWorldGuard world, CollideEntityEvent event) {
		if (event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) {
			event.filterEntities(entity -> {
				if (this.entities.contains(entity) && world.getRegions(entity.getLocation().getPosition()).getFlagDefault(Flags.BUILD).equals(State.DENY)) {
					return false;
				}
				return true;
			});
		}
	}
	
	/*
	 * DamageEntity
	 */
	
	// TODO Bug : Painting, ItemFrame ...
	public void onDamageEntity(WorldWorldGuard world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		if (!this.entities.contains(event.getTargetEntity().getType())) return;
		
		Entity entity = event.getTargetEntity();
		
		Object source = event.getCause().root();
		if (source instanceof FallingBlockDamageSource) {				
			FallingBlockDamageSource damageSource = (FallingBlockDamageSource) source;
			
			Optional<UUID> creator = damageSource.getSource().getCreator();
			if (creator.isPresent()) {
				Optional<Player> player = this.plugin.getEServer().getPlayer(creator.get());
				if (player.isPresent()) {
					this.onDamageEntity(world, event, entity, player.get());
				} else {
					this.onDamageEntity(world, event, entity);
				}
			} else {
				this.onDamageEntity(world, event, entity);
			}
		} else if (source instanceof IndirectEntityDamageSource) {				
			IndirectEntityDamageSource damageSource = (IndirectEntityDamageSource) source;
			
			if (damageSource.getIndirectSource() instanceof Player) {
				this.onDamageEntity(world, event, entity, (Player) damageSource.getIndirectSource());
			} else {
				this.onDamageEntity(world, event, entity);
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (damageSource.getSource() instanceof Player) {
				this.onDamageEntity(world, event, entity, (Player) damageSource.getSource());
			} else {
				this.onDamageEntity(world, event, entity);
			}
		} else if (source instanceof BlockDamageSource) {
			BlockDamageSource damageSource = (BlockDamageSource) source;
			
			// TODO Bug BUCKET : no creator
			Optional<UUID> creator = damageSource.getBlockSnapshot().getCreator();
			if (creator.isPresent()) {
				Optional<Player> player = this.plugin.getEServer().getPlayer(creator.get());
				
				if (player.isPresent()) {
					if (this.onDamageEntity(world, event, entity, player.get())) {
						// TODO Bug IgniteEntityEvent : no implemented
						if (damageSource.getType().equals(DamageTypes.FIRE)) {
							entity.offer(Keys.FIRE_TICKS, 0);
						}
					}
				} else {
					if (this.onDamageEntity(world, event, entity)) {
						// TODO Bug IgniteEntityEvent : no implemented
						if (damageSource.getType().equals(DamageTypes.FIRE)) {
							entity.offer(Keys.FIRE_TICKS, 0);
						}
					}
				}
			} else {
				if (this.onDamageEntity(world, event, entity)) {
					// TODO Bug IgniteEntityEvent : no implemented
					if (damageSource.getType().equals(DamageTypes.FIRE)) {
						entity.offer(Keys.FIRE_TICKS, 0);
					}
				}
			}
		} else if (source instanceof DamageSource) {
			DamageSource damageSource = (DamageSource) source;
			
			if (damageSource.getType().equals(DamageTypes.SUFFOCATE)) {
				Location<World> location = entity.getLocation().add(Vector3d.from(0, 2, 0));
				Optional<UUID> creator = location.getExtent().getCreator(location.getBlockPosition());
				if (creator.isPresent()) {
					Optional<Player> player = this.plugin.getEServer().getPlayer(creator.get());
					if (player.isPresent()) {
						this.onDamageEntity(world, event, entity, player.get());
					} else {
						this.onDamageEntity(world, event, entity);
					}
				} else {
					this.onDamageEntity(world, event, entity);
				}
			}
		}
	}
	
	public boolean onDamageEntity(WorldWorldGuard world, DamageEntityEvent event, Entity entity, Player player) {
		if (!world.getRegions(entity.getLocation().getPosition()).getFlag(player, Flags.INTERACT_ENTITY).contains(event.getTargetEntity(), player)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	
	public boolean onDamageEntity(WorldWorldGuard world, DamageEntityEvent event, Entity entity) {
		if (!world.getRegions(entity.getLocation().getPosition()).getFlagDefault(Flags.INTERACT_ENTITY).contains(event.getTargetEntity())) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
