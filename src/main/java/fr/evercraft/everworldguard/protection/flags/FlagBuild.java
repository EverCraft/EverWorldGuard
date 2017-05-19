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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
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
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class FlagBuild extends StateFlag {
	
	private final EverWorldGuard plugin;
	
	private final Set<EntityTemplate> entities;

	public FlagBuild(EverWorldGuard plugin) {
		super("BUILD");
		
		this.plugin = plugin;
		this.entities = Sets.newConcurrentHashSet();
		
		this.reload();
	}
	
	public void reload() {
		this.entities.clear();
		
		Map<String, Set<EntityTemplate>> config = this.plugin.getProtectionService().getConfigFlags().get("BUILD", EntityTemplate.class);
		if (config.containsKey("entities")) {
			this.entities.addAll(config.get("entities"));
		}
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_BUILD_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_BUILD_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ()));
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public boolean containsEntity(Entity value) {
		return this.entities.stream()
			.filter(element -> element.contains(value))
			.findAny().isPresent();
	}
	
	public boolean containsEntity(Entity value, Player player) {
		return this.entities.stream()
			.filter(element -> element.contains(value, player))
			.findAny().isPresent();
	}
	
	// TODO TNT
	
	/*
	 * ChangeBlockEvent.Pre
	 */
	
	public void onChangeBlockPre(ChangeBlockEvent.Pre event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> piston = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (piston.isPresent() && event.getCause().containsNamed(NamedCause.PISTON_EXTEND)) {
			this.onChangeBlockPrePiston(this.plugin.getProtectionService(), event, piston.get());
		} else {
			this.onChangeBlockPreOthers(this.plugin.getProtectionService(), event);
		}
	}
	
	// Il faut vérifer le block d'arrivé
	private void onChangeBlockPrePiston(EProtectionService service, ChangeBlockEvent.Pre event, LocatableBlock block) {
		Vector3d direction = block.get(Keys.DIRECTION).orElse(Direction.NONE).asOffset();
		Stream<Location<World>> locations = Stream.concat(
				event.getLocations().stream().map(location -> location),
				event.getLocations().stream().map(location -> location.add(direction)))
				.distinct();
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			
			if (locations.anyMatch(location -> service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.DENY))) {
				event.setCancelled(true);
			}
		} else {
			if (locations.anyMatch(location -> service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.DENY))) {
				event.setCancelled(true);
			}
		}
	}
	
	// Vérification des pistons...
	private void onChangeBlockPreOthers(EProtectionService service, ChangeBlockEvent.Pre event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			if (event.getLocations().stream().anyMatch(location -> 
					service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.DENY))) {
				event.setCancelled(true);
			}
		} else {
			if (event.getLocations().stream().anyMatch(location -> 
					service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.DENY))) {
				event.setCancelled(true);
			}
		}
	}
	
	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;		
		
		Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
		if (falling.isPresent()) {
			this.onChangeBlockPlaceFalling(this.plugin.getProtectionService(), event, falling.get());
		} else {
			this.onChangeBlockPlaceNoFalling(this.plugin.getProtectionService(), event);
		}
	}
	
	// Drop l'item au sol pour le bloc avec gravité
	private void onChangeBlockPlaceFalling(EProtectionService service, ChangeBlockEvent.Place event, FallingBlock falling) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			event.filter(location -> service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.ALLOW))
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
			event.filter(location -> service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.ALLOW))
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
	private void onChangeBlockPlaceNoFalling(EProtectionService service, ChangeBlockEvent.Place event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			Player player = optPlayer.get();
			List<Transaction<BlockSnapshot>> filter = event.filter(location -> 
				service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.ALLOW));
			
			if (!filter.isEmpty()) {
				// Message
				this.sendMessage(player, filter.get(0).getOriginal().getPosition());
			}
		} else {
			event.filter(location -> 
				service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.ALLOW));
		}
	}
	
	/*
	 * ChangeBlockEvent.Break
	 */
	
	public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onChangeBlockBreakPlayer(this.plugin.getProtectionService(), event, optPlayer.get());
		} else {
			this.onChangeBlockBreakNatural(this.plugin.getProtectionService(), event);
		}
	}
	
	private void onChangeBlockBreakPlayer(EProtectionService service, ChangeBlockEvent.Break event, Player player) {
		List<Transaction<BlockSnapshot>> filter = event.filter(location -> 
			service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.ALLOW));
		
		if (!filter.isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			} else {
				
				// Message
				this.sendMessage(player, filter.get(0).getOriginal().getPosition());
			}
		}
	}
	
	private void onChangeBlockBreakNatural(EProtectionService service, ChangeBlockEvent.Break event) {		
		if (!event.filter(location -> 
				service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.ALLOW)).isEmpty()) {
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
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onInteractEntityPlayer(world, event, optPlayer.get());
		} else {
			this.onInteractEntityNatural(world, event);
		}
	}
	
	public void onInteractEntityPlayer(WorldWorldGuard world, InteractEntityEvent event, Player player) {
		if (!this.containsEntity(event.getTargetEntity(), player)) return;
		
		if (world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			
			// Message
			this.sendMessage(player, event.getTargetEntity().getLocation().getPosition().toInt());
		}
	}
	
	public void onInteractEntityNatural(WorldWorldGuard world, InteractEntityEvent event) {
		if (!this.containsEntity(event.getTargetEntity())) return;
		
		if (world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlagDefault(this).equals(State.DENY)) {
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
			List<? extends Entity> filter = event.filterEntities(entity -> {
				if (this.containsEntity(entity, player) && world.getRegions(entity.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
					return false;
				}
				return true;
			});
			
			if (!filter.isEmpty()) {
				// Message
				this.sendMessage(player, filter.get(0).getLocation().getPosition().toInt());
			}
		}
	}
	
	public void onCollideEntityNatural(WorldWorldGuard world, CollideEntityEvent event) {
		if (event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) {
			event.filterEntities(entity -> {
				if (this.containsEntity(entity) && world.getRegions(entity.getLocation().getPosition()).getFlagDefault(this).equals(State.DENY)) {
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
				if (this.onDamageEntity(world, event, entity, (Player) damageSource.getSource())) {
					// Message
					this.sendMessage((Player) damageSource.getSource(), event.getTargetEntity().getLocation().getPosition().toInt());
				}
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
		if (!this.containsEntity(event.getTargetEntity(), player)) return false;
		
		if (world.getRegions(entity.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	
	public boolean onDamageEntity(WorldWorldGuard world, DamageEntityEvent event, Entity entity) {
		if (!this.containsEntity(event.getTargetEntity())) return false;
		
		if (world.getRegions(entity.getLocation().getPosition()).getFlagDefault(this).equals(State.DENY)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
