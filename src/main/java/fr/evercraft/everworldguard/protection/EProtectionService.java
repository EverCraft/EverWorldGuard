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
package fr.evercraft.everworldguard.protection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.message.EMessageSender;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.flag.EWFlagConfig;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.subject.EUserSubject;

public class EProtectionService implements WorldGuardService {
	
	public static final String GLOBAL_REGION = "__global__";
	
	private final EverWorldGuard plugin;
	
	private final EUserSubjectList subjects;
	private final EWorldList worlds;
	private final FlagRegister flagsRegister;
	private final EWFlagConfig flagsConfig;
	
	private int intervalMessage;
	
	
	public EProtectionService(final EverWorldGuard plugin) {		
		this.plugin = plugin;
		
		this.subjects = new EUserSubjectList(this.plugin);
		this.worlds = new EWorldList(this.plugin);
		this.flagsRegister = new FlagRegister();
		this.flagsConfig = new EWFlagConfig(this.plugin);
		
		this.reload();
	}
	
	public void reload() {		
		this.subjects.reload();
		this.worlds.reload();
		
		this.intervalMessage = this.plugin.getConfigs().getIntervalMessage();
	}
	
	/*
	 * Messages
	 */
	
	public int getIntervalMessage() {
		return this.intervalMessage;
	}
	
	public boolean sendMessage(Player player, Flag<?> flag, EMessageSender message) {
		return this.sendMessage(this.plugin.getEServer().getEPlayer(player), flag, message);
	}
	
	public boolean sendMessage(EPlayer player, Flag<?> flag, EMessageSender message) {
		Optional<EUserSubject> subject = this.getSubject(player.getUniqueId());
		if (subject.isPresent()) {
			subject.get().sendMessage(player, flag, message);
			return true;
		}
		return false;
	}
	
	/*
	 * Subjects
	 */
	
	public EUserSubjectList getSubjectList() {
		return this.subjects;
	}

	@Override
	public Optional<SubjectWorldGuard> get(UUID uuid) {
		return this.subjects.get(uuid);
	}
	
	public Optional<EUserSubject> getSubject(UUID uuid) {
		return this.subjects.getSubject(uuid);
	}
	
	@Override
	public boolean hasRegistered(UUID uuid) {
		return this.subjects.hasRegistered(uuid);
	}
	
	/*
	 * World
	 */
	@Override
	public WorldWorldGuard getOrCreateWorld(World world) {
		return this.worlds.getOrCreate(world);
	}
	
	public EWWorld getOrCreateEWorld(World world) {
		return this.worlds.getOrCreate(world);
	}
	
	@Override
	public void unLoadWorld(World world) {
		this.worlds.unLoad(world);
	}
	
	@Override
	public Set<WorldWorldGuard> getAll() {
		return this.worlds.getAll();
	}
	
	/*
	 * Flags
	 */

	public boolean hasPermissionFlag(Subject subject, Flag<?> flag) {
		return subject.hasPermission(EWPermissions.FLAGS.get() + "." + flag.getIdentifier());
	}
	
	@Override
	public Optional<Flag<?>> getFlag(String name) {
		return this.flagsRegister.get(name);
	}

	@Override
	public void registerFlag(Flag<?> flag) {
		this.flagsRegister.register(flag);
	}
	
	@Override
	public void registerFlag(Set<Flag<?>> flags) {
		this.flagsRegister.register(flags);
	}

	@Override
	public boolean hasRegisteredFlag(Flag<?> flag) {
		return this.flagsRegister.hasRegistered(flag);
	}
	
	public Set<Flag<?>> getFlags() {
		return this.flagsRegister.getAll();
	}

	@Override
	public void clearFlags() {
		// TODO Supprimer les flags non utilisé dans la config
	}
	
	public FlagRegister getRegister() {
		return this.flagsRegister;
	}

	public EWFlagConfig getConfigFlags() {
		return this.flagsConfig;
	}
}
