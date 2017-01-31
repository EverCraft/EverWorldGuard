package fr.evercraft.everworldguard.flag.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.services.worldguard.flag.EFlag;
import fr.evercraft.everapi.services.worldguard.flag.State;

public abstract class StateFlag extends EFlag<State> {

	public StateFlag(String name) {
		super(name, TypeToken.of(State.class));
	}
	
	@Override
	public Collection<String> getSuggests() {
		Set<String> suggests = new HashSet<String>();
		for (State state : State.values()) {
			suggests.add(state.name());
		}
		return suggests;
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
