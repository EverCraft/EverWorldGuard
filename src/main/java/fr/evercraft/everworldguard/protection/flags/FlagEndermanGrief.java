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
package fr.evercraft.everworldguard.protection.flags;

import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.entity.living.monster.Enderman;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class FlagEndermanGrief extends StateFlag {
		
	private final EverWorldGuard plugin;

	public FlagEndermanGrief(EverWorldGuard plugin) {
		super("ENDERMAN_GRIEF");
		
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENDERMAN_GRIEF_DESCRIPTION.getString();
	}
	
	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Override
	public Set<Group> getGroups() {
		return ImmutableSet.of(Group.DEFAULT);
	}
	
	/*
	 * ChangeBlockEvent.Break
	 */
	
	public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<Enderman> enderman = event.getCause().get(NamedCause.SOURCE, Enderman.class);
		if (!enderman.isPresent()) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		if (!event.filter(location -> 
			service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.ALLOW)).isEmpty()) {
			event.setCancelled(true);
			// TODO L'enderman garde l'item dans la main mais l'inventaire n'est pas encore implémenté
			//enderman.get().getInventory().clear();
		}
	}
	
	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;
		
		Optional<Enderman> enderman = event.getCause().get(NamedCause.SOURCE, Enderman.class);
		if (!enderman.isPresent()) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		if (!event.filter(location -> 
			service.getOrCreateWorld(location.getExtent()).getRegions(location.getPosition()).getFlagDefault(this).equals(State.ALLOW)).isEmpty()) {
			// TODO 
		}
	}
}
