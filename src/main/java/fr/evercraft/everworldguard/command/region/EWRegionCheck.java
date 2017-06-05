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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionCheck extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_REGION_GROUP = "-g";
	public static final String MARKER_FLAG = "-f";
	
	private final Args.Builder pattern;
	
	public EWRegionCheck(final EverWorldGuard plugin, final EWRegion command) {
		super(plugin, command, "check");

		this.pattern = Args.builder()
			.value(MARKER_REGION_GROUP, 
					(source, args) -> {
						return this.plugin.getGame().getRegistry().getAllOf(ProtectedRegion.Group.class).stream()
							.map(group -> group.getName())
							.collect(Collectors.toList());
					},
					(source, args) -> args.countValues() == 0)
			.value(MARKER_FLAG, 
					(source, args) -> {
						return this.plugin.getGame().getRegistry().getAllOf(ProtectedRegion.Group.class).stream()
							.map(group -> group.getName())
							.collect(Collectors.toList());
					},
					(source, args) -> args.countValues() == 0);
	}
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_CHECK.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_FLAG_ADD_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_REGION_GROUP + " " + EAMessages.ARGS_REGION_GROUP.getString()
												 + " | " + MARKER_FLAG + " " + EAMessages.ARGS_FLAG.getString() + "]")
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
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		EPlayer player = (EPlayer) source;
		
		Args args = this.pattern.build(args_list);
		
		Optional<String> optGroupString = args.getValue(MARKER_REGION_GROUP);
		Optional<String> optFlagString = args.getValue(MARKER_FLAG);
		
		if (!args.getArgs().isEmpty() || optGroupString.isPresent() && optFlagString.isPresent()) {
			player.sendMessage(this.help(player));
			return false;
		}
		
		if (optGroupString.isPresent()) {
			Optional<Group> group = this.plugin.getGame().getRegistry().getType(Group.class, optGroupString.get());
			if (!group.isPresent()) {
				EWMessages.GROUP_NOT_FOUND.sender()
					.replace("<group>", optGroupString.get())
					.sendTo(source);
				return false;
			}
			
			return this.commandRegionCheck(player, group.get());
		}
		
		if (optFlagString.isPresent()) {
			Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(optFlagString.get());
			if (!flag.isPresent()) {
				EWMessages.FLAG_NOT_FOUND.sender()
					.replace("<flag>", optFlagString.get())
					.sendTo(source);
				return false;
			}
			
			return this.commandRegionCheck(player, flag.get());
		}
		
		return this.commandRegionCheck(player);
	}

	private boolean commandRegionCheck(final EPlayer source) {
		return true;
	}
	
	private boolean commandRegionCheck(final EPlayer source, Group group) {
		return true;
	}

	private boolean commandRegionCheck(final EPlayer source, Flag<?> flag) {
		return true;
	}
}
