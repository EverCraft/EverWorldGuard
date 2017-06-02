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
import java.util.Set;

import org.spongepowered.api.service.context.Context;

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.message.EMessageBuilder;
import fr.evercraft.everapi.message.EMessageFormat;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everapi.services.worldguard.flag.type.MessageFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
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
		return EMessageFormat.builder();
	}
	
	private void sendMessage(EPlayer player, EMessageBuilder message, String region) {
		message.prefix(EWMessages.PREFIX)
			.build(WorldGuardService.MESSAGE_FLAG)
			.sender()
			.replace("<region>", region)
			.sendTo(player);
	}
	
	public void onMoveRegionPost(MoveRegionEvent.Post event) {
		Set<ProtectedRegion> regions = event.getEnterRegions().getAll();
		if (regions.isEmpty()) return;
		
		EPlayer player = event.getPlayer();
		Set<Context> context = player.getActiveContexts();
		
		for (ProtectedRegion region : regions) {
			Optional<EMessageBuilder> flag_value = region.getFlagInherit(this, region.getGroup(player, context));
			if (flag_value.isPresent()) {
				this.sendMessage(player, flag_value.get(), region.getName());
				return;
			}
		}
	}
}
