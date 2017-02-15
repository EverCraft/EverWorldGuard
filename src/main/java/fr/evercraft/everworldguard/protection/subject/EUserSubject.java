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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;

import fr.evercraft.everapi.event.ESpongeEventFactory;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.SelectionType;
import fr.evercraft.everapi.services.worldguard.MoveType;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.Region;
import fr.evercraft.everapi.sponge.UtilsLocation;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.ESetProtectedRegion;

public class EUserSubject implements SubjectWorldGuard {
	
	private final EverWorldGuard plugin;
	
	private final UUID identifier;
	
	private Vector3i pos1;
	private Vector3i pos2;
	private List<Vector3i> points;
	private SelectionType type;
	
	private Location<World> lastLocation;
	private SetProtectedRegion lastRegions;

	public EUserSubject(final EverWorldGuard plugin, final UUID identifier) {
		Preconditions.checkNotNull(plugin, "plugin");
		Preconditions.checkNotNull(identifier, "identifier");
		
		this.plugin = plugin;
		this.identifier = identifier;
		this.points = new ArrayList<Vector3i>();
		
		this.lastRegions = SetProtectedRegion.empty();
	}
	
	/*
	 * Region
	 */
	
	public void initialize(Player player) {
		this.lastLocation = player.getLocation();
		this.lastLocation = this.lastLocation.setPosition(this.lastLocation.getBlockPosition().toDouble());
		
		this.moveToPost(player, this.lastLocation, MoveType.OTHER_NON_CANCELLABLE, Cause.source(this.plugin).build(), true);
	}
	
	@Override
	public SetProtectedRegion getRegions() {
		return this.lastRegions;
	}
	
	public Optional<Location<World>> moveToPre(Player player_sponge, Location<World> toLocation, MoveType move, Cause cause) {
		if (!UtilsLocation.isDifferentBlock(this.lastLocation, toLocation)) return Optional.empty();
		
		EPlayer player = this.plugin.getEServer().getEPlayer(player_sponge);
		SetProtectedRegion toRegions = this.plugin.getService().getOrCreateWorld(toLocation.getExtent()).getRegions(toLocation.getPosition());
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
		SetProtectedRegion toRegions = this.plugin.getService().getOrCreateWorld(toLocation.getExtent()).getRegions(toLocation.getPosition());
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
	 * Select
	 */
	
	@Override
	public Optional<Vector3i> getSelectPos1() {
		return Optional.ofNullable(this.pos1);
	}

	@Override
	public boolean setSelectPos1(Vector3i pos) {
		this.pos1 = pos;
		return true;
	}

	@Override
	public Optional<Vector3i> getSelectPos2() {
		return Optional.ofNullable(this.pos2);
	}

	@Override
	public boolean setSelectPos2(Vector3i pos) {
		this.pos2 = pos;
		return true;
	}

	@Override
	public List<Vector3i> getSelectPoints() {
		Builder<Vector3i> build = ImmutableList.builder();
		if (this.pos1 != null) {
			build.add(this.pos1);
		}
		build.addAll(this.points);
		if (this.pos2 != null) {
			build.add(this.pos2);
		}
		return build.build();
	}

	@Override
	public boolean addSelectPoint(Vector3i pos) {
		Preconditions.checkNotNull(pos, "pos");
		
		this.points.add(pos);
		return true;
	}
	
	@Override
	public boolean setSelectPoints(List<Vector3i> pos) {
		Preconditions.checkNotNull(pos, "pos");
		
		this.points.clear();
		this.points.addAll(pos);
		return true;
	}

	@Override
	public boolean removeSelectPoint(Vector3i pos) {
		Preconditions.checkNotNull(pos, "pos");
		
		this.points.remove(pos);
		return true;
	}
	
	@Override
	public boolean removeSelectPoint(int num) {
		Preconditions.checkArgument(num >= 0 && num < this.points.size(), "index");
		
		return this.points.remove(num) != null;
	}

	@Override
	public boolean clearSelectPoints() {
		this.points.clear();
		return true;
	}

	@Override
	public SelectionType getSelectType() {
		if (this.type != null) {
			return this.type;
		}
		return SelectionType.CUBOID;
	}

	@Override
	public boolean setSelectType(SelectionType type) {
		Preconditions.checkNotNull(type, "type");
		
		if (this.type == null || !this.type.equals(type)) {
			this.type = type;
			return true;
		}
		return false;
	}

	@Override
	public Optional<Integer> getSelectArea() {
		Optional<Region> region = this.getSelectRegion();
		if (region.isPresent()) {
			return Optional.of(region.get().getArea());
		}
		return Optional.empty();
    }
	
	@Override
	public Optional<Region> getSelectRegion() {
		if (this.getSelectType().equals(SelectionType.CUBOID)) {
			if (this.pos1 != null && this.pos2 != null) {
				return Optional.empty();
			}
		}
		return Optional.empty();
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
}
