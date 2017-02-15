package fr.evercraft.everworldguard.protection.flags;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import fr.evercraft.everapi.event.MoveRegionEvent;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagExit extends StateFlag {

	public FlagExit() {
		super("EXIT");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_EXIT.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Listener(order=Order.FIRST)
	public void onMoveRegion(MoveRegionEvent.Pre.Cancellable event) {
		if(event.getExitRegions().getFlag(event.getPlayer(), Flags.EXIT).equals(State.DENY)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("MoveRegion : DENY");
		}
	}
}
