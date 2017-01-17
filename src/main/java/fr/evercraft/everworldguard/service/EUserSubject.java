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
package fr.evercraft.everworldguard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SelectType;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everapi.services.worldguard.regions.Region;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.CuboidRegion;

public class EUserSubject implements SubjectWorldGuard {
	
	private final EverWorldGuard plugin;
	
	private final UUID identifier;
	
	private Vector3i pos1;
	private Vector3i pos2;
	private List<Vector3i> points;
	private SelectType type;

	public EUserSubject(final EverWorldGuard plugin, final UUID uuid) {
		Preconditions.checkNotNull(plugin, "plugin");
		Preconditions.checkNotNull(uuid, "uuid");
		
		this.plugin = plugin;
		this.identifier = uuid;
		this.points = new ArrayList<Vector3i>();
	}
	
	@Override
	public Optional<Vector3i> getSelectPos1() {
		return Optional.ofNullable(this.pos1);
	}

	@Override
	public boolean setSelectPos1(Vector3i pos) {
		if (this.pos1 == null || !this.pos1.equals(pos)) {
			this.pos1 = pos;
			return true;
		}
		return false;
	}

	@Override
	public Optional<Vector3i> getSelectPos2() {
		return Optional.ofNullable(this.pos2);
	}

	@Override
	public boolean setSelectPos2(Vector3i pos) {
		if (this.pos2 == null || !this.pos2.equals(pos)) {
			this.pos2 = pos;
			return true;
		}
		return false;
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
	public SelectType getSelectType() {
		if (this.type != null) {
			return this.type;
		}
		return SelectType.CUBOID;
	}

	@Override
	public boolean setSelectType(SelectType type) {
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
		if (this.getSelectType().equals(SelectType.CUBOID)) {
			if (this.pos1 != null && this.pos2 != null) {
				return Optional.of(new CuboidRegion(null, this.pos1, this.pos2));
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
