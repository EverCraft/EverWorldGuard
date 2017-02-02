package fr.evercraft.everworldguard.flag.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.services.worldguard.flag.EFlag;
import fr.evercraft.everapi.services.worldguard.flag.State;

public abstract class StringFlag extends EFlag<String> {

	public StringFlag(String name) {
		super(name, TypeToken.of(String.class));
	}
	
	@Override
	public Collection<String> getSuggests() {
		Set<String> suggests = new HashSet<String>();
		for (State state : State.values()) {
			suggests.add(state.name());
		}
		return Arrays.asList("1", "2", "3");
	}

	@Override
	public String serialize(String value) {
		return value.replaceAll("\\n", "\\\\\\\\n").replaceAll("\n", "\\\\n");
	}

	@Override
	public String deserialize(String value) throws IllegalArgumentException {
		return value.replaceAll("(?!\\\\)\\\\n", "\n").replaceAll("\\\\\\\\n", "\\n");
	}
}
