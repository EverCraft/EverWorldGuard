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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.registers.IceType;
import fr.evercraft.everapi.registers.IceType.IceTypes;
import fr.evercraft.everapi.services.worldguard.flag.type.CatalogTypeFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagIce extends CatalogTypeFlag<IceType> {

	private final EverWorldGuard plugin;
	
	public FlagIce(EverWorldGuard plugin) {
		super("ICE");
		
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<IceType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), IceType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ICE_DESCRIPTION.getString();
	}
	
	@Override
	public Set<Group> getGroups() {
		return ImmutableSet.of(Group.DEFAULT);
	}

	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;
		
		this.onChangeBlockPlaceForm(event);
		this.onChangeBlockPlaceMelt(event);
	}
	
	public void onChangeBlockPlaceForm(ChangeBlockEvent.Place event) {
		Optional<World> optWorld = event.getCause().get(NamedCause.SOURCE, World.class);
		if (!optWorld.isPresent()) return;
		
		if (!this.defaults.containsValue(IceTypes.FORM)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		event.getTransactions().stream()
			.forEach(transaction -> {
				if (!transaction.isValid()) return;
				
				BlockType typeOriginal = transaction.getOriginal().getState().getType();
				if (!typeOriginal.equals(BlockTypes.WATER)) return;
				
				BlockType typeFinal = transaction.getFinal().getState().getType();
				if (!typeFinal.equals(BlockTypes.ICE)) return;
				
				Optional<Location<World>> location = transaction.getFinal().getLocation();
				if (!location.isPresent()) return;

				if(!service.getOrCreateWorld(location.get().getExtent()).getRegions(location.get().getPosition()).getFlagDefault(this).containsValue(IceTypes.FORM)) {
					transaction.setValid(false);
				}
			});
	}
	
	public void onChangeBlockPlaceMelt(ChangeBlockEvent.Place event) {
		Optional<LocatableBlock> optBlock = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (!optBlock.isPresent()) return;
		if (!optBlock.get().getBlockState().getType().equals(BlockTypes.ICE)) return;
		
		if (!this.defaults.containsValue(IceTypes.MELT)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		event.getTransactions().stream()
			.forEach(transaction -> {
				if (!transaction.isValid()) return;
				
				BlockType typeOriginal = transaction.getOriginal().getState().getType();
				if (!typeOriginal.equals(BlockTypes.ICE)) return;
				
				BlockType typeFinal = transaction.getFinal().getState().getType();
				if (!typeFinal.equals(BlockTypes.FLOWING_WATER)) return;
				
				Optional<Location<World>> location = transaction.getFinal().getLocation();
				if (!location.isPresent()) return;

				if(!service.getOrCreateWorld(location.get().getExtent()).getRegions(location.get().getPosition()).getFlagDefault(this).containsValue(IceTypes.MELT)) {
					transaction.setValid(false);
				}
			});
	}
}
