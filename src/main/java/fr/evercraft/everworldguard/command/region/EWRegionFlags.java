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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionFlags extends ESubCommand<EverWorldGuard> {
	
	private final Args.Builder pattern;
	
	public EWRegionFlags(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "flags");
        
        this.pattern = Args.builder()
    		.arg((source, args) -> {
				return this.plugin.getProtectionService().getFlags().stream()
						.map(flag -> flag.getName())
						.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_FLAGS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_FLAGS_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [-w " + EAMessages.ARGS_WORLD.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(source, args);
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args_list) throws CommandException {
		Args args = this.pattern.build(args_list);
		List<String> args_string = args.getArgs();
		
		if (args_string.size() > 1) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
		}
		
		if (args_string.size() == 1) {
			return this.commandRegionFlags(source, args_string.get(0));
		}
		
		return this.commandRegionFlags(source);
	}
	
	private CompletableFuture<Boolean> commandRegionFlags(final CommandSource source) {
		TreeMap<String, Text> map = new TreeMap<String, Text>();
		for (Flag<?> flag : this.plugin.getProtectionService().getFlags()) {
			map.put(flag.getName(), EWMessages.REGION_FLAGS_LIST_LINE.getFormat().toText(
					"<flag>", flag.getName(),
					"<description>", flag.getDescription()));
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
			EWMessages.REGION_FLAGS_LIST_TITLE.getText()
				.toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				new ArrayList<Text>(map.values()), source);
		return CompletableFuture.completedFuture(false);
	}

	private CompletableFuture<Boolean> commandRegionFlags(final CommandSource source, final String flagString) {
		Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(flagString);
		if (!flag.isPresent()) {
			EWMessages.FLAG_NOT_FOUND.sender()
				.replace("<flag>", flagString)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		EWMessages.REGION_FLAGS_MESSAGE.sender()
			.replace("<flag>", flag.get().getName())
			.replace("<description>", flag.get().getDescription())
			.sendTo(source);
		return CompletableFuture.completedFuture(true);
	}
}
