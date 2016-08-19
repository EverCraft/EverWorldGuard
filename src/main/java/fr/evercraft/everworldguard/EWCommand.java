package fr.evercraft.everworldguard;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class EWCommand extends EParentCommand<EverWorldGuard> {
	
	public EWCommand(final EverWorldGuard plugin) {
        super(plugin, "everworldguard", "worldguard", "everwg", "wg");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.EVERWORLDGUARD.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return source.hasPermission(EWPermissions.HELP.get());
	}
}
