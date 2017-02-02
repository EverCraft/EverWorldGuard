package fr.evercraft.everworldguard.flag.type;

import java.util.Set;

import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.services.worldguard.flag.EFlag;
public abstract class SetFlag<T> extends EFlag<Set<T>> {

	public SetFlag(String name) {
		super(name, new TypeToken<Set<T>>() {
			private static final long serialVersionUID = 1L;
		});
	}
	
	public abstract String subSerialize(T value);
	public abstract T subDeserialize(String value) throws IllegalArgumentException;

	@Override
	public String serialize(Set<T> value) {
		return "";
	}

	@Override
	public Set<T> deserialize(String value) throws IllegalArgumentException {
		return null;
	}
}
