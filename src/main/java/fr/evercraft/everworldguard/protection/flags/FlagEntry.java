package fr.evercraft.everworldguard.protection.flags;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagEntry extends StateFlag {

	public FlagEntry() {
		super("ENTRY");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENTRY.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveRegion(MoveRegionEvent.Pre.Cancellable event) {
		if(event.getEnterRegions().getFlag(event.getPlayer(), Flags.ENTRY).equals(State.DENY)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("MoveRegion : DENY");
		}
	}
}
