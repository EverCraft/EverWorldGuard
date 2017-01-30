package fr.evercraft.everworldguard.flag;

import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.services.worldguard.flag.EFlag;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.State;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagBuild extends EFlag<State> implements Flag<State>  {

	public FlagBuild() {
		super("BUILD", TypeToken.of(State.class));
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_BUILD.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}

	@Override
	public String serialize(State value) {
		return value.name();
	}

	@Override
	public State deserialize(String value) throws IllegalArgumentException {
		try {
			return State.valueOf(value);
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}
}
