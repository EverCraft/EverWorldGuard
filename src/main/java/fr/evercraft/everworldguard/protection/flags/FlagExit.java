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

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagExit extends StateFlag {

	public FlagExit() {
		super("EXIT");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_EXIT_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public void onMoveRegionPreCancellable(MoveRegionEvent.Pre.Cancellable event) {
		// Bypass
		if (event.getPlayer().hasProtectionBypass()) return;
		
		if(event.getExitRegions().getFlag(event.getPlayer(), this).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
}
