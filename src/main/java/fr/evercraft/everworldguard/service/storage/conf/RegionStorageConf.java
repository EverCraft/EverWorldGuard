package fr.evercraft.everworldguard.service.storage.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.regions.EProtectedGlobalRegion;
import fr.evercraft.everworldguard.regions.EProtectedRegion;
import fr.evercraft.everworldguard.regions.EProtectedTemplateRegion;
import fr.evercraft.everworldguard.service.EWorldGuardService;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RegionStorageConf extends EConfig<EverWorldGuard> implements RegionStorage {
	
	private static final String DIR = "worlds";
	
	private final EverWorldGuard plugin;
	
	public RegionStorageConf(EverWorldGuard plugin, World world) {
		super(plugin, DIR + "/" + world.getName() + "/regions", false);	
		
		this.plugin = plugin;
	}
	
	@Override
	protected void loadDefault() {
		EProtectedRegion global = new EProtectedGlobalRegion(EWorldGuardService.GLOBAL_REGION);
		if (this.getNode().getNode(global.getIdentifier()).isVirtual()) {
			this.add(global);
		}
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
						this.plugin.getLogger().warn("Parent int : " + parent.getIdentifier());
					} else {
						this.plugin.getLogger().warn("Parent incorrect : " + value);
					}
				}
			}
		}
		
		return ImmutableSet.copyOf(regions.values());
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<EProtectedRegion> get(String id, ConfigurationNode config) {
		// Type
		String type_string = config.getNode("type").getString("");
		Optional<ProtectedRegion.Type> optType = ProtectedRegion.Type.of(type_string);
		if (!optType.isPresent()) {
			this.plugin.getLogger().warn("Type incorrect : " + type_string + " (id:'" + id + "')");
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
			this.plugin.getLogger().warn("Owners incorrect (id:'" + id + "') : " + e.getMessage());
		}
		
		Set<String> group_owners = null;
		try {
			group_owners = ImmutableSet.copyOf(config.getNode("group-owners").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_owners = ImmutableSet.of();
			this.plugin.getLogger().warn("GroupOwners incorrect (id:'" + id + "') : " + e.getMessage());
		}
		
		// Members
		Set<UUID> members = null;
		try {
			members = ImmutableSet.copyOf(config.getNode("members").getList(TypeToken.of(UUID.class)));
		} catch (ObjectMappingException e) {
			members = ImmutableSet.of();
			this.plugin.getLogger().warn("Members incorrect (id:'" + id + "') : " + e.getMessage());
		}
		
		Set<String> group_members = null;
		try {
			group_members = ImmutableSet.copyOf(config.getNode("group-members").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_members = ImmutableSet.of();
			this.plugin.getLogger().warn("GroupMembers incorrect (id:'" + id + "') : " + e.getMessage());
		}
		
		// Flags
		Map<Flag<?>, FlagValue<?>> flags = new HashMap<Flag<?>, FlagValue<?>>();
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-default").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((FlagValue<T>) flags.get(flag), Group.DEFAULT, value));
				} else {
					this.plugin.getLogger().warn("FlagDefault no register : " + config_flags.getKey().toString() + " (id:'" + id + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagDefault incorrect : " + config_flags.getKey().toString() + " (id:'" + id + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-member").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((FlagValue<T>) flags.get(flag), Group.MEMBER, value));
				} else {
					this.plugin.getLogger().warn("FlagMember no register : " + config_flags.getKey().toString() + " (id:'" + id + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagMember incorrect : " + config_flags.getKey().toString() + " (id:'" + id + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-owner").getChildrenMap().entrySet()) {
			if (config_flags.getKey() instanceof String) {
				Optional<Flag<?>> optFlag = this.plugin.getService().getFlag((String) config_flags.getKey());
				if (optFlag.isPresent()) {
					Flag<T> flag = (Flag<T>) optFlag.get();
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((FlagValue<T>) flags.get(flag), Group.OWNER, value));
				} else {
					this.plugin.getLogger().warn("FlagOwner no register : " + config_flags.getKey().toString() + " (id:'" + id + "')");
				}
			} else {
				this.plugin.getLogger().warn("FlagOwner incorrect : " + config_flags.getKey().toString() + " (id:'" + id + "')");
			}
		}
		
		EProtectedRegion region = null;
		if (type.equals(ProtectedRegion.Type.GLOBAL)) {
			region = new EProtectedGlobalRegion(id);
		} else if (type.equals(ProtectedRegion.Type.CUBOID)) {
			Vector3i min, max;
			try {
				min = config.getNode("min").getValue(TypeToken.of(Vector3i.class));
			} catch (ObjectMappingException e1) {
				this.plugin.getLogger().warn("Min incorrect (id:'" + id + "')");
				return Optional.empty();
			}
			try {
				max = config.getNode("max").getValue(TypeToken.of(Vector3i.class));
			} catch (ObjectMappingException e) {
				this.plugin.getLogger().warn("Max incorrect (id:'" + id + "')");
				return Optional.empty();
			}
			region = new EProtectedCuboidRegion(id, min, max);
		} else if (type.equals(ProtectedRegion.Type.TEMPLATE)) {
			region = new EProtectedTemplateRegion(id);
		} else if (type.equals(ProtectedRegion.Type.POLYGONAL)) {
			region = new EProtectedGlobalRegion(id);
		} else {
			return Optional.empty();
		}
		
		region.init(priority, owners, group_owners, members, group_members, flags);
		
		return Optional.of(region);
	}
	
	public <T> FlagValue<T> putFlags(FlagValue<T> values, Group association, T value) {
		if (values == null) {
			values = new FlagValue<T>();
		}
		values.set(association, value);
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void add(EProtectedRegion region) {
		ConfigurationNode config = this.getNode().getNode(region.getIdentifier());
		
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
					
				} else if (value.getKey().equals(Group.OWNER)) {
					
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
			config.getNode("min").setValue(region.getMinimumPoint());
			config.getNode("max").setValue(region.getMaximumPoint());
		} else if (region.getType().equals(ProtectedRegion.Type.POLYGONAL)) {
			
		}
	}

	@Override
	public void remove(EProtectedRegion region) throws StorageException {
		this.getNode().removeChild(region.getIdentifier());
	}
	
	@Override
	public void remove(Set<EProtectedRegion> regions) throws StorageException {
		regions.stream().forEach(region -> this.getNode().removeChild(region.getIdentifier()));
	}
}
