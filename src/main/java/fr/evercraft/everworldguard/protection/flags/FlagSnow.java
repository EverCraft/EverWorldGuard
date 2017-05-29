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

import fr.evercraft.everapi.registers.SnowType;
import fr.evercraft.everapi.registers.SnowType.SnowTypes;
import fr.evercraft.everapi.services.worldguard.flag.type.CatalogTypeFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagSnow extends CatalogTypeFlag<SnowType> {

	private final EverWorldGuard plugin;
	
	public FlagSnow(EverWorldGuard plugin) {
		super("SNOW");
		
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<SnowType>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().get(this.getName(), SnowType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_SNOW_DESCRIPTION.getString();
	}
	
	@Override
	public Set<Group> getGroups() {
		return ImmutableSet.of(Group.DEFAULT);
	}

	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		this.onChangeBlock(event);
	}
	
	/*
	 * ChangeBlockEvent.Modify
	 */
	
	public void onChangeBlockModify(ChangeBlockEvent.Modify event) {
		this.onChangeBlock(event);
	}
	
	// ChangeBlockEvent.Place et ChangeBlockEvent.Modify
	public void onChangeBlock(ChangeBlockEvent event) {
		if (event.isCancelled()) return;
		
		Optional<World> optWorld = event.getCause().get(NamedCause.SOURCE, World.class);
		if (!optWorld.isPresent()) return;
		
		if (!this.defaults.containsValue(SnowTypes.FALL)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		event.getTransactions().stream()
			.forEach(transaction -> {
				if (!transaction.isValid()) return;
				
				BlockType type = transaction.getFinal().getState().getType();
				if (!type.equals(BlockTypes.SNOW_LAYER)) return;
				
				Optional<Location<World>> location = transaction.getFinal().getLocation();
				if (!location.isPresent()) return;

				if(!service.getOrCreateWorld(location.get().getExtent()).getRegions(location.get().getPosition()).getFlagDefault(this).containsValue(SnowTypes.FALL)) {
					transaction.setValid(false);
				}
			});
	}
	
	/*
	 * ChangeBlockEvent.Break
	 */
	
	public void onChangeBlockBreak(ChangeBlockEvent.Break event) {
		if (event.isCancelled()) return;
		
		Optional<LocatableBlock> optBlock = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (!optBlock.isPresent()) return;
		if (!optBlock.get().getBlockState().getType().equals(BlockTypes.SNOW_LAYER)) return;
		
		if (!this.defaults.containsValue(SnowTypes.MELT)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		event.getTransactions().stream()
			.forEach(transaction -> {
				if (!transaction.isValid()) return;
				
				BlockType type = transaction.getOriginal().getState().getType();
				if (!type.equals(BlockTypes.SNOW_LAYER)) return;
				
				Optional<Location<World>> location = transaction.getOriginal().getLocation();
				if (!location.isPresent()) return;

				if(!service.getOrCreateWorld(location.get().getExtent()).getRegions(location.get().getPosition()).getFlagDefault(this).containsValue(SnowTypes.MELT)) {
					transaction.setValid(false);
				}
			});
	}
}
