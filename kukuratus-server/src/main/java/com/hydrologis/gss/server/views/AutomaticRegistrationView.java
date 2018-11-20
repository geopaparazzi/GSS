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
package com.hydrologis.gss.server.views;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;

import com.hydrologis.gss.server.GssWebConfig;
import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.Settings;
import com.hydrologis.kukuratus.libs.spi.SettingsPage;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.KukuratusWindows;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AutomaticRegistrationView extends VerticalLayout implements View, SettingsPage {
    private static final long serialVersionUID = 1L;

    @Override
    public void enter( ViewChangeEvent event ) {
        Button activateRegistrationButton = new Button(VaadinIcons.TIMER);

        String limitStr = RegistryHandler.INSTANCE.getGlobalSettingByKey(GssWebConfig.KEY_AUTOMATIC_REGISTRATION, "0"); //$NON-NLS-1$
        long limitTmp = Long.parseLong(limitStr);
        long now = System.currentTimeMillis();
        double deltaMinutes = (now - limitTmp) / 60.0 / 1000.0;
        String label = MessageFormat.format(Messages.getString("AutomaticRegistrationView.activate_auto_reg"), //$NON-NLS-1$
                GssWebConfig.timerMinutes);
        if (deltaMinutes < GssWebConfig.timerMinutes) {
            String endTime = new DateTime(limitTmp + GssWebConfig.timerMinutes * 60 * 1000)
                    .toString(HMConstants.dateTimeFormatterYYYYMMDDHHMM);
            label = Messages.getString("AutomaticRegistrationView.auto_reg_active_until") + endTime; //$NON-NLS-1$
        }

        activateRegistrationButton.setCaption(label);
        activateRegistrationButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        activateRegistrationButton.setDescription(""); //$NON-NLS-1$
        activateRegistrationButton.addClickListener(e -> {
            Settings setting = new Settings(GssWebConfig.KEY_AUTOMATIC_REGISTRATION, String.valueOf(now), null);
            try {
                RegistryHandler.INSTANCE.insertOrUpdateGlobalSetting(setting);

                long limit = now + GssWebConfig.timerMinutes * 60 * 1000;

                String endTime = new DateTime(limit).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMM);

                KukuratusWindows.openInfoNotification(MessageFormat
                        .format(Messages.getString("AutomaticRegistrationView.devices_will_register"), endTime)); //$NON-NLS-1$
            } catch (SQLException e1) {
                KukuratusLogger.logError(this, e1);
            }
        });
        addComponent(activateRegistrationButton);
        setComponentAlignment(activateRegistrationButton, Alignment.MIDDLE_CENTER);

        setSizeFull();
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.TIMER;
    }

    @Override
    public String getLabel() {
        return Messages.getString("AutomaticRegistrationView.autoregistration"); //$NON-NLS-1$
    }

    @Override
    public String getPagePath() {
        return "automaticregistration"; //$NON-NLS-1$
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends View> Class<T> getNavigationViewClass() {
        return (Class<T>) this.getClass();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean onlyAdmin() {
        return true;
    }
}
