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
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagPvp extends StateFlag {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;

	public FlagPvp(EverWorldGuard plugin) {
		super("PVP");
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_PVP.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public void onCollideEntity(WorldWorldGuard world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent() && event.getCause().get(NamedCause.OWNER, Player.class).isPresent()) {
			event.filterEntities(entity -> {
				if (entity instanceof Player) {
					if (world.getRegions(entity.getLocation().getPosition()).getFlag((Player) entity, this).equals(State.DENY)) {
						return false;
					}
				}
				return true;
			});
		}
	}
	
	public void onDamageEntity(WorldWorldGuard world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getTargetEntity() instanceof Player)) return;
		
		Player player = (Player) event.getTargetEntity();
		
		Object source = event.getCause().root();
		 if (source instanceof FallingBlockDamageSource) {				
			FallingBlockDamageSource damageSource = (FallingBlockDamageSource) source;
			
			Optional<UUID> creator = damageSource.getSource().getCreator();
			if (creator.isPresent() && !creator.get().equals(player.getUniqueId())) {
				this.onDamageEntity(world, event, player);
			}
		} else if (source instanceof IndirectEntityDamageSource) {				
			IndirectEntityDamageSource damageSource = (IndirectEntityDamageSource) source;
			
			if (damageSource.getIndirectSource() instanceof Player && !damageSource.getSource().equals(player)) {
				this.onDamageEntity(world, event, player);
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (damageSource.getSource() instanceof Player && !damageSource.getSource().equals(player)) {
				this.onDamageEntity(world, event, player);
			}
		} else if (source instanceof BlockDamageSource) {
			BlockDamageSource damageSource = (BlockDamageSource) source;
			
			// TODO Bug BUCKET : no creator
			Optional<UUID> creator = damageSource.getBlockSnapshot().getCreator();
			if (creator.isPresent() && !creator.get().equals(player.getUniqueId())) {
				if (this.onDamageEntity(world, event, player)) {
					// TODO Bug IgniteEntityEvent : no implemented
					if (damageSource.getType().equals(DamageTypes.FIRE)) {
						player.offer(Keys.FIRE_TICKS, 0);
					}
				}
			}
		} else if (source instanceof DamageSource){
			DamageSource damageSource = (DamageSource) source;
			
			if (damageSource.getType().equals(DamageTypes.SUFFOCATE)) {
				Location<World> location = player.getLocation().add(Vector3d.from(0, 2, 0));
				Optional<UUID> creator = location.getExtent().getCreator(location.getBlockPosition());
				if (creator.isPresent() && !creator.get().equals(player.getUniqueId())) {
					this.onDamageEntity(world, event, player);
				}
			}
		}
	}
	
	public boolean onDamageEntity(WorldWorldGuard world, DamageEntityEvent event, Player player) {
		if (world.getRegions(player.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
