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
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
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
						return this.plugin.getGame().getRegistry().getAllOf(Flag.class).stream()
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
		return Text.builder("/" + this.getName() + " <" + MARKER_REGION_GROUP + " " + EAMessages.ARGS_REGION_GROUP.getString()
												 + " | " + MARKER_FLAG + " " + EAMessages.ARGS_FLAG.getString() + ">")
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
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		EPlayer player = (EPlayer) source;
		
		Args args = this.pattern.build(args_list);
		
		Optional<String> optGroupString = args.getValue(MARKER_REGION_GROUP);
		Optional<String> optFlagString = args.getValue(MARKER_FLAG);
		
		if (!args.getArgs().isEmpty() || args.countValues() != 1) {
			player.sendMessage(this.help(player));
			return CompletableFuture.completedFuture(false);
		}
		
		if (optGroupString.isPresent()) {
			Optional<ProtectedRegion.Group> group = this.plugin.getGame().getRegistry().getType(ProtectedRegion.Group.class, optGroupString.get());
			if (!group.isPresent()) {
				EWMessages.GROUP_NOT_FOUND.sender()
					.replace("<group>", optGroupString.get())
					.sendTo(source);
				return CompletableFuture.completedFuture(false);
			}
			
			return this.commandRegionCheck(player, group.get());
		}
		
		if (optFlagString.isPresent()) {
			Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(optFlagString.get());
			if (!flag.isPresent()) {
				EWMessages.FLAG_NOT_FOUND.sender()
					.replace("<flag>", optFlagString.get())
					.sendTo(source);
				return CompletableFuture.completedFuture(false);
			}
			
			return this.commandRegionCheck(player, flag.get());
		}
		return CompletableFuture.completedFuture(false);
	}
	
	private CompletableFuture<Boolean> commandRegionCheck(final EPlayer player, final ProtectedRegion.Group group) {
		SetProtectedRegion regions = player.getRegions();
		TreeMap<String, Text> map = new TreeMap<String, Text>();
		
		for (Flag<?> flag: this.plugin.getProtectionService().getFlags()) {
			if (flag.getGroups().contains(group)) {
				map.put(flag.getName(), 
					this.getMessage(regions, player.getWorld(), flag, ProtectedRegion.Groups.DEFAULT, EWMessages.REGION_CHECK_GROUP_LINE, EWMessages.REGION_CHECK_GROUP_LINE_DEFAULT));
			}
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.REGION_CHECK_GROUP_TITLE.getFormat()
					.toText("<group>", group.getNameFormat())
					.toBuilder()
					.onClick(TextActions.runCommand("/" + this.getName() + " " + MARKER_REGION_GROUP + " " + group.getName()))
					.build(), 
					new ArrayList<Text>(map.values()), player);
		
		return CompletableFuture.completedFuture(true);
	}

	private CompletableFuture<Boolean> commandRegionCheck(final EPlayer player, final Flag<?> flag) {
		SetProtectedRegion regions = player.getRegions();
		List<Text> list = new ArrayList<Text>();
		
		if (flag.getGroups().contains(ProtectedRegion.Groups.DEFAULT)) {
			list.add(this.getMessage(regions, player.getWorld(), flag, ProtectedRegion.Groups.DEFAULT, EWMessages.REGION_CHECK_FLAG_DEFAULT, EWMessages.REGION_CHECK_FLAG_DEFAULT_DEFAULT));
		}
		
		if (flag.getGroups().contains(ProtectedRegion.Groups.MEMBER)) {
			list.add(this.getMessage(regions, player.getWorld(), flag, ProtectedRegion.Groups.MEMBER, EWMessages.REGION_CHECK_FLAG_MEMBER, EWMessages.REGION_CHECK_FLAG_MEMBER_DEFAULT));
		}

		if (flag.getGroups().contains(ProtectedRegion.Groups.OWNER)) {
			list.add(this.getMessage(regions, player.getWorld(), flag, ProtectedRegion.Groups.OWNER, EWMessages.REGION_CHECK_FLAG_OWNER, EWMessages.REGION_CHECK_FLAG_OWNER_DEFAULT));
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.REGION_CHECK_FLAG_TITLE.getFormat()
					.toText("<flag>", flag.getNameFormat())
					.toBuilder()
					.onClick(TextActions.runCommand("/" + this.getName()))
					.build(), 
				list, player);
		
		return CompletableFuture.completedFuture(true);
	}
	
	private <V> Text getMessage(final SetProtectedRegion regions, final World world, final Flag<V> flag, 
			final ProtectedRegion.Group group, final EWMessages messageRegion, final EWMessages messageDefault) {
		V value = regions.getFlag(ProtectedRegion.Groups.DEFAULT, flag);
		String valueString = flag.serialize(value);
		Text valueFormat = flag.getValueFormat(value);
		Optional<ProtectedRegion> region = regions.getRegion(ProtectedRegion.Groups.DEFAULT, flag);
		
		if (region.isPresent()) {
			return messageRegion.getFormat()
						.toText("<region>",  Text.builder(region.get().getName())
												.onShiftClick(TextActions.insertText(flag.getId()))
												.onClick(TextActions.suggestCommand(
					"/" + this.getParentName() + " info -w \"" + world.getName() + "\" \"" + region.get().getName() + "\""))
												.build(),
								"<flag>",  flag.getNameFormat().toBuilder()
												.onShiftClick(TextActions.insertText(flag.getId()))
												.build(),
								"<value>", valueFormat.toBuilder()
												.onShiftClick(TextActions.insertText(valueString))
												.onClick(TextActions.suggestCommand(
					"/" + this.getParentName() + " removeflag -w \"" + world.getName() + "\" \"" + region.get().getName() + "\" \"" + flag.getName() + "\" \"" + ProtectedRegion.Groups.DEFAULT.getName() + "\""))
												.build());
		} else {
			return messageDefault.getFormat()
						.toText("<flag>",  flag.getNameFormat().toBuilder()
												.onShiftClick(TextActions.insertText(flag.getId()))
												.build(),
								"<value>", valueFormat.toBuilder()
												.onShiftClick(TextActions.insertText(valueString))
												.build());
		}
	}
}
