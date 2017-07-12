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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.exception.FlagRegisterException;
import fr.evercraft.everworldguard.EverWorldGuard;


@SuppressWarnings("rawtypes")
public class FlagRegister implements AdditionalCatalogRegistryModule<Flag<?>> {
	
	private final ConcurrentMap<String, Flag<?>> flags;
	private boolean initialized;
	
	// MultiThreading
	private final ReadWriteLock lock;
	private final Lock write_lock;
	private final Lock read_lock;
	
	@SuppressWarnings("unchecked")
	public FlagRegister(final EverWorldGuard plugin) {
		this.flags = new ConcurrentHashMap<String, Flag<?>>();
		this.initialized = false;
		
		// MultiThreading
		this.lock = new ReentrantReadWriteLock();
		this.write_lock = this.lock.writeLock();
		this.read_lock = this.lock.readLock();
		
		plugin.getGame().getRegistry().registerModule(Flag.class, (AdditionalCatalogRegistryModule) this);
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	public void registerAdditionalCatalog(final Flag flag) throws FlagRegisterException {
		Preconditions.checkNotNull(flag, "flag");
		
		this.write_lock.lock();
		try {
			if (this.initialized) {
				throw new FlagRegisterException(FlagRegisterException.Type.INITIALIZED, flag);
			}
			
			if (this.flags.containsKey(flag.getId().toLowerCase())) {
				throw new FlagRegisterException(FlagRegisterException.Type.CONFLICT, flag);
			}
			this.flags.put(flag.getId().toLowerCase(), flag);
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public void registerAdditionalCatalog(final Set<Flag<?>> flags) throws FlagRegisterException {
		Preconditions.checkNotNull(flags, "flags");
		
		this.write_lock.lock();
		try {
			for (Flag<?> flag : flags) {
				this.registerAdditionalCatalog(flag);
			}
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public Optional<Flag<?>> getById(final String identifier) {
		Preconditions.checkNotNull(identifier, "identifier");
		Flag<?> flag;
		
		this.read_lock.lock();
		try {
			flag = this.flags.get(identifier.toLowerCase());
		} finally {
			this.read_lock.unlock();
		}
		
		return Optional.ofNullable(flag);
	}

	public <T> boolean hasRegistered(final Flag<T> flag) {
		Preconditions.checkNotNull(flag, "flag");
		
		boolean registered = false;
		
		this.read_lock.lock();
		try {
			registered = this.flags.containsValue(flag);
		} finally {
			this.read_lock.unlock();
		}
		
		return registered;
	}
	
	public Set<Flag<?>> getAll() {
		Builder<Flag<?>> builder = ImmutableSet.builder();
		
		this.read_lock.lock();
		try {
			builder.addAll(this.flags.values());
		} finally {
			this.read_lock.unlock();
		}
		
		return builder.build();
	}
}
