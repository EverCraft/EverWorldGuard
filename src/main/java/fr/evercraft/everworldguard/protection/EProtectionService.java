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
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.message.EMessageSender;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.WorldGuardSubject;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.subject.EUserSubject;

public class EProtectionService implements WorldGuardService {
	
	private final EverWorldGuard plugin;
	
	private final EUserSubjectList subjects;
	private final EWorldList worlds;
	private final FlagRegister flagsRegister;
	
	private int intervalMessage;
	
	
	public EProtectionService(final EverWorldGuard plugin) {		
		this.plugin = plugin;
		
		this.subjects = new EUserSubjectList(this.plugin);
		this.worlds = new EWorldList(this.plugin);
		this.flagsRegister = new FlagRegister(this.plugin);
		
		this.reload();
	}
	
	public void reload() {		
		this.subjects.reload();
		this.worlds.reload();
		
		this.intervalMessage = this.plugin.getConfigs().getMessageInterval();
	}
	
	/*
	 * Messages
	 */
	
	public int getIntervalMessage() {
		return this.intervalMessage;
	}
	
	public boolean sendMessage(final Player player, final Flag<?> flag, final EMessageSender message) {
		return this.sendMessage(this.plugin.getEServer().getEPlayer(player), flag, message);
	}
	
	public boolean sendMessage(final EPlayer player, final Flag<?> flag, final EMessageSender message) {
		Optional<EUserSubject> subject = this.getSubject(player.getUniqueId());
		if (subject.isPresent()) {
			subject.get().sendMessage(player, flag, message);
			return true;
		}
		return false;
	}
	
	/*
	 * Bypass
	 */
	
	public boolean hasBypass(final User player) {
		return this.hasBypass(player.getUniqueId());
	}
	
	public boolean hasBypass(final UUID uuid) {
		Optional<WorldGuardSubject> subject = this.get(uuid);
		if (subject.isPresent()) {
			return subject.get().hasBypass();
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
	public Optional<WorldGuardSubject> get(final UUID uuid) {
		return this.subjects.get(uuid);
	}
	
	public Optional<EUserSubject> getSubject(final UUID uuid) {
		return this.subjects.getSubject(uuid);
	}
	
	@Override
	public boolean hasRegistered(final UUID uuid) {
		return this.subjects.hasRegistered(uuid);
	}
	
	/*
	 * World
	 */
	@Override
	public CompletableFuture<WorldGuardWorld> getOrCreateWorld(final World world) {
		return this.worlds.getOrCreate(world);
	}
	
	public EWWorld getOrCreateEWorld(final World world) {
		return this.worlds.get(world);
	}
	
	public void unLoadWorld(final World world) {
		this.worlds.unLoad(world);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set<WorldGuardWorld> getAll() {
		return (Set) this.worlds.getAll();
	}
	
	public Set<EWWorld> getAllEWorld() {
		return this.worlds.getAll();
	}
	
	/*
	 * Flags
	 */
	
	@Override
	public Optional<Flag<?>> getFlag(final String name) {
		return this.flagsRegister.getById(name);
	}

	@Override
	public void registerFlag(final Flag<?> flag) {
		this.flagsRegister.registerAdditionalCatalog(flag);
	}
	
	@Override
	public void registerFlag(final Set<Flag<?>> flags) {
		this.flagsRegister.registerAdditionalCatalog(flags);
	}

	@Override
	public boolean hasRegisteredFlag(final Flag<?> flag) {
		return this.flagsRegister.hasRegistered(flag);
	}
	
	public Set<Flag<?>> getFlags() {
		return this.flagsRegister.getAll();
	}
	
	public FlagRegister getRegister() {
		return this.flagsRegister;
	}
}
