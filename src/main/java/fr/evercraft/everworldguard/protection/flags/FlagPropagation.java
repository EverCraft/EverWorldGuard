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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.flag.CatalogTypeFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagPropagation extends CatalogTypeFlag<BlockType> {

	private final EverWorldGuard plugin;
	
	public FlagPropagation(EverWorldGuard plugin) {
		super("PROPAGATION");
		
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<BlockType>> getConfig() {
		return this.plugin.getConfigFlags().get(this.getName(), BlockType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_PROPAGATION_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_PROPAGATION_MESSAGE.sender()
					.replace("{x}", position.getX())
					.replace("{y}", position.getY())
					.replace("{z}", position.getZ()));
	}
	
	@Override
	public Set<Group> getGroups() {
		return ImmutableSet.of(Groups.DEFAULT);
	}
	
	/*
	 * ChangeBlockEvent.Place
	 */
	
	public void onChangeBlockPlace(ChangeBlockEvent.Place event) {
		if (event.isCancelled()) return;
		
		this.onChangeBlock(event);
	}
	
	/*
	 * ChangeBlockEvent.Modify
	 */
	
	public void onChangeBlockModify(ChangeBlockEvent.Modify event) {
		if (event.isCancelled()) return;
		
		this.onChangeBlock(event);
	}

	/*
	 * ChangeBlockEvent
	 */
	
	public void onChangeBlock(ChangeBlockEvent event) {
		Optional<LocatableBlock> optBlock = event.getCause().get(NamedCause.SOURCE, LocatableBlock.class);
		if (!optBlock.isPresent()) return;
		BlockType typeSource = optBlock.get().getBlockState().getType();
		
		if (!this.defaults.containsValue(typeSource)) return;
		
		EProtectionService service = this.plugin.getProtectionService();
		
		event.getTransactions().stream()
			.forEach(transaction -> {
				if (!transaction.isValid()) return;
				
				BlockType typeFinal = transaction.getFinal().getState().getType();
				if (!typeFinal.equals(typeSource)) return;
				
				Optional<Location<World>> location = transaction.getFinal().getLocation();
				if (!location.isPresent()) return;

				if(!service.getOrCreateEWorld(location.get().getExtent()).getRegions(location.get().getPosition()).getFlagDefault(this).containsValue(typeSource)) {
					transaction.setValid(false);
				}
			});
	}
}
