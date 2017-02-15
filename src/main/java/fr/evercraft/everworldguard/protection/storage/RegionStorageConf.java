package fr.evercraft.everworldguard.protection.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.protection.flag.EFlagValue;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedGlobalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedTemplateRegion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RegionStorageConf extends EConfig<EverWorldGuard> implements RegionStorage {
	
	private static final String DIR = "worlds";
	
	private final EverWorldGuard plugin;
	private final EWWorld world;
	
	public RegionStorageConf(EverWorldGuard plugin, EWWorld world) {
		super(plugin, DIR + "/" + world.getWorld().getName() + "/regions", false);
		Preconditions.checkNotNull(world);

		this.plugin = plugin;
		this.world = world;
        this.loadGlobal();
	}
	
	@Override
	protected void loadDefault() {}

    protected void loadGlobal() {
		try {
			EProtectedRegion global = new EProtectedGlobalRegion(this.world, EProtectionService.GLOBAL_REGION);
			if (this.getNode().getNode(global.getIdentifier()).isVirtual()) {
				this.add(global);
			}
		} catch (RegionIdentifierException e) {}
	}

	@Override
	public Set<EProtectedRegion> getAll() {
		Map<String, EProtectedRegion> regions = new HashMap<String, EProtectedRegion>();
		for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
			if (config.getKey() instanceof String) {
				this.get((String) config.getKey(), config.getValue())
					.ifPresent(region -> regions.put(region.getIdentifier().toLowerCase(), region));
			} else {
				this.plugin.getLogger().warn("Nom de la région incorrect : " + config.getKey().toString());
			}
		}
		
		// Parents
		for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
			if (config.getKey() instanceof String) {
				String key = (String) config.getKey();
				String value = config.getValue().getNode("parent").getString(null);
				EProtectedRegion region = regions.get(key.toLowerCase());
				if (region != null && value != null && !value.isEmpty()) {
					EProtectedRegion parent = regions.get(value.toLowerCase());
					if (parent != null) {
						region.init(parent);
					} else {
						this.plugin.getLogger().warn("Parent incorrect : " + value);
					}
				}
			}
		}
		
		return ImmutableSet.copyOf(regions.values());
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<EProtectedRegion> get(String identifier, ConfigurationNode config) {
		// Type
		String type_string = config.getNode("type").getString("");
		Optional<ProtectedRegion.Type> optType = ProtectedRegion.Type.of(type_string);
		if (!optType.isPresent()) {
			this.plugin.getLogger().warn("Type incorrect : " + type_string + " (id:'" + identifier + "')");
			return Optional.empty();
		}
		ProtectedRegion.Type type = optType.get();
		
		int priority = config.getNode("priority").getInt(0);
		
		// Owners
		Set<UUID> owners = null;
		try {
			owners = ImmutableSet.copyOf(config.getNode("owners").getList(TypeToken.of(UUID.class)));
		} catch (ObjectMappingException e) {
			owners = ImmutableSet.of();
			this.plugin.getLogger().warn("Owners incorrect (id:'" + identifier + "') : " + e.getMessage());
		}
		
		Set<String> group_owners = null;
		try {
			group_owners = ImmutableSet.copyOf(config.getNode("group-owners").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_owners = ImmutableSet.of();
			this.plugin.getLogger().warn("GroupOwners incorrect (id:'" + identifier + "') : " + e.getMessage());
		}
		
		// Members
		Set<UUID> members = null;
		try {
			members = ImmutableSet.copyOf(config.getNode("members").getList(TypeToken.of(UUID.class)));
		} catch (ObjectMappingException e) {
			members = ImmutableSet.of();
			this.plugin.getLogger().warn("Members incorrect (id:'" + identifier + "') : " + e.getMessage());
		}
		
		Set<String> group_members = null;
		try {
			group_members = ImmutableSet.copyOf(config.getNode("group-members").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_members = ImmutableSet.of();
			this.plugin.getLogger().warn("GroupMembers incorrect (id:'" + identifier + "') : " + e.getMessage());
		}
		
		// Flags
		Map<Flag<?>, EFlagValue<?>> flags = new HashMap<Flag<?>, EFlagValue<?>>();
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-default").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Group.DEFAULT, value));
				} else {
					this.plugin.getLogger().warn("FlagDefault no register : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagDefault incorrect : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-member").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Group.MEMBER, value));
				} else {
					this.plugin.getLogger().warn("FlagMember no register : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagMember incorrect : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-owner").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Group.OWNER, value));
				} else {
					this.plugin.getLogger().warn("FlagOwner no register : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagOwner incorrect : " + config_flags.getKey().toString() + " (id:'" + identifier + "')");
			}
		}
		
		EProtectedRegion region = null;
		try {
			if (type.equals(ProtectedRegion.Type.GLOBAL)) {
				region = new EProtectedGlobalRegion(this.world, identifier);
			} else if (type.equals(ProtectedRegion.Type.TEMPLATE)) {
				region = new EProtectedTemplateRegion(this.world, identifier);
			} else if (type.equals(ProtectedRegion.Type.CUBOID)) {
				Vector3i min = null;
				Vector3i max = null;
				
				try {
					min = config.getNode("min").getValue(TypeToken.of(Vector3i.class));
				} catch (ObjectMappingException e1) {}
				if (min == null) {
					this.plugin.getLogger().warn("Min incorrect (id:'" + identifier + "')");
					return Optional.empty();
				}
				
				
				try {
					max = config.getNode("max").getValue(TypeToken.of(Vector3i.class));
				} catch (ObjectMappingException e) {}
				if (max == null) {
					this.plugin.getLogger().warn("Max incorrect (id:'" + identifier + "')");
					return Optional.empty();
				}

				region = new EProtectedCuboidRegion(this.world, identifier, min, max);
			} else if (type.equals(ProtectedRegion.Type.POLYGONAL)) {
				List<Vector3i> vectors = null;
				try {
					vectors = config.getNode("positions").getList(TypeToken.of(Vector3i.class));
				} catch (ObjectMappingException e) {}
                if (vectors == null) {
                    this.plugin.getLogger().warn("Positions incorrect (id:'" + identifier + "')");
                    return Optional.empty();
                }

				region = new EProtectedPolygonalRegion(this.world, identifier, vectors);
			} else {
				return Optional.empty();
			}
		
			region.init(priority, owners, group_owners, members, group_members, flags);
			return Optional.of(region);
			
		} catch (RegionIdentifierException e) {
			this.plugin.getLogger().warn("Identifier invalid (id:'" + identifier + "')");
			return Optional.empty();
		}
	}
	
	public <T> EFlagValue<T> putFlags(EFlagValue<T> values, Group association, T value) {
		if (values == null) {
			values = new EFlagValue<T>();
		}
		values.set(association, value);
		return values;
	}
	
	@Override
	public void add(EProtectedRegion region) {
		this.add(region, this.getNode().getNode(region.getIdentifier()));
		this.save(true);
	}

	@SuppressWarnings("unchecked")
	public <T> void add(EProtectedRegion region, ConfigurationNode config) {		
		// Type
		config.getNode("type").setValue(region.getType().name());
		config.getNode("priority").setValue(region.getPriority());
		
		// Owners
		config.getNode("owners").setValue(region.getOwners().getPlayers());
		if (!region.getOwners().getGroups().isEmpty()) {
			config.getNode("group-owners").setValue(region.getOwners().getGroups());
		}
		
		// Members
		config.getNode("members").setValue(region.getMembers().getPlayers());
		if (!region.getMembers().getGroups().isEmpty()) {
			config.getNode("group-members").setValue(region.getMembers().getGroups());
		}
		
		// Parent
		region.getParent().ifPresent(parent -> config.getNode("parent").setValue(parent));
		
		// Flags
		Map<String, String> flags_owner = new HashMap<String, String>();
		Map<String, String> flags_member = new HashMap<String, String>();
		Map<String, String> flags_default = new HashMap<String, String>();
		
		for (Entry<Flag<?>, FlagValue<?>> flag : region.getFlags().entrySet()) {
			Flag<T> key = (Flag<T>) flag.getKey();
			for (Entry<Group, ?> value : flag.getValue().getAll().entrySet()) {
				T val = (T) value.getValue();
				if (value.getKey().equals(Group.DEFAULT)) {
					flags_default.put(key.getIdentifier(), key.serialize(val));
				} else if (value.getKey().equals(Group.MEMBER)) {
					flags_member.put(key.getIdentifier(), key.serialize(val));
				} else if (value.getKey().equals(Group.OWNER)) {
					flags_default.put(key.getIdentifier(), key.serialize(val));
				}
			}
		}
		
		if (!flags_owner.isEmpty()) {
			config.getNode("flags-owner").setValue(flags_owner);
		}
		if (!flags_member.isEmpty()) {
			config.getNode("flags-member").setValue(flags_member);
		}
		if (!flags_default.isEmpty()) {
			config.getNode("flags-default").setValue(flags_default);
		}
		
		// Points
		if (region.getType().equals(ProtectedRegion.Type.CUBOID)) {
			try {
				config.getNode("min").setValue(TypeToken.of(Vector3i.class), region.getMinimumPoint());
				config.getNode("max").setValue(TypeToken.of(Vector3i.class), region.getMaximumPoint());
			} catch (ObjectMappingException e) {}
		} else if (region.getType().equals(ProtectedRegion.Type.POLYGONAL)) {
			config.getNode("positions").setValue(region.getPoints().stream()
					.map(vector -> Arrays.asList(vector.getX(), vector.getY(), vector.getZ()))
					.collect(Collectors.toList()));
		}
	}

	@Override
	public void remove(EProtectedRegion region) {
		this.getNode().removeChild(region.getIdentifier());
		this.save(true);
	}
	
	@Override
	public void remove(Set<EProtectedRegion> regions) {
		regions.stream().forEach(region -> this.getNode().removeChild(region.getIdentifier()));
		this.save(true);
	}

	@Override
	public void setIdentifier(EProtectedRegion region, String identifier) {
		this.getNode().removeChild(region.getIdentifier());
		this.add(region, this.getNode().getNode(identifier));
		this.save(true);
	}

	@Override
	public void setPriority(EProtectedRegion region, int priority) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		config.getNode("priority").setValue(priority);
		this.save(true);
	}

	@Override
	public void setParent(EProtectedRegion region, ProtectedRegion parent) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
        if (parent  == null) {
            config.removeChild("parent");
        } else {
            config.getNode("parent").setValue(parent.getIdentifier());
        }
		this.save(true);
	}

	@Override
	public <V> void setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		if (group.equals(Group.OWNER)) {
			config = config.getNode("flags-owner");
		} else if (group.equals(Group.MEMBER)) {
			config = config.getNode("flags-member");
		} else if (group.equals(Group.DEFAULT)) {
			config = config.getNode("flags-default");
		}
		
		if (value == null) {
			config.removeChild(flag.getIdentifier());
		} else {
			config.getNode(flag.getIdentifier()).setValue(flag.serialize(value));
		}
		this.save(true);
	}

	@Override
	public void addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.addAll(players);
		
		config.getNode("owners").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		this.save(true);
	}

	@Override
	public void addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.addAll(groups);
		
		config.getNode("group-owners").setValue(all);
		this.save(true);
	}

	@Override
	public void addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.addAll(players);
		
		config.getNode("members").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		this.save(true);
	}

	@Override
	public void addMemberGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.addAll(groups);
		
		config.getNode("group-members").setValue(all);
		this.save(true);
	}

	@Override
	public void removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.removeAll(players);
		
		config.getNode("owners").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		this.save(true);
	}

	@Override
	public void removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.removeAll(groups);
		
		config.getNode("group-owners").setValue(all);
		this.save(true);
	}

	@Override
	public void removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.removeAll(players);
		
		config.getNode("members").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		this.save(true);
	}

	@Override
	public void removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.removeAll(groups);
		
		config.getNode("group-members").setValue(all);
		this.save(true);
	}
}
