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
package fr.evercraft.everworldguard.command.region;

import java.util.Optional;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegion extends EParentCommand<EverWorldGuard> {
	
	public EWRegion(final EverWorldGuard plugin) {
        super(plugin, "region", "rg");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return true;
	}
	
	public static Optional<World> getWorld(EverWorldGuard plugin, CommandSource source, Args args, String marker) {
		Optional<String> optWorld = args.getValue(marker);
		
		if (optWorld.isPresent()) {
			return plugin.getEServer().getWorld(optWorld.get());
		} else if (source instanceof Player) {
			return Optional.of(((Player) source).getWorld());
		}
		return Optional.empty();
	}
}
