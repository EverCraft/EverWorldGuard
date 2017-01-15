package fr.evercraft.everworldguard.command.select;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SelectType;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectRemove extends ESubCommand<EverWorldGuard> {
	
	public EWSelectRemove(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "type");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_TYPE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName())
				.onClick(TextActions.suggestCommand("/" + this.getName()))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return new ArrayList<String>();
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 0) {
			if (source instanceof EPlayer) {
				resultat = this.commandSelectRemove((EPlayer) source);
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}

	private boolean commandSelectRemove(final EPlayer player) {		
		if (!player.getSelectType().equals(SelectType.POLY)) {
			EWMessages.SELECT_REMOVE_ERROR.sendTo(player);
			return false;
		}
		
		if (player.getSelectPoints().isEmpty()) {
			EWMessages.SELECT_REMOVE_EMPTY.sendTo(player);
			return false;
		}
		List<Vector3i> points = player.getSelectPoints();
		Vector3i pos = points.get(points.size() - 1);
		player.removeSelectPoint(points.size() - 1);
		
		EWMessages.SELECT_REMOVE_PLAYER.sender()
			.replace("<pos>", EWSelect.getPositionHover(pos))
			.sendTo(player);
		return true;
	}
}
