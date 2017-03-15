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

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagEntry extends StateFlag {

	public FlagEntry() {
		super("ENTRY");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENTRY.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveRegion(MoveRegionEvent.Pre.Cancellable event) {
		if(event.getEnterRegions().getFlag(event.getPlayer(), Flags.ENTRY).equals(State.DENY)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("MoveRegion : DENY");
		}
	}
}