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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.command.SendCommandEvent;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.MapStringFlag;
import fr.evercraft.everapi.services.worldguard.flag.value.EntryFlagValue;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagCommand extends MapStringFlag {
	
	private final EntryFlagValue<String> ALL = new EntryFlagValue<String>(ImmutableSet.of("ALL"), ImmutableSet.of("*"));
	
	private final EverWorldGuard plugin;

	public FlagCommand(EverWorldGuard plugin) {
		super("COMMAND");
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<String>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().getString(this.getName());
	}
	
	@Override
	public EntryFlagValue<String> getDefault() {
		return ALL;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_COMMAND_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position, String command) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_COMMAND_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<command>", command));
	}
		
	public void onSendCommand(SendCommandEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld());
		EntryFlagValue<String> flag = world.getRegions(player.getLocation().getPosition()).getFlag(player, this);
		
		Optional<? extends CommandMapping> optCommand = this.plugin.getGame().getCommandManager().get(event.getCommand(), player);
		if (!optCommand.isPresent()) return;
		CommandMapping command = optCommand.get();
		
		if (flag.containsValue("*") || flag.containsValue(command.getPrimaryAlias())) {
			if (!flag.containsValue("-" + command.getPrimaryAlias())) {
				return;
			}
		}
		
		event.setCancelled(true);
		event.setResult(CommandResult.empty());
		this.sendMessage(player, player.getLocation().getPosition().toInt(), command.getPrimaryAlias());
	}
}
