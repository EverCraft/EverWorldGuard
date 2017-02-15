package fr.evercraft.everworldguard.protection.flags;

import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagInvincibility extends StateFlag {

	public FlagInvincibility() {
		super("INVINCIBILITY");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INVINCIBILITY.getString();
	}

	@Override
	public State getDefault() {
		return State.DENY;
	}
}
