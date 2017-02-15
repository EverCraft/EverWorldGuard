package fr.evercraft.everworldguard.protection.flags;

import fr.evercraft.everapi.server.location.VirtualLocation;
import fr.evercraft.everapi.services.worldguard.flag.type.LocationFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

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
