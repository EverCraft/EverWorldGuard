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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.EntityTemplateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagDamageEntity extends EntityTemplateFlag {
	
	private final EverWorldGuard plugin;
	
	public FlagDamageEntity(EverWorldGuard plugin) {
		super("DAMAGE_ENTITY");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<EntityTemplate>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().getEntities(this.getName());
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_DAMAGE_ENTITY_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Entity entity) {
		Vector3i position = entity.getLocation().getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_DAMAGE_ENTITY_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<entity>", entity.getType().getTranslation()));
	}
	
	/*
	 * CollideEntityEvent : Pour les arcs Flame
	 */
	public void onCollideEntityImpact(WorldGuardWorld world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		// TODO Owner == Cible ?
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		if (!event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) return;
		
		event.filterEntities(entity -> {
			if (this.getDefault().contains(entity) && !world.getRegions(entity.getLocation().getPosition()).getFlag(player, entity.getLocation(), this).contains(entity, player)) {
				return false;
			}
			return true;
		});
	}
	
	/*
	 * DamageEntity
	 */
	
	public void onDamageEntity(WorldGuardWorld world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		if (!this.getDefault().contains(event.getTargetEntity())) return;
		
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
				if (this.onDamageEntity(world, event, entity, (Player) damageSource.getIndirectSource())) {
					// Message
					this.sendMessage((Player) damageSource.getIndirectSource(), entity);
				}
			} else {
				this.onDamageEntity(world, event, entity);
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (damageSource.getSource() instanceof Player) {
				if (this.onDamageEntity(world, event, entity, (Player) damageSource.getSource())) {
					// Message
					this.sendMessage((Player) damageSource.getSource(), entity);
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
	
	public boolean onDamageEntity(WorldGuardWorld world, DamageEntityEvent event, Entity entity, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return false;
		
		if (!world.getRegions(entity.getLocation().getPosition()).getFlag(player, entity.getLocation(), this).contains(event.getTargetEntity(), player)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	
	public boolean onDamageEntity(WorldGuardWorld world, DamageEntityEvent event, Entity entity) {
		if (!world.getRegions(entity.getLocation().getPosition()).getFlagDefault(this).contains(event.getTargetEntity())) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	
	/*
	 * InteractEntityEvent.Primary
	 */

	// TODO Bug : ARMOR_STAND
	public void onInteractEntityPrimary(WorldGuardWorld world, InteractEntityEvent.Primary event) {
		if (event.isCancelled()) return;
		
		if (!event.getTargetEntity().equals(EntityTypes.ARMOR_STAND)) return;
		if (!this.getDefault().contains(event.getTargetEntity())) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		if (!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, event.getTargetEntity().getLocation(), this).contains(event.getTargetEntity(), player)) {
			event.setCancelled(true);
			
			// Message
			this.sendMessage((Player) player, event.getTargetEntity());
		}
	}
}
