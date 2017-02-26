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

import fr.evercraft.everapi.services.selection.CUIMessage;

public class MinMaxCuiMessage implements CUIMessage {

    protected final int min;
    protected final int max;

    public MinMaxCuiMessage(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getTypeId() {
        return "mm";
    }

    @Override
    public String[] getParameters() {
        return new String[] {
                    String.valueOf(this.min),
                    String.valueOf(this.max),
                };
    }

}
