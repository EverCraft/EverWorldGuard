/*
 * This file is part of EverSigns.
 *
 * EverSigns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverSigns is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverSigns.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.eversigns;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.eversigns.service.ESign;

public class ESListener {
	private EverSigns plugin;
	
	public ESListener(final EverSigns plugin) {
		this.plugin = plugin;
	}
	
	@Listener
	public void onSignChange(ChangeSignEvent event, @First Player player) {
		if(!event.isCancelled()) {
			SignData signData = event.getText();
			Optional<ListValue<Text>> value = signData.getValue(Keys.SIGN_LINES);
			if (value.isPresent()) {
				Optional<ESign> sign = this.plugin.getService().get(value.get().get(0).toPlain());
				if(sign.isPresent()) {
					Optional<EPlayer> optPlayer = this.plugin.getEServer().getEPlayer(player.getUniqueId());
					if(optPlayer.isPresent()) {
						event.setCancelled(!sign.get().createSign(optPlayer.get(), event.getTargetTile().getLocation(), signData));
					}
				} else {
					String line_0 = value.get().get(0).toPlain();
					String line_1 = value.get().get(1).toPlain();
					String line_2 = value.get().get(2).toPlain();
					String line_3 = value.get().get(3).toPlain();
					
					if(!player.hasPermission(ESPermissions.REPLACE_COLOR.get())) {
						line_0 = line_0.replaceAll(EChat.REGEX_COLOR, "");
						line_1 = line_1.replaceAll(EChat.REGEX_COLOR, "");
						line_2 = line_2.replaceAll(EChat.REGEX_COLOR, "");
						line_3 = line_3.replaceAll(EChat.REGEX_COLOR, "");
					}
					if(!player.hasPermission(ESPermissions.REPLACE_FORMAT.get())) {
						line_0 = line_0.replaceAll(EChat.REGEX_FORMAT, "");
						line_1 = line_1.replaceAll(EChat.REGEX_FORMAT, "");
						line_2 = line_2.replaceAll(EChat.REGEX_FORMAT, "");
						line_3 = line_3.replaceAll(EChat.REGEX_FORMAT, "");
					}
					if(!player.hasPermission(ESPermissions.REPLACE_MAGIC.get())) {
						line_0 = line_0.replaceAll(EChat.REGEX_MAGIC, "");
						line_1 = line_1.replaceAll(EChat.REGEX_MAGIC, "");
						line_2 = line_2.replaceAll(EChat.REGEX_MAGIC, "");
						line_3 = line_3.replaceAll(EChat.REGEX_MAGIC, "");
					}
					
					signData = signData.set(value.get().set(0, EChat.of(line_0)));
					signData = signData.set(value.get().set(1, EChat.of(line_1)));
					signData = signData.set(value.get().set(2, EChat.of(line_2)));
					signData = signData.set(value.get().set(3, EChat.of(line_3)));
				}
			}
		}
	}
	
	@Listener
	public void onSignClicked(InteractBlockEvent event, @First Player subject) {
	    Optional<Location<World>> location = event.getTargetBlock().getLocation();
	    if(location.isPresent()) {
	    	Optional<TileEntity> title = location.get().getTileEntity();
	    	if(title.isPresent() && title.get() instanceof Sign) {
	    		Sign sign = (Sign) title.get();
	    		if(sign.getSignData().get(0).isPresent()) {
		    		Optional<ESign> esign = this.plugin.getService().get(sign.getSignData().get(0).get().toPlain());
					if(esign.isPresent()) {
						Optional<EPlayer> optPlayer = this.plugin.getEServer().getEPlayer(subject.getUniqueId());
						if(optPlayer.isPresent()) {
							esign.get().useSign(optPlayer.get(), sign);
						}
					}
	    		}
	    	}
	    }
	}
	
	@Listener
	public void onSignBreak(ChangeBlockEvent.Break event, @First Player subject) {
		if(!event.isCancelled()) {
			for(Transaction<BlockSnapshot> transaction : event.getTransactions()) {
				if(transaction.isValid() && transaction.getOriginal().getLocation().isPresent()) {
					Optional<List<Text>> lines = transaction.getOriginal().get(Keys.SIGN_LINES);
					if(lines.isPresent() && !lines.get().isEmpty() && !lines.get().get(0).isEmpty()) {
						Optional<ESign> sign = this.plugin.getService().get(lines.get().get(0).toPlain());
						if(sign.isPresent()) {
							Optional<EPlayer> player = this.plugin.getEServer().getEPlayer(subject.getUniqueId());
							if(player.isPresent()) {
								sign.get().breakSign(player.get(), transaction.getOriginal().getLocation().get(), lines.get());
							}
						}
					}
				}
			}
		}
	}
}
