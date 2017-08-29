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
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.WorldGuardService.Priorities;
import fr.evercraft.everapi.services.worldguard.flag.MessageFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagEntryDenyMsg extends MessageFlag {
	
	public FlagEntryDenyMsg() {
		super("ENTRY_DENY_MSG");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENTRY_DENY_MESSAGE_DESCRIPTION.getString();
	}

	@Override
	public EMessageBuilder getDefault() {
		return EWMessages.FLAG_ENTRY_DENY_MESSAGE_DEFAULT.getBuilder();
	}
	
	private void sendMessage(EPlayer player, EMessageBuilder message, String region) {
		message.prefix(EWMessages.PREFIX)
			.build(Priorities.FLAG)
			.sender()
			.replace("<region>", region)
			.sendTo(player);
	}
	
	public void onMoveRegionPreCancelled(MoveRegionEvent.Pre.Cancellable event) {
		if (!event.isCancelled()) return;
		
		Set<ProtectedRegion> regions = event.getEnterRegions().getAll();
		if (regions.isEmpty()) return;
		
		EPlayer player = event.getPlayer();
		Set<Context> context = UtilsContexts.get(player.getWorld().getName());
		
		for (ProtectedRegion region : regions) {
			Optional<EMessageBuilder> flag_value = region.getFlagInherit(this, region.getGroup(player, context));
			if (flag_value.isPresent()) {
				this.sendMessage(player, flag_value.get(), region.getName());
				return;
			}
		}
		this.sendMessage(player, this.getDefault(), regions.iterator().next().getName());
 	}
}
