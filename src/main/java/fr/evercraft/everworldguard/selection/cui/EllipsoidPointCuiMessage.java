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

import com.flowpowered.math.vector.Vector3i;

public class EllipsoidPointCuiMessage implements CUIMessage {

    protected final int identifier;
    protected final Vector3i position;

    public EllipsoidPointCuiMessage(int identifier, Vector3i position) {
        this.identifier = identifier;
        this.position = position;
    }

    @Override
    public String getTypeId() {
        return "e";
    }

    @Override
    public String[] getParameters() {
        return new String[] {
            String.valueOf(this.identifier),
            String.valueOf(this.position.getX()),
            String.valueOf(this.position.getY()),
            String.valueOf(this.position.getZ())
        };
    }

}
