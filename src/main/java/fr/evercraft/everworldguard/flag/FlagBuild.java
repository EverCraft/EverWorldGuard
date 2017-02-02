package fr.evercraft.everworldguard.flag;

import fr.evercraft.everapi.services.worldguard.flag.State;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.flag.type.StateFlag;

public class FlagBuild extends StateFlag {

	public FlagBuild() {
		super("BUILD");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_BUILD.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
}
