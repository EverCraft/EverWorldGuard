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
package fr.evercraft.everworldguard.selection;

import java.util.UUID;

import com.google.common.base.Preconditions;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.SubjectSelection;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.selection.selector.ECuboidSelector;
import fr.evercraft.everworldguard.selection.selector.ESelector;

public class ESelectionSubject implements SubjectSelection {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;
	
	private final UUID identifier;
	private ESelector selector;
	
	public ESelectionSubject(final EverWorldGuard plugin, final UUID identifier) {
		Preconditions.checkNotNull(plugin, "plugin");
		Preconditions.checkNotNull(identifier, "identifier");
		
		this.plugin = plugin;
		this.identifier = identifier;
		
		this.selector = new ECuboidSelector();
	}
	
	@Override
	public ESelector getSelector() {
		return this.selector;
	}
	
	@Override
	public void setType(SelectionRegion.Type type) {
		Preconditions.checkNotNull(type, "type");
		
		if (!this.selector.getType().equals(type)) return;
		
		if (type.equals(SelectionRegion.Type.CUBOID)) {
			this.selector = new ECuboidSelector();
		} else if (type.equals(SelectionRegion.Type.POLYGONAL)) {
			// TODO this.selector = new EPolygonalSelector();
		} else if (type.equals(SelectionRegion.Type.CYLINDER)) {
			// TODO this.selector = new ECylinderSelector();
		}
	}

	@Override
	public SelectionRegion.Type getType() {
		return this.selector.getType();
	}
	
	public String getIdentifier() {
		return this.identifier.toString();
	}
	
	public UUID getUniqueId() {
		return this.identifier;
	}
}
