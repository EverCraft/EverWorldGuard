/*
 * This file is part of EverWorldGuard.
 *
 * EverWorldGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverWorldGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverWorldGuard.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.selection.cui;

import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.selection.ESelectionSubject;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CUIChannel implements RawDataListener {
    public static final String CUI_CHANNEL = "WECUI";

    private final EverWorldGuard plugin;
    private final ChannelBinding.RawDataChannel channel;

    public CUIChannel(EverWorldGuard plugin) {
    	this.plugin = plugin;
    	this.channel = Sponge.getChannelRegistrar().createRawChannel(this.plugin, CUI_CHANNEL);
        this.channel.addListener(Platform.Type.SERVER, this);
    }


    public ChannelBinding.RawDataChannel getChannel() {
        return this.channel;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Platform.Type side) {
        if (connection instanceof PlayerConnection) {
            Player player = ((PlayerConnection) connection).getPlayer();

            Optional<ESelectionSubject> optSubject = this.plugin.getSelectionService().getSubject(player.getUniqueId());
            if (!optSubject.isPresent()) return;
            ESelectionSubject subject = optSubject.get();
            
            if (subject.isCuiSupport()) return;
            
            String[] split = (new String(data.readBytes(data.available()), StandardCharsets.UTF_8)).split("\\|");
            if (split.length > 1 && split[0].equalsIgnoreCase("v")) {
            	subject.setCuiSupport(true);
                try {
                	subject.setCUIVersion(Integer.parseInt(split[1]));
                } catch (NumberFormatException e) {
                    this.plugin.getELogger().warn("Error while reading CUI init message: " + e.getMessage());
                }
            }
            
            subject.describeCUI(player);
        }
    }
}
