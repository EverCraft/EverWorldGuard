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
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagPvp extends StateFlag {
	
	private final EverWorldGuard plugin;

	public FlagPvp(EverWorldGuard plugin) {
		super("PVP");
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_PVP_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_PVP_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ()));
	}
	
	/*
	 * CollideEntityEvent : Pour les arcs Flame
	 */
	
	public void onCollideEntityImpact(WorldGuardWorld world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (!event.getCause().get(NamedCause.SOURCE, Projectile.class).isPresent()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		// Le joueur n'est pas dans une region où il a le droit de PVP
		if (world.getRegions(player.getLocation().getPosition()).getFlag(player, player.getLocation(), this).equals(State.DENY)) {
			event.filterEntities(entity -> !(entity instanceof Player) || entity.equals(player));
		
		// Le joueur est dans une region où il a le droit de PVP donc on vérifie la position de la cible
		} else {
			event.filterEntities(entity -> {
				if (entity instanceof Player && !entity.equals(player)) {
					if (world.getRegions(entity.getLocation().getPosition()).getFlag(player, entity.getLocation(), this).equals(State.DENY)) {
						return false;
					}
				}
				return true;
			});
		}
	}
	
	public void onDamageEntity(WorldGuardWorld world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getTargetEntity() instanceof Player)) return;
		
		Player target = (Player) event.getTargetEntity();
		
		Object source = event.getCause().root();
		 if (source instanceof FallingBlockDamageSource) {				
			FallingBlockDamageSource damageSource = (FallingBlockDamageSource) source;
			
			Optional<UUID> creator = damageSource.getSource().getCreator();
			if (creator.isPresent() && !creator.get().equals(target.getUniqueId())) {
				this.plugin.getEServer().getPlayer(creator.get())
					.ifPresent(player -> this.onDamageEntity(world, event, player, target, false));
			}
		} else if (source instanceof IndirectEntityDamageSource) {				
			IndirectEntityDamageSource damageSource = (IndirectEntityDamageSource) source;
			
			if (damageSource.getIndirectSource() instanceof Player && !damageSource.getSource().equals(target)) {
				this.onDamageEntity(world, event, (Player) damageSource.getIndirectSource(), target, false);
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (damageSource.getSource() instanceof Player && !damageSource.getSource().equals(target)) {
				this.onDamageEntity(world, event, (Player) damageSource.getSource(), target, true);
			}
		} else if (source instanceof BlockDamageSource) {
			// TODO Bug BUCKET : no creator
			// TODO Bug IgniteEntityEvent : no implemented
		} else if (source instanceof DamageSource){
			DamageSource damageSource = (DamageSource) source;
			
			if (damageSource.getType().equals(DamageTypes.SUFFOCATE)) {
				Location<World> location = target.getLocation().add(Vector3d.from(0, 2, 0));
				Optional<UUID> creator = location.getExtent().getCreator(location.getBlockPosition());
				if (creator.isPresent() && !creator.get().equals(target.getUniqueId())) {
					this.plugin.getEServer().getPlayer(creator.get())
						.ifPresent(player -> this.onDamageEntity(world, event, player, target, false));
				}
			}
		}
	}
	
	public boolean onDamageEntity(WorldGuardWorld world, DamageEntityEvent event, Player player, Player target, boolean message) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return false;
		
		if (world.getRegions(player.getLocation().getPosition()).getFlag(player, player.getLocation(), this).equals(State.DENY)) {
			event.setCancelled(true);
			if (message) {
				this.sendMessage(player, player.getLocation().getPosition().toInt());
			}
			return true;
		} else if (world.getRegions(target.getLocation().getPosition()).getFlag(player, target.getLocation(), this).equals(State.DENY)) {
			event.setCancelled(true);
			if (message) {
				this.sendMessage(player, target.getLocation().getPosition().toInt());
			}
			return true;
		}
		return false;
	}
}
