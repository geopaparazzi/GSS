/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.hydrologis.gss.GssContext;

import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.StageUtils;

public class GssUserPermissionsHandler {
    private List<String> allowedPagesNames;
    private boolean isAdmin;
    private List<String> allPagesNames;

    public GssUserPermissionsHandler( boolean isAdmin ) {
        this.isAdmin = isAdmin;
        List<String> availableEntryPointNames = GssContext.instance().getAvailableEntryPointNames();
        allPagesNames = availableEntryPointNames.stream().map(id -> StageUtils.getPathforEntryPointID(id)).sorted()
                .collect(Collectors.toList());
        String allDefault = allPagesNames.stream().filter(path -> {
            return path.contains("dashboard") || path.contains("mapviewer");
        }).collect(Collectors.joining(GssGuiUtilities.DELIMITER));
        String userPermissions = RegistryHandler.INSTANCE.getSettingByKey(GssGuiUtilities.KEY_USER_VIEWS_PERMISSIONS,
                allDefault);
        allowedPagesNames = new ArrayList<>();
        Collections.addAll(allowedPagesNames, userPermissions.split(GssGuiUtilities.DELIMITER));
    }

    public List<String> getAllPagesNames() {
        return allPagesNames;
    }

    public List<String> getAllowedPagesNames() {
        return allowedPagesNames;
    }

    public boolean isAllowed( String path ) {
        if (isAdmin) {
            return true;
        } else {
            return allowedPagesNames.contains(path);
        }
    }
}
