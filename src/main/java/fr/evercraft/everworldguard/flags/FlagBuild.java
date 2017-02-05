package fr.evercraft.everworldguard.flags;

import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

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
