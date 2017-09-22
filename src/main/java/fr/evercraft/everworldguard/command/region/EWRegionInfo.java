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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionInfo extends ESubCommand<EverWorldGuard> {
		
	private final Args.Builder pattern;
	
	public EWRegionInfo(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "info");
        
        this.plugin.getGame().getEventManager().registerListeners(this.plugin, this);
        
        this.pattern = Args.builder()
    		.value(Args.MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				return this.plugin.getProtectionService().getOrCreateEWorld(args.getWorld()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_INFO.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_INFO_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [[" + Args.MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " " + EAMessages.ARGS_REGION.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(this.plugin, source, args);
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args_list) throws CommandException {
		Args args = this.pattern.build(this.plugin, source, args_list);
		
		if (args.getArgs().size() == 0) {
			if (source instanceof EPlayer) {
				Optional<String> world = args.getValue(Args.MARKER_WORLD);
				if (world.isPresent()) {
					source.sendMessage(this.help(source));
				} else {
					EPlayer player = (EPlayer) source;
					return this.commandRegionInfo(source, player.getRegions(), player.getWorld());
				}
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else if (args.getArgs().size() == 1) {
			Optional<String> world = args.getValue(Args.MARKER_WORLD);
			if (world.isPresent()) {
				return this.commandRegionInfo(source, args.getArgs().get(0), world.get());
			} else {
				if (source instanceof EPlayer) {
					return this.commandRegionInfo(source, args.getArgs().get(0), ((EPlayer) source).getWorld());
				} else {
					EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
						.prefix(EWMessages.PREFIX)
						.sendTo(source);
				}
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return CompletableFuture.completedFuture(false);
	}
	
	private CompletableFuture<Boolean> commandRegionInfo(final CommandSource player, final SetProtectedRegion setregions, final World world) {
		if (setregions.getAll().isEmpty()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		Set<ProtectedRegion> regions = setregions.getAll().stream()
			.filter(region -> this.hasPermission(player, region, world))
			.collect(Collectors.toSet());
				
		if (regions.isEmpty()) {
			EWMessages.REGION_INFO_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		} else if (regions.size() == 1) {
			return this.commandRegionInfo(player, regions.iterator().next(), world);
		} else {
			regions = setregions.getAll().stream()
				.filter(region -> !region.getType().equals(ProtectedRegion.Types.GLOBAL))
				.collect(Collectors.toSet());
			
			if (regions.isEmpty()) {
				EWMessages.REGION_INFO_EMPTY.sendTo(player);
				return CompletableFuture.completedFuture(false);
			} else if (regions.size() == 1) {
				return this.commandRegionInfo(player, regions.iterator().next(), world);
			} else {
				return this.commandRegionInfo(player, regions, world);
			}
		}
	}	
	
	private CompletableFuture<Boolean> commandRegionInfo(final CommandSource player, final String region_string, final String world_string) {
		Optional<World> world = this.plugin.getEServer().getEWorld(world_string);
		// Monde introuvable
		if (!world.isPresent()) {
			EAMessages.WORLD_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{world}", world_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandRegionInfo(player, region_string, world.get());
	}
	
	private CompletableFuture<Boolean> commandRegionInfo(final CommandSource player, final String region_string, final World world) {
		Optional<ProtectedRegion> region = this.plugin.getProtectionService().getOrCreateEWorld(world).getRegion(region_string);
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{region}", region_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!this.hasPermission(player, region.get(), world)) {
			EWMessages.REGION_NO_PERMISSION.sender()
				.replace("{region}", region.get().getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}		
		
		return this.commandRegionInfo(player, region.get(), world);
	}
	
	private CompletableFuture<Boolean> commandRegionInfo(final CommandSource player, final Set<ProtectedRegion> regions, final World world) {
		TreeMap<ProtectedRegion, Text> map = new TreeMap<ProtectedRegion, Text>();
		
		for (ProtectedRegion region : regions) {
			this.addLine(map, region, EWMessages.REGION_INFO_LIST_LINE.getFormat()
					.toText("{region}", Text.builder(region.getName())
								.onShiftClick(TextActions.insertText(region.getName()))
								.onClick(TextActions.suggestCommand(
									"/" + this.getName() + " -w \"" + world.getName() + "\" \"" + region.getName() + "\""))
								.build(),
							"{type}", region.getType().getNameFormat(),
							"{priority}", Text.builder(String.valueOf(region.getPriority()))
								.onClick(TextActions.suggestCommand(
									"/" + this.getParentName() + " setpriority -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + region.getPriority()))
								.build()));
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.REGION_INFO_LIST_TITLE.getFormat()
					.toText("{region}", world.getName())
					.toBuilder()
					.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\""))
					.build(), 
				new ArrayList<Text>(map.values()), player);		
		return CompletableFuture.completedFuture(true);
	}

	@SuppressWarnings("unchecked")
	private <T> CompletableFuture<Boolean> commandRegionInfo(final CommandSource player, final ProtectedRegion region, final World world) {
		List<Text> list = new ArrayList<Text>();
		
		// World
		this.addLine(list, EWMessages.REGION_INFO_ONE_WORLD.getFormat()
								.toText("{world}", world.getName()));
		
		// Type
		this.addLine(list, EWMessages.REGION_INFO_ONE_TYPE.getFormat()
								.toText("{type}", region.getType().getNameFormat()));
		
		// Priority
		this.addLine(list, EWMessages.REGION_INFO_ONE_PRIORITY.getFormat()
				.toText("{prority}", Text.builder(String.valueOf(region.getPriority()))
					.onClick(TextActions.suggestCommand(
						"/" + this.getParentName() + " setpriority -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + region.getPriority()))
					.build()));
		
		// Points
		if (region.getType().equals(ProtectedRegion.Types.CUBOID) || region.getType().equals(ProtectedRegion.Types.POLYGONAL)) {
			Vector3i min = region.getMinimumPoint();
			Vector3i max = region.getMaximumPoint();
			Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
			replaces.put("{min_x}", EReplace.of(String.valueOf(min.getX())));
			replaces.put("{min_y}", EReplace.of(String.valueOf(min.getY())));
			replaces.put("{min_z}", EReplace.of(String.valueOf(min.getZ())));
			replaces.put("{max_x}", EReplace.of(String.valueOf(max.getX())));
			replaces.put("{max_y}", EReplace.of(String.valueOf(max.getY())));
			replaces.put("{max_z}", EReplace.of(String.valueOf(max.getZ())));
			
			
			if (region.getType().equals(ProtectedRegion.Types.CUBOID)) {
				this.addLine(list, EWMessages.REGION_INFO_ONE_POINTS.getFormat()
						.toText("{positions}",  EWMessages.REGION_INFO_ONE_POINTS_CUBOID.getFormat()
								.toText2(replaces).toBuilder()
								.onHover(TextActions.showText(EWMessages.REGION_INFO_ONE_POINTS_CUBOID_HOVER.getFormat()
										.toText2(replaces)))
								.build()));
			} else if (region.getType().equals(ProtectedRegion.Types.POLYGONAL)) {
				List<Text> positions = new ArrayList<Text>();
				int num = 1;
				for(Vector3i pos : region.getPoints()) {
					positions.add(EWMessages.REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_POSITIONS.getFormat()
							.toText("{num}", String.valueOf(num),
									"{x}", String.valueOf(pos.getX()),
									"{y}", String.valueOf(pos.getY()),
									"{z}", String.valueOf(pos.getZ())));
					num++;
				}				
				replaces.put("{positions}", EReplace.of(Text.joinWith(EWMessages.REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_JOIN.getText(), positions)));
				
				this.addLine(list, EWMessages.REGION_INFO_ONE_POINTS.getFormat()
						.toText("{positions}",  EWMessages.REGION_INFO_ONE_POINTS_POLYGONAL.getFormat()
								.toText2(replaces).toBuilder()
								.onHover(TextActions.showText(EWMessages.REGION_INFO_ONE_POINTS_POLYGONAL_HOVER.getFormat()
										.toText2(replaces)))
								.build()));
			}
		}
		
		// Parent
		Optional<ProtectedRegion> parent = region.getParent();
		if (parent.isPresent()) {
			this.addLine(list, EWMessages.REGION_INFO_ONE_PARENT.getFormat()
					.toText("{parent}", Text.builder(parent.get().getName())
						.onShiftClick(TextActions.insertText(region.getName()))
						.onClick(TextActions.suggestCommand(
							"/" + this.getParentName() + " setparent -w \"" + world.getName() + "\" \"" + region.getName() + "\" \"" + parent.get().getName() + "\""))
						.build()));
		}
		
		// Héritage
		List<ProtectedRegion> parents = null;
		parents = region.getHeritage();
		if (parents.size() > 1) {
			List<Text> messages = new ArrayList<Text>();
			messages.add(EWMessages.REGION_INFO_ONE_HERITAGE.getText());
			
			Text padding = EWMessages.REGION_INFO_ONE_HERITAGE_PADDING.getText();
			for (int cpt=0; cpt < parents.size(); cpt++) {
				Text message = Text.EMPTY;
				for (int cpt2=0; cpt2 < cpt; cpt2++) {
					message = message.concat(padding);
				}
				
				ProtectedRegion curParent = parents.get(cpt);
				message = message.concat(EWMessages.REGION_INFO_ONE_HERITAGE_LINE.getFormat()
					.toText("{region}", Text.builder(curParent.getName())
								.onShiftClick(TextActions.insertText(curParent.getName()))
								.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\" \"" + curParent.getName() + "\" "))
								.build(),
							"{type}", curParent.getType().getNameFormat(),
							"{priority}", String.valueOf(curParent.getPriority())));
				messages.add(message);
			}
			this.addLine(list, Text.joinWith(Text.of("\n"), messages));
		}
		
		// Owner
		Set<UUID> owners = region.getOwners().getPlayers();
		if (!owners.isEmpty()) {
			List<Text> messages = new ArrayList<Text>();
			for (UUID owner : owners) {
				Optional<User> user = this.plugin.getEServer().getUser(owner);
				if (user.isPresent()) {
					messages.add(Text.builder(user.get().getName())
										.onShiftClick(TextActions.insertText(user.get().getName()))
										.onClick(TextActions.suggestCommand(
										"/" + this.getParentName() + " removeowner -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + user.get().getName()))
										.build());
				} else {
					messages.add(Text.builder(owner.toString())
							.onShiftClick(TextActions.insertText(owner.toString()))
							.onClick(TextActions.suggestCommand(
									"/" + this.getParentName() + " removeowner -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + owner.toString()))
							.build());
				}
			}
			
			this.addLine(list, EWMessages.REGION_INFO_ONE_OWNERS.getFormat()
					.toText("{owners}", Text.joinWith(EWMessages.REGION_INFO_ONE_OWNERS_JOIN.getText(), messages)));
		}
		
		// Groups Owner
		Set<String> groups_owners = region.getOwners().getGroups();
		if (!groups_owners.isEmpty()) {
			List<Text> messages = new ArrayList<Text>();
			for (String owner : groups_owners) {
				messages.add(Text.builder(owner)
					.onShiftClick(TextActions.insertText(owner))
					.onClick(TextActions.suggestCommand(
							"/" + this.getParentName() + " removeowner -w \"" + world.getName() + "\" \"" + region.getName() + "\" -g \"" + owner + "\""))
					.build());
			}
			
			this.addLine(list, EWMessages.REGION_INFO_ONE_GROUP_OWNERS.getFormat()
					.toText("{owners}", Text.joinWith(EWMessages.REGION_INFO_ONE_GROUP_OWNERS_JOIN.getText(), messages)));
		}
		
		// Members
		Set<UUID> members = region.getMembers().getPlayers();
		if (!members.isEmpty()) {
			List<Text> messages = new ArrayList<Text>();
			for (UUID member : members) {
				Optional<User> user = this.plugin.getEServer().getUser(member);
				if (user.isPresent()) {
					messages.add(Text.builder(user.get().getName())
						.onShiftClick(TextActions.insertText(user.get().getName()))
						.onClick(TextActions.suggestCommand(
								"/" + this.getParentName() + " removemember -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + user.get().getName()))
						.build());
				} else {
					messages.add(Text.builder(member.toString())
						.onShiftClick(TextActions.insertText(member.toString()))
						.onClick(TextActions.suggestCommand(
								"/" + this.getParentName() + " removemember -w \"" + world.getName() + "\" \"" + region.getName() + "\" " + member.toString()))
						.build());
				}
			}
			
			this.addLine(list, EWMessages.REGION_INFO_ONE_MEMBERS.getFormat()
					.toText("{members}", Text.joinWith(EWMessages.REGION_INFO_ONE_MEMBERS_JOIN.getText(), messages)));
		}
		
		// Groups Members
		Set<String> groups_members = region.getMembers().getGroups();
		if (!groups_members.isEmpty()) {
			List<Text> messages = new ArrayList<Text>();
			for (String member : groups_members) {
				messages.add(Text.builder(member)
					.onShiftClick(TextActions.insertText(member))
					.onClick(TextActions.suggestCommand(
							"/" + this.getParentName() + " removemember -w \"" + world.getName() + "\" \"" + region.getName() + "\" -g \"" + member + "\""))
					.build());
			}
			
			this.addLine(list, EWMessages.REGION_INFO_ONE_GROUP_MEMBERS.getFormat()
					.toText("{members}", Text.joinWith(EWMessages.REGION_INFO_ONE_GROUP_MEMBERS_JOIN.getText(), messages)));
		}
		
		// Flags
		Map<Flag<?>, FlagValue<?>> flags = region.getFlags();
		TreeMap<String, Text> flags_default = new TreeMap<String, Text>();
		TreeMap<String, Text> flags_member = new TreeMap<String, Text>();
		TreeMap<String, Text> flags_owner = new TreeMap<String, Text>();
		if (!flags.isEmpty()) {
			
			flags.forEach((flag, values) -> {
				Flag<T> key = (Flag<T>) flag;
				values.getAll().forEach((association, value) ->  {
					String value_string = key.serialize((T) value);
					Text message = EWMessages.REGION_INFO_ONE_FLAGS_LINE.getFormat()
							.toText("{flag}",  flag.getNameFormat().toBuilder()
													.onShiftClick(TextActions.insertText(flag.getId()))
													.build(),
									"{value}", key.getValueFormat((T) value).toBuilder()
													.onShiftClick(TextActions.insertText(value_string))
													.onClick(TextActions.suggestCommand(
						"/" + this.getParentName() + " removeflag -w \"" + world.getName() + "\" \"" + region.getName() + "\" \"" + flag.getName() + "\" \"" + association.getName() + "\""))
													.build());
					if (association.equals(Groups.DEFAULT)) {
						flags_default.put(flag.getId(), message);
					} else if (association.equals(Groups.MEMBER)) {
						flags_member.put(flag.getId(), message);
					} else if (association.equals(Groups.OWNER)) {
						flags_owner.put(flag.getId(), message);
					}
				});
			});
			
			this.addLine(list, EWMessages.REGION_INFO_ONE_FLAGS.getText());
			
			if (!flags_default.isEmpty()) {
				this.addLine(list, EWMessages.REGION_INFO_ONE_FLAGS_DEFAULT.getText());
				this.addLine(list, Text.joinWith(Text.of("\n"), flags_default.values()));
			}
			
			if (!flags_member.isEmpty()) {
				this.addLine(list, EWMessages.REGION_INFO_ONE_FLAGS_MEMBER.getText());
				this.addLine(list, Text.joinWith(Text.of("\n"), flags_member.values()));
			}
			
			if (!flags_owner.isEmpty()) {
				this.addLine(list, EWMessages.REGION_INFO_ONE_FLAGS_OWNER.getText());
				this.addLine(list, Text.joinWith(Text.of("\n"), flags_owner.values()));
			}
		}
		
		// Flags Heritage
		if (parents != null && !parents.isEmpty()) {
			TreeMap<String, Text> heritage_flags_default = new TreeMap<String, Text>();
			TreeMap<String, Text> heritage_flags_member = new TreeMap<String, Text>();
			TreeMap<String, Text> heritage_flags_owner = new TreeMap<String, Text>();
			
			for (ProtectedRegion curParent : parents) {
				Map<Flag<?>, FlagValue<?>> curFlags = curParent.getFlags();
				curFlags.forEach((flag, values) -> {
					Flag<T> key = (Flag<T>) flag;
					values.getAll().forEach((association, value) ->  {
						if (association.equals(Groups.DEFAULT)) {
							if (!flags_default.containsKey(key.getId()) && 
									!heritage_flags_default.containsKey(key.getId())) {
								heritage_flags_default.put(key.getId(), this.getTextHeritagFlagsLine(key, (T) value, association, curParent, world));
							}
						} else if (association.equals(Groups.MEMBER)) {
							if (!flags_member.containsKey(key.getId()) && 
									!heritage_flags_member.containsKey(key.getId())) {
								heritage_flags_member.put(key.getId(), this.getTextHeritagFlagsLine(key, (T) value, association, curParent, world));
							}
						} else if (association.equals(Groups.OWNER)) {
							if (!flags_owner.containsKey(key.getId()) && 
									!heritage_flags_owner.containsKey(key.getId())) {
								heritage_flags_owner.put(key.getId(), this.getTextHeritagFlagsLine(key, (T) value, association, curParent, world));
							}
						}
					});
				});
				
			}
			
			if (!heritage_flags_default.isEmpty() || !heritage_flags_member.isEmpty() || !heritage_flags_owner.isEmpty()) {
				this.addLine(list, EWMessages.REGION_INFO_ONE_HERITAGE_FLAGS.getText());
				
				if (!heritage_flags_default.isEmpty()) {
					this.addLine(list, EWMessages.REGION_INFO_ONE_HERITAGE_FLAGS_DEFAULT.getText());
					this.addLine(list, Text.joinWith(Text.of("\n"), heritage_flags_default.values()));
				}
				
				if (!heritage_flags_member.isEmpty()) {
					this.addLine(list, EWMessages.REGION_INFO_ONE_HERITAGE_FLAGS_MEMBER.getText());
					this.addLine(list, Text.joinWith(Text.of("\n"), heritage_flags_member.values()));
				}
				
				if (!heritage_flags_owner.isEmpty()) {
					this.addLine(list, EWMessages.REGION_INFO_ONE_HERITAGE_FLAGS_OWNER.getText());
					this.addLine(list, Text.joinWith(Text.of("\n"), heritage_flags_owner.values()));
				}
			}
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.REGION_INFO_ONE_TITLE.getFormat()
					.toText("{region}", region.getName())
					.toBuilder()
					.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\" \"" + region.getName() + "\" "))
					.build(), 
				list, player);
		
		return CompletableFuture.completedFuture(true);
	}
	
	private void addLine(TreeMap<ProtectedRegion, Text> map, ProtectedRegion region, Text line) {
		if (!line.isEmpty()) {
			map.put(region, line);
		}
	}
	
	private void addLine(List<Text> list, Text line) {
		if (!line.isEmpty()) {
			list.add(line);
		}
	}
	
	private <T> Text getTextHeritagFlagsLine(final Flag<T> flag, final T value, final Group association, final ProtectedRegion curParent, final World world) {
		String value_string = flag.serialize(value);
		return EWMessages.REGION_INFO_ONE_HERITAGE_FLAGS_LINE.getFormat()
				.toText("{flag}",  flag.getNameFormat().toBuilder()
										.onShiftClick(TextActions.insertText(flag.getId()))
										.build(),
						"{value}", flag.getValueFormat(value).toBuilder()
										.onShiftClick(TextActions.insertText(value_string))
										.onClick(TextActions.suggestCommand(
			"/" + this.getParentName() + " removeflag -w \"" + world.getName() + "\" \"" + curParent.getName() + "\" \"" + flag.getName() + "\" \"" + association.getName() + "\""))
										.build());
		
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_INFO_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_INFO_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_INFO_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerInteractEntity(InteractItemEvent.Secondary event, @First Player player) {
		Optional<ItemStack> optItemInHand = player.getItemInHand(event.getHandType());
		Optional<Vector3d> optPosition = event.getInteractionPoint();
		
		if (optItemInHand.isPresent() && optPosition.isPresent()) {
			ItemStack itemInHand = optItemInHand.get();

			if (itemInHand.getType().equals(this.plugin.getConfigs().getRegionInfo()) && player.hasPermission(EWPermissions.REGION_INFO_ITEM.get())) { 
				Vector3i position = optPosition.get().toInt();
				
				this.commandRegionInfo(player, this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld()).getRegions(position), player.getWorld());
				event.setCancelled(true);
			}
		}
	}
}
