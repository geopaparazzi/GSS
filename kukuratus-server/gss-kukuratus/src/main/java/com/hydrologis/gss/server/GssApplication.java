package com.hydrologis.gss.server;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.hydrologis.gss.server.views.AboutView;
import com.hydrologis.gss.server.views.DashboardPage;
import com.hydrologis.gss.server.views.KmzExportView;
import com.hydrologis.gss.server.views.MapPage;
import com.hydrologis.gss.server.views.PdfExportView;
import com.hydrologis.gss.server.views.SurveyorsView;
import com.hydrologis.kukuratus.libs.KukuratusLibs;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.auth.LoginPage;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.User;
import com.hydrologis.kukuratus.libs.spi.ExportPage;
import com.hydrologis.kukuratus.libs.spi.SettingsPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.log.LogView;
import com.hydrologis.kukuratus.libs.views.MapChooserView;
import com.hydrologis.kukuratus.libs.views.WebUsersView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import kaesdingeling.hybridmenu.HybridMenu;
import kaesdingeling.hybridmenu.components.HMButton;
import kaesdingeling.hybridmenu.components.HMLabel;
import kaesdingeling.hybridmenu.components.HMSubMenu;
import kaesdingeling.hybridmenu.components.LeftMenu;
import kaesdingeling.hybridmenu.data.MenuConfig;
import kaesdingeling.hybridmenu.design.DesignItem;

@Title("Geopaparazzi Survey Server")
@Push
@Theme("mytheme")
public class GssApplication extends UI {
    private static final long serialVersionUID = 1L;
    private HybridMenu hybridMenu;
    private String authenticatedUsername;

    @Override
    protected void init( VaadinRequest request ) {
        KukuratusLibs.init();

        // FIXME remove this
//        VaadinSession.getCurrent().setAttribute(AuthService.USERNAME_ATTRIBUTE, "god");
        if (AuthService.INSTANCE.isAuthenticated()) {

            authenticatedUsername = AuthService.INSTANCE.getAuthenticatedUsername();

            MenuConfig menuConfig = new MenuConfig();
            menuConfig.withDesignItem(DesignItem.getWhiteDesign());
            menuConfig.withBreadcrumbs(false);

            VerticalLayout naviRootContent = new VerticalLayout();
            naviRootContent.setSizeFull();
            hybridMenu = HybridMenu.get().withNaviContent(naviRootContent)//
                    .withConfig(menuConfig)//
                    .build();

            buildTopOnlyMenu();
            buildLeftMenu();

            // default page
            UI.getCurrent().getNavigator().setErrorView(DashboardPage.class);

            hybridMenu.setSizeFull();
            setContent(hybridMenu);

        } else {
            LoginPage loginPage = new LoginPage();
            VerticalLayout layout = new VerticalLayout();
            loginPage.setSizeUndefined();

            ThemeResource resource = new ThemeResource("images/gss_logo.png");
            Image image = new Image("", resource);

            layout.addComponent(image);
            layout.addComponent(loginPage);
            layout.setComponentAlignment(loginPage, Alignment.MIDDLE_CENTER);
            layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
//            layout.setSizeFull();
            setContent(layout);
        }

    }

    private void buildTopOnlyMenu() {
//        TopMenu topMenu = hybridMenu.getTopMenu();
//        topMenu.add(HMButton.get().withIcon(VaadinIcons.EXIT).withDescription("Logout").withClickListener(e -> {
//            AuthService.INSTANCE.logout();
//        }));
//        hybridMenu.getNotificationCenter().setNotiButton(topMenu.add(HMButton.get().withDescription("Notifications")));
    }

    private void buildLeftMenu() {
        boolean isAdmin = false;
        try {
            User loggedUser = RegistryHandler.INSTANCE.getUserByUniqueName(authenticatedUsername);
            isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        } catch (Exception e1) {
            KukuratusLogger.logError(this, e1);
        }

        LeftMenu leftMenu = hybridMenu.getLeftMenu();

        HMLabel logoLabel = HMLabel.get().withIcon(new ThemeResource("images/logo.png"));
        leftMenu.add(logoLabel);

        leftMenu.add(HMButton.get().withCaption("Dashboard").withIcon(VaadinIcons.DASHBOARD).withNavigateTo(DashboardPage.class));
        leftMenu.add(HMButton.get().withCaption("Map View").withIcon(VaadinIcons.MAP_MARKER).withNavigateTo(MapPage.class));

        // add settings
        List<SettingsPage> settingsPages = SpiHandler.INSTANCE.getSettingsPages();
        if (!isAdmin) {
            settingsPages.removeIf(sp -> sp.onlyAdmin());
        }
        if (!settingsPages.isEmpty()) {
            HMSubMenu settingsMenu = leftMenu.add(HMSubMenu.get().withCaption("Settings").withIcon(VaadinIcons.COGS));
            for( SettingsPage settingsPage : settingsPages ) {
                settingsMenu.add(HMButton.get().withCaption(settingsPage.getLabel()).withIcon(settingsPage.getIcon())
                        .withNavigateTo(settingsPage.getNavigationViewClass()));
            }
        }

        // add exports
        List<ExportPage> exportsPages = SpiHandler.INSTANCE.getExportPages();
        if (!isAdmin) {
            exportsPages.removeIf(sp -> sp.onlyAdmin());
        }
        if (!exportsPages.isEmpty()) {
            HMSubMenu exportsMenu = leftMenu.add(HMSubMenu.get().withCaption("Export").withIcon(VaadinIcons.CLOUD_DOWNLOAD));
            for( ExportPage exportPage : exportsPages ) {
                exportsMenu.add(HMButton.get().withCaption(exportPage.getLabel()).withIcon(exportPage.getIcon())
                        .withNavigateTo(exportPage.getNavigationViewClass()));
            }
        }

        if (isAdmin) {
            leftMenu.add(
                    HMButton.get().withCaption("Log View").withIcon(VaadinIcons.CLIPBOARD_PULSE).withNavigateTo(LogView.class));
        }

        leftMenu.add(HMButton.get().withCaption("About").withIcon(VaadinIcons.INFO_CIRCLE_O).withNavigateTo(AboutView.class));

        leftMenu.add(HMButton.get().withCaption("Logout").withIcon(VaadinIcons.EXIT_O).withClickListener(e -> {
            AuthService.INSTANCE.logout();
        }));

    }

}
