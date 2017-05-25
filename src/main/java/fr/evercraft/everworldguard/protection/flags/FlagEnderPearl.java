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

import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.teleport.EntityTeleportCause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagEnderPearl extends StateFlag {

	private final EverWorldGuard plugin;

	public FlagEnderPearl(EverWorldGuard plugin) {
		super("ENDERPEARL");
		
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENDERPEARL_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public boolean sendMessage(Player player, Vector3i position) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_ENDERPEARL_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ()));
	}

	public void onInteractItem(InteractItemEvent event) {
		if (event.isCancelled()) return;
		
		if (!event.getItemStack().getType().equals(ItemTypes.ENDER_PEARL)) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		
		Player player = optPlayer.get();
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld());
		if (world.getRegions(player.getLocation().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			this.sendMessage(player, player.getLocation().getPosition().toInt());
		}
	}

	public void onMoveEntityTeleport(MoveEntityEvent.Teleport event, WorldWorldGuard world, Player player) {
		if (event.isCancelled()) return;
		
		Optional<EntityTeleportCause> optCause = event.getCause().get(NamedCause.SOURCE, EntityTeleportCause.class);
		if (!optCause.isPresent()) return;
		EntityTeleportCause cause = optCause.get();
		
		if (!cause.getTeleporter().getType().equals(EntityTypes.ENDER_PEARL)) return;
		
		if (world.getRegions(event.getToTransform().getPosition()).getFlag(player, this).equals(State.DENY)) {
			event.setCancelled(true);
			this.sendMessage(player, player.getLocation().getPosition().toInt());
			
			// TODO Rendre l'enderpearl
		}
	}
}
