package fr.evercraft.everworldguard.flag;

import fr.evercraft.everapi.server.location.VirtualLocation;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.flag.type.LocationFlag;

public class FlagTeleport extends LocationFlag {

	public FlagTeleport() {
		super("TELEPORT");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_TELEPORT.getString();
	}

	@Override
	public VirtualLocation getDefault() {
		return VirtualLocation.empty();
	}
	
	@Override
	public VirtualLocation getDefault(ProtectedRegion region) {
		return VirtualLocation.empty();
	}
}
