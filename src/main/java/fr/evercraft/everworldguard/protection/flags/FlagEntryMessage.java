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

import fr.evercraft.everapi.event.MoveRegionEvent.Post;
import fr.evercraft.everapi.message.EMessageBuilder;
import fr.evercraft.everapi.services.worldguard.flag.type.MessageFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagEntryMessage extends MessageFlag {
	
	public FlagEntryMessage() {
		super("ENTRY_MESSAGE");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENTRY_MESSAGE_DESCRIPTION.getString();
	}

	@Override
	public EMessageBuilder getDefault() {
		return EMessageBuilder.empty();
	}
	
	public void onMoveRegionPost(Post event) {
		event.getEnterRegions().getFlagIfPresent(event.getPlayer(), this).ifPresent(message ->
			message.prefix(EWMessages.PREFIX)
				.build()
				.sender()
				.sendTo(event.getPlayer()));
	}
}
