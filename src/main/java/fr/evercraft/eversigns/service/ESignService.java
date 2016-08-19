/*
 * This file is part of EverSigns.
 *
 * EverSigns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverSigns is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverSigns.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.eversigns.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fr.evercraft.everapi.services.signs.SignService;
import fr.evercraft.everapi.services.signs.SignSubject;
import fr.evercraft.eversigns.EverSigns;

public class ESignService implements SignService {

	@SuppressWarnings("unused")
	private final EverSigns plugin;
	
	private final ConcurrentMap<String, ESign> signs;
	
	public ESignService(final EverSigns plugin) {
		this.plugin = plugin;
		
		this.signs = new ConcurrentHashMap<String, ESign>();
	}
	
	@Override
	public boolean add(SignSubject subject) {
		if(!this.signs.containsKey(subject.getName())) {
			ESign sign = new ESign(subject);
			this.signs.put(sign.getTitle().toLowerCase(), sign);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(SignSubject sign) {
		String title = "[" + sign.getName() + "]";
		return this.signs.remove(title.toLowerCase()) != null;
	}

	@Override
	public Collection<SignSubject> getAll() {
		Collection<SignSubject> signs = new ArrayList<SignSubject>();
		for(ESign sign : this.signs.values()) {
			signs.add(sign.getSubject());
		}
		return signs;
	}
	
	public Optional<ESign> get(String title) {
		return Optional.ofNullable(this.signs.get(title.toLowerCase()));
	}

}
