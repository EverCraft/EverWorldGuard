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
package fr.evercraft.everworldguard.command.region;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionBypass extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_PLAYER = "-p";
	
	private final Args.Builder pattern;
	
	public EWRegionBypass(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "bypass");
        
        this.pattern = Args.builder()
        	.value(MARKER_PLAYER, (source, args) -> {
        		List<String> list = args.getArgs();
				Set<String> suggests = this.getAllUsers(list.get(list.size()-1), source);
				return suggests;
			})
    		.arg((source, args) -> Arrays.asList("on", "off", "status"));
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_BYPASS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_BYPASS_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + "[on|off|status] [-p " + EAMessages.ARGS_PLAYER.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(source, args);
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args_list) throws CommandException {
		Args args = this.pattern.build(args_list);
		List<String> argsString = args.getArgs();
		
		if (argsString.size() > 1) {
			source.sendMessage(this.help(source));
			return false;
		}
		
		Optional<String> playerString = args.getValue(MARKER_PLAYER);
		if (playerString.isPresent()) {
			return this.commandRegionBypass(source, playerString.get(), argsString);
		}
		
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
		}
		
		String value = this.getValue((EPlayer) source, argsString);
		if (value.equalsIgnoreCase("on")) {
			return this.commandRegionBypassOn((EPlayer) source);
		} else if (value.equalsIgnoreCase("off")) {
			return this.commandRegionBypassOff((EPlayer) source);
		} else if (value.equalsIgnoreCase("status")) {
			return this.commandRegionBypassStatus((EPlayer) source);
		}
		
		source.sendMessage(this.help(source));
		return false;
	}
	
	private String getValue(EPlayer player, List<String> argsString) {
		if (argsString.isEmpty()) {
			if (!player.hasProtectionBypass()) {
				return "on";
			} else {
				return "off";
			}
		} else {
			return argsString.get(0);
		}
	}
	
	private boolean commandRegionBypassOn(EPlayer player) {
		if (player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_ON_PLAYER_ERROR.sendTo(player);
			return false;
		}
		
		player.setProtectionBypass(true);
		EWMessages.REGION_BYPASS_ON_PLAYER.sendTo(player);
		return true;
	}
	
	private boolean commandRegionBypassOff(EPlayer player) {
		if (!player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_OFF_PLAYER_ERROR.sendTo(player);
			return false;
		}
		
		player.setProtectionBypass(true);
		EWMessages.REGION_BYPASS_OFF_PLAYER.sendTo(player);
		return true;
	}
	
	private boolean commandRegionBypassStatus(EPlayer player) {
		if (player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_STATUS_PLAYER_ON.sendTo(player);
		} else {
			EWMessages.REGION_BYPASS_STATUS_PLAYER_OFF.sendTo(player);
		}
		return true;
	}
	
	private boolean commandRegionBypass(CommandSource staff, String playerString, List<String> argsString) {
		Optional<EPlayer> optPlayer = this.plugin.getEServer().getEPlayer(playerString);
		if (!optPlayer.isPresent()) {
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<player>", playerString)
				.sendTo(staff);
			return false;
		}
		
		EPlayer player = optPlayer.get();
		String value = this.getValue(player, argsString);
		if (value.equalsIgnoreCase("on")) {
			return this.commandRegionBypassOn(staff, player);
		} else if (value.equalsIgnoreCase("off")) {
			return this.commandRegionBypassOff(staff, player);
		} else if (value.equalsIgnoreCase("status")) {
			return this.commandRegionBypassStatus(staff, player);
		}
		
		staff.sendMessage(this.help(staff));
		return false;
	}
	
	private boolean commandRegionBypassOn(CommandSource staff, EPlayer player) {
		if (player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_ON_OTHERS_ERROR.sender()
				.replace("<player>", player.getName())
				.sendTo(staff);
			return false;
		}
		
		player.setProtectionBypass(true);
		EWMessages.REGION_BYPASS_ON_OTHERS_PLAYER.sender()
			.replace("<staff>", staff.getName())
			.sendTo(player);
		EWMessages.REGION_BYPASS_ON_OTHERS_STAFF.sender()
			.replace("<player>", player.getName())
			.sendTo(staff);
		return true;
	}
	
	private boolean commandRegionBypassOff(CommandSource staff, EPlayer player) {
		if (!player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_OFF_OTHERS_ERROR.sender()
				.replace("<player>", player.getName())
				.sendTo(staff);
			return false;
		}
		
		player.setProtectionBypass(true);
		EWMessages.REGION_BYPASS_OFF_OTHERS_PLAYER.sender()
			.replace("<staff>", staff.getName())
			.sendTo(player);
		EWMessages.REGION_BYPASS_OFF_OTHERS_STAFF.sender()
			.replace("<player>", player.getName())
			.sendTo(staff);
		return true;
	}
	
	private boolean commandRegionBypassStatus(CommandSource staff, EPlayer player) {
		if (player.hasProtectionBypass()) {
			EWMessages.REGION_BYPASS_STATUS_OTHERS_ON.sender()
				.replace("<player>", player.getName())
				.sendTo(staff);
		} else {
			EWMessages.REGION_BYPASS_STATUS_OTHERS_OFF.sender()
				.replace("<player>", player.getName())
				.sendTo(staff);
		}
		return true;
	}
	
}
