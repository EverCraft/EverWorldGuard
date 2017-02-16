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
package fr.evercraft.everworldguard.selection.region;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.selection.SelectionRegion;
import org.spongepowered.api.world.World;

import java.util.Optional;

public abstract class ESelectionRegion implements SelectionRegion {

	protected World world;
	
	public ESelectionRegion(World world) {
		this.world = world;
	}

	@Override
	public Optional<World> getWorld() {
		return Optional.of(this.world);
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public Vector3i getCenter() {
		return this.getMinimumPoint().add(this.getMaximumPoint()).div(2);
	}

	@Override
	public int getWidth() {
		return this.getMinimumPoint().getX() - this.getMaximumPoint().getX() + 1;
	}

	@Override
	public int getHeight() {
		return this.getMinimumPoint().getZ() - this.getMaximumPoint().getZ() + 1;
	}

	@Override
	public int getLength() {
		return this.getMinimumPoint().getY() - this.getMaximumPoint().getY() + 1;
	}	
}
