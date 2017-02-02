package fr.evercraft.everworldguard.flag.type;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.services.worldguard.flag.EFlag;

public abstract class IntegerFlag extends EFlag<Integer> {

	public IntegerFlag(String name) {
		super(name, TypeToken.of(Integer.class));
	}
	
	@Override
	public Collection<String> getSuggests() {
		return Arrays.asList("1", "2", "3");
	}

	@Override
	public String serialize(Integer value) {
		return value.toString();
	}

	@Override
	public Integer deserialize(String value) throws IllegalArgumentException {
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}
}
