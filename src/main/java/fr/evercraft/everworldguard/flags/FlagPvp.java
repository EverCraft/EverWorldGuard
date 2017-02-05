package fr.evercraft.everworldguard.flags;

import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagPvp extends StateFlag {

	public FlagPvp() {
		super("PVP");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_PVP.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
}
