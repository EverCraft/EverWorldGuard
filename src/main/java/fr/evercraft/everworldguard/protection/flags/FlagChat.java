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
import java.util.Map;
import java.util.Set;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;

import fr.evercraft.everapi.registers.ChatType;
import fr.evercraft.everapi.registers.ChatType.ChatTypes;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.CatalogTypeFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagChat extends CatalogTypeFlag<ChatType> {
	
	private final EverWorldGuard plugin;

	public FlagChat(EverWorldGuard plugin) {
		super("CHAT");
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<ChatType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), ChatType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_CHAT_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_CHAT_SEND_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ()));
	}
	
    public void onMessageChannelChat(MessageChannelEvent.Chat event, WorldWorldGuard worldSender, Player playerSender) {
		this.onMessageChannelChatSend(event, worldSender, playerSender);
		this.onMessageChannelChatReceive(event, worldSender, playerSender);
	}
		
	public void onMessageChannelChatSend(MessageChannelEvent.Chat event, WorldWorldGuard worldSender, Player playerSender) {
		if (event.isCancelled()) return;
		
		if (!this.getDefault().containsValue(ChatTypes.SEND)) return;
		
		if (!worldSender.getRegions(playerSender.getLocation().getPosition()).getFlag(playerSender, this).containsValue(ChatTypes.SEND)) {
			event.setCancelled(true);
			this.sendMessage(playerSender, playerSender.getLocation().getPosition().toInt());
			return;
		}
	}
		
	public void onMessageChannelChatReceive(MessageChannelEvent.Chat event, WorldWorldGuard worldSender, Player playerSender) {
		if (event.isCancelled()) return;
		
		if (!this.getDefault().containsValue(ChatTypes.RECEIVE)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		Collection<MessageReceiver> members = event.getChannel().orElse(event.getOriginalChannel()).getMembers();
		List<MessageReceiver> list = Lists.newArrayList(members);
        list.removeIf(messageReceiver -> {
        	if(messageReceiver instanceof Player && !playerSender.equals(messageReceiver)) {
        		Player playerReceiver = (Player) messageReceiver;
        		WorldWorldGuard worldReceiver = service.getOrCreateWorld(playerReceiver.getWorld());
        		return worldReceiver.getRegions(playerReceiver.getLocation().getPosition()).getFlag(playerReceiver, this).containsValue(ChatTypes.RECEIVE);
        	}
        	return false;
        });
        
        if (list.size() != members.size()) {
            event.setChannel(MessageChannel.fixed(list));
        }
    }
}
