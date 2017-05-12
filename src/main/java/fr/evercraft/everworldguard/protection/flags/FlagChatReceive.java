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

import java.util.Collection;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import com.google.common.collect.Lists;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class FlagChatReceive extends StateFlag {
	
	private final EverWorldGuard plugin;

	public FlagChatReceive(EverWorldGuard plugin) {
		super("CHAT_RECEIVE");
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_CHAT_RECEIVE_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Listener
    public void onMessageChannelChat(MessageChannelEvent.Chat event, WorldWorldGuard worldSender, Player playerSender) {
		if (event.isCancelled()) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		Collection<MessageReceiver> members = event.getChannel().orElse(event.getOriginalChannel()).getMembers();
		List<MessageReceiver> list = Lists.newArrayList(members);
        list.removeIf(messageReceiver -> {
        	if(messageReceiver instanceof Player) {
        		Player playerReceiver = (Player) messageReceiver;
        		WorldWorldGuard worldReceiver = service.getOrCreateWorld(playerReceiver.getWorld());
        		return worldReceiver.getRegions(playerReceiver.getLocation().getPosition()).getFlag(playerReceiver, this).equals(State.DENY);
        	}
        	return false;
        });
        
        if (list.size() != members.size()) {
            event.setChannel(MessageChannel.fixed(list));
        }
    }
}
