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

import fr.evercraft.everapi.server.location.EVirtualPosition;
import fr.evercraft.everapi.server.location.VirtualTransform;
import fr.evercraft.everapi.services.worldguard.flag.LocationFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagTeleport extends LocationFlag {

	public FlagTeleport(EverWorldGuard plugin) {
		super(plugin, "TELEPORT");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_TELEPORT_DESCRIPTION.getString();
	}

	@Override
	public VirtualTransform getDefault() {
		return VirtualTransform.empty();
	}
	
	@Override
	public VirtualTransform getDefault(ProtectedRegion region) {
		return new EVirtualPosition(region.getMinimumPoint().add(region.getMaximumPoint()).toDouble().div(2));
	}
}
