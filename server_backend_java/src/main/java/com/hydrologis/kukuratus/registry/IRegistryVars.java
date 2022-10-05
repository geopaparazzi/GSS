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
package com.hydrologis.kukuratus.registry;

/**
 * Variables used in the registry.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public interface IRegistryVars {
    String adminAuthorization = "administrator"; //$NON-NLS-1$
    String userAuthorization = "user"; //$NON-NLS-1$

    String adminGroup = "administrators"; //$NON-NLS-1$
    String userGroup = "users"; //$NON-NLS-1$

    String FIRST_ADMIN_PWD = "god"; //$NON-NLS-1$
    String FIRST_ADMIN_EMAIL = "info@hydrologis.com"; //$NON-NLS-1$
    String FIRST_ADMIN_UNIQUE_USER = "god"; //$NON-NLS-1$
    String FIRST_ADMIN_USERNAME = "HydroloGIS S.r.l."; //$NON-NLS-1$

    String FIRST_USER_PWD = "user"; //$NON-NLS-1$
    String FIRST_USER_EMAIL = "info@hydrologis.com"; //$NON-NLS-1$
    String FIRST_USER_UNIQUE_USER = "user"; //$NON-NLS-1$
    String FIRST_USER_USERNAME = "Normal User"; //$NON-NLS-1$
}
