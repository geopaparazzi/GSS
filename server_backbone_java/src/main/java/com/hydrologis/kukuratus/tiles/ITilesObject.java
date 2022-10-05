/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.tiles;

/**
 * Interface for tiles handling objects.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public interface ITilesObject {

    String ID = "id"; //$NON-NLS-1$
    String X = "x"; //$NON-NLS-1$
    String Y = "y"; //$NON-NLS-1$
    String Z = "z"; //$NON-NLS-1$

    /**
     * @return the name of the object.
     */
    String getName();

}
