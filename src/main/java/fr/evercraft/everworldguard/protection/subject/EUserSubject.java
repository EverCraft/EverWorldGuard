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
package fr.evercraft.everworldguard.protection.subject;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.event.ESpongeEventFactory;
import fr.evercraft.everapi.message.EMessageSender;
import fr.evercraft.everapi.registers.MoveType;
import fr.evercraft.everapi.registers.MoveType.MoveTypes;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsLocation;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.ESetProtectedRegion;

public class EUserSubject implements SubjectWorldGuard {
	
	private final EverWorldGuard plugin;
	
	private final UUID identifier;
	
	private Location<World> lastLocation;
	private boolean lastRide;
	private SetProtectedRegion lastRegions;
	private final LoadingCache<Flag<?>, Long> messages;

	public EUserSubject(final EverWorldGuard plugin, final UUID identifier) {
		Preconditions.checkNotNull(plugin, "plugin");
		Preconditions.checkNotNull(identifier, "identifier");
		
		this.plugin = plugin;
		this.identifier = identifier;
		
		this.lastRegions = SetProtectedRegion.empty();
		this.messages = CacheBuilder.newBuilder()
			    .maximumSize(10)
			    .expireAfterAccess(10, TimeUnit.MINUTES)
			    .build(new CacheLoader<Flag<?>, Long>() {
			        @Override
			        public Long load(Flag<?> flag) {
			            return 0L;
			        }
			    });
	}
	
	public void rebuild() {
		if (this.lastLocation == null) return;
		
		this.lastRegions = this.plugin.getProtectionService().getOrCreateWorld(this.lastLocation.getExtent()).getRegions(this.lastLocation.getPosition());
	}
	
	/*
	 * Message
	 */
	
	public boolean sendMessage(EPlayer player, Flag<?> flag, EMessageSender message) {
		Long time = this.messages.getIfPresent(flag);
		if (time != null && time > System.currentTimeMillis()) return false;
			
		message.sendTo(player);
		
		this.messages.put(flag, System.currentTimeMillis() + this.plugin.getProtectionService().getIntervalMessage());
		return true;
	}
	
	/*
	 * Region
	 */
	
	public void initialize(Player player) {
		this.lastLocation = player.getLocation();
		this.lastLocation = this.lastLocation.setPosition(this.lastLocation.getBlockPosition().toDouble());
		this.lastRide = player.getVehicle().isPresent();
		
		this.moveToPost(player, this.lastLocation, MoveTypes.UNKNOWN_NON_CANCELLABLE, Cause.source(this.plugin).build(), true);
	}
	
	@Override
	public SetProtectedRegion getRegions() {
		return this.lastRegions;
	}
	
	public Optional<Location<World>> moveToPre(Player player_sponge, Location<World> toLocation, MoveType move, Cause cause) {
		if (!UtilsLocation.isDifferentBlock(this.lastLocation, toLocation)) return Optional.empty();
		
		EPlayer player = this.plugin.getEServer().getEPlayer(player_sponge);
		SetProtectedRegion toRegions = this.plugin.getProtectionService().getOrCreateWorld(toLocation.getExtent()).getRegions(toLocation.getPosition());
		SetProtectedRegion entered = new ESetProtectedRegion(Sets.difference(toRegions.getAll(), this.lastRegions.getAll()));
        SetProtectedRegion exited = new ESetProtectedRegion(Sets.difference(this.lastRegions.getAll(), toRegions.getAll()));
		
        Event event = null;
		if (move.isCancellable()) {
			event = ESpongeEventFactory.createMoveRegionEventPreCancellable(player, this.lastLocation, toLocation, this.lastRegions, toRegions, entered, exited, cause);
		} else {
			event = ESpongeEventFactory.createMoveRegionEventPre(player, this.lastLocation, toLocation, this.lastRegions, toRegions, entered, exited, cause);
		}
		
		if (this.plugin.getGame().getEventManager().post(event)) {
			return Optional.ofNullable(this.lastLocation);
		}
		return Optional.empty();
	}
	
	public void moveToPost(Player player_sponge, Location<World> toLocation, MoveType move, Cause cause) {
		this.moveToPost(player_sponge, toLocation, move, cause, false);
	}
	
	public void moveToPost(Player player_sponge, Location<World> toLocation, MoveType move, Cause cause, boolean force) {
		if (!force && !UtilsLocation.isDifferentBlock(this.lastLocation, toLocation)) return;

		EPlayer player = this.plugin.getEServer().getEPlayer(player_sponge);
		SetProtectedRegion toRegions = this.plugin.getProtectionService().getOrCreateWorld(toLocation.getExtent()).getRegions(toLocation.getPosition());
		SetProtectedRegion entered = new ESetProtectedRegion(Sets.difference(toRegions.getAll(), this.lastRegions.getAll()));
        SetProtectedRegion exited = new ESetProtectedRegion(Sets.difference(this.lastRegions.getAll(), toRegions.getAll()));
        
        
        Location<World> lastLocation = this.lastLocation;
		SetProtectedRegion lastRegions = this.lastRegions;
        this.lastLocation = toLocation.setPosition(toLocation.getBlockPosition().toDouble());
		this.lastRegions = toRegions;
		
		this.plugin.getGame().getEventManager().post(
				ESpongeEventFactory.createMoveRegionEventPost(player, lastLocation, toLocation, lastRegions, toRegions, entered, exited, cause));
	}
	
	/*
	 * Accesseurs
	 */
	
	public String getIdentifier() {
		return this.identifier.toString();
	}
	
	public UUID getUniqueId() {
		return this.identifier;
	}
	
	@SuppressWarnings("unused")
	private Optional<EPlayer> getEPlayer() {
		return this.plugin.getEServer().getEPlayer(this.getUniqueId());
	}

	public Location<World> getLastLocation() {
		return this.lastLocation;
	}
	
	public boolean getLastRide() {
		return this.lastRide;
	}
	
	public void setLastRide(boolean ride) {
		this.lastRide = ride;
	}
}
