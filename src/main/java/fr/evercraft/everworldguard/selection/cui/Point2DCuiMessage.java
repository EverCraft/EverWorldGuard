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

public class Point2DCuiMessage implements CUIMessage {

    protected final int identifier;
    protected final Vector3i position;
    protected final int area;

    public Point2DCuiMessage(int identifier, final Vector3i position, int area) {
        this.identifier = identifier;
        this.position = position;
        this.area = area;
    }

    @Override
    public String getTypeId() {
        return "p2";
    }

    @Override
    public String[] getParameters() {
        return new String[] {
            String.valueOf(this.identifier),
            String.valueOf(this.position.getX()),
            String.valueOf(this.position.getZ()),
            String.valueOf(this.area)
        };
    }

}
