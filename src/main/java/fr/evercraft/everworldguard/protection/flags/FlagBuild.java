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
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
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
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
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
		
		Map<String, Set<EntityTemplate>> config = this.plugin.getProtectionService().getConfigFlags().getEntities("BUILD");
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
	
	/*
	 * InteractBlockEvent.Secondary : TODO : Fix le bug de la TNT
	 */
	public void onInteractBlockSecondary(WorldGuardWorld world, InteractBlockEvent.Secondary event, Location<World> location) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		BlockType type = event.getTargetBlock().getState().getType();
		if (!type.equals(BlockTypes.TNT)) return;
		
		Optional<ItemStack> itemstack = player.getItemInHand(event.getHandType());
		if (!itemstack.isPresent()) return;
		
		ItemType itemtype = itemstack.get().getItem();
		if (!itemtype.equals(ItemTypes.FLINT_AND_STEEL) && !itemtype.equals(ItemTypes.FIRE_CHARGE)) return;
		
		if (world.getRegions(location.getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			
			// Message
			this.sendMessage(player, location.getPosition().toInt());
		}
	}
	
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
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
			
		if (locations.anyMatch(location -> service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.DENY))) {
			event.setCancelled(true);
		}
	}
	
	// Vérification des pistons...
	private void onChangeBlockPreOthers(EProtectionService service, ChangeBlockEvent.Pre event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		if (event.getLocations().stream().anyMatch(location -> 
				service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.DENY))) {
			event.setCancelled(true);
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
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
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
	}
	
	// Placement de bloc
	private void onChangeBlockPlaceNoFalling(EProtectionService service, ChangeBlockEvent.Place event) {
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		List<Transaction<BlockSnapshot>> filter = event.filter(location -> 
			service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.ALLOW));
		
		if (!filter.isEmpty()) {
			// Vérifie que c'est une action directe
			Optional<Player> optSource = event.getCause().get(NamedCause.SOURCE, Player.class);
			if(!optSource.isPresent() || !optSource.get().equals(player)) return;
			
			// Message
			this.sendMessage(player, filter.get(0).getOriginal().getPosition());
		}
	}
	
	/*
	 * ChangeBlockEvent.Break
	 */
	
	public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		List<Transaction<BlockSnapshot>> filter = event.filter(location -> 
			service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlag(player, this).equals(State.ALLOW));
	
		if (!filter.isEmpty()) {
			Optional<FallingBlock> falling = event.getCause().get(NamedCause.SOURCE, FallingBlock.class);
			if (falling.isPresent()) {
				falling.get().remove();
			} else {
				
				// Vérifie que c'est une action directe
				Optional<Player> optSource = event.getCause().get(NamedCause.SOURCE, Player.class);
				if(!optSource.isPresent() || !optSource.get().equals(player)) return;
				
				// Message
				this.sendMessage(player, filter.get(0).getOriginal().getPosition());
			}
		}
	}
	
	/*
	 * InteractEntity
	 */

	public void onInteractEntity(WorldGuardWorld world, InteractEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		if (!this.containsEntity(event.getTargetEntity(), player)) return;
		
		if (world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			
			// Vérifie que c'est une action directe
			Optional<Player> optSource = event.getCause().get(NamedCause.SOURCE, Player.class);
			if(!optSource.isPresent() || !optSource.get().equals(player)) return;
			
			// Message
			this.sendMessage(player, event.getTargetEntity().getLocation().getPosition().toInt());
		}
	}
	
	/*
	 * CollideEntity
	 */
	
	public void onCollideEntityImpact(WorldGuardWorld world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
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
	
	/*
	 * DamageEntity
	 */
	
	// TODO Bug : Painting, ItemFrame ...
	public void onDamageEntity(WorldGuardWorld world, DamageEntityEvent event) {
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
				}
			}
		} else if (source instanceof IndirectEntityDamageSource) {				
			IndirectEntityDamageSource damageSource = (IndirectEntityDamageSource) source;
			
			if (damageSource.getIndirectSource() instanceof Player) {
				this.onDamageEntity(world, event, entity, (Player) damageSource.getIndirectSource());
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (damageSource.getSource() instanceof Player) {
				if (this.onDamageEntity(world, event, entity, (Player) damageSource.getSource())) {
					// Message
					this.sendMessage((Player) damageSource.getSource(), event.getTargetEntity().getLocation().getPosition().toInt());
				}
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
					}
				}
			}
		}
	}
	
	public boolean onDamageEntity(WorldGuardWorld world, DamageEntityEvent event, Entity entity, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return false;
		
		if (!this.containsEntity(event.getTargetEntity(), player)) return false;
		
		if (world.getRegions(entity.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
