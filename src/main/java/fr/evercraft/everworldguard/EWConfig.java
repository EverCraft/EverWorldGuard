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
package fr.evercraft.everworldguard;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.sponge.UtilsItemType;

public class EWConfig extends EConfig<EverWorldGuard> {

	public EWConfig(final EverWorldGuard plugin) {
		super(plugin);
	}
	
	public void reload() {
		super.reload();
		this.plugin.getELogger().setDebug(this.isDebug());
	}
	
	@Override
	public List<String> getHeader() {
		return 	Arrays.asList(	"####################################################### #",
								"                EverWorldGuard (By rexbut)               #",
								"    For more information : https://docs.evercraft.fr     #",
								"####################################################### #");
	}
	
	@Override
	public void loadDefault() {
		this.configDefault();
		this.sqlDefault();
		
		addDefault("message.interval", 1, "Second");
		
		addDefault("select.item", ItemTypes.WOODEN_AXE.getId());
		addDefault("select.maxPolygonalPoints", 20);
		
		addDefault("region.info", ItemTypes.LEATHER.getId());
		addDefault("region.maxRegionCountPerPlayer", 10, "");
	}

	public ItemType getSelectItem() {
		String item_string = this.get("select.item").getString("");
		Optional<ItemType> item = UtilsItemType.getItemType(item_string);
		if (!item.isPresent()) {
			this.plugin.getELogger().warn("[Config] 'select.item' : Can not find itemType '" + item_string + "'");
			return ItemTypes.WOODEN_AXE;
		}
		return item.get();
	}
	
	public int getSelectMaxPolygonalPoints() {
		return this.get("select.maxPolygonalPoints").getInt(20);
	}

	/*
	 * Millisecond
	 */
	public int getMessageInterval() {
		return this.get("message.interval").getInt(1) * 1000;
	}
	
	public int getRegionMaxRegionCountPerPlayer() {
		return this.get("region.maxRegionCountPerPlayer").getInt(10);
	}
	
	public ItemType getRegionInfo() {
		String item_string = this.get("region.info").getString("");
		Optional<ItemType> item = UtilsItemType.getItemType(item_string);
		if (!item.isPresent()) {
			this.plugin.getELogger().warn("[Config] 'region.info' : Can not find itemType '" + item_string + "'");
			return ItemTypes.LEATHER;
		}
		return item.get();
	}

	public void setSql(boolean value) {
		this.get("SQL.enable").setValue(value);
		this.save(true);
	}
}
