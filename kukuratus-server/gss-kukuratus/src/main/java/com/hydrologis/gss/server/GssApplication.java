package com.hydrologis.gss.server;

import com.hydrologis.gss.server.views.AboutView;
import com.hydrologis.gss.server.views.DashboardPage;
import com.hydrologis.gss.server.views.KmzExportView;
import com.hydrologis.gss.server.views.MapChooserView;
import com.hydrologis.gss.server.views.MapPage;
import com.hydrologis.gss.server.views.PdfExportView;
import com.hydrologis.gss.server.views.SurveyorsView;
import com.hydrologis.gss.server.views.WebUsersView;
import com.hydrologis.kukuratus.libs.KukuratusLibs;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.auth.LoginPage;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.User;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.log.LogView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

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
        VaadinSession.getCurrent().setAttribute(AuthService.USERNAME_ATTRIBUTE, "god");
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
//        logoLabel.setStyleName("mytheme-v-icon", true);
        leftMenu.add(logoLabel);

        leftMenu.add(HMButton.get().withCaption("Dashboard").withIcon(VaadinIcons.DASHBOARD).withNavigateTo(DashboardPage.class));

        leftMenu.add(HMButton.get().withCaption("Map View").withIcon(VaadinIcons.MAP_MARKER).withNavigateTo(MapPage.class));

        HMSubMenu settingsList = leftMenu.add(HMSubMenu.get().withCaption("Settings").withIcon(VaadinIcons.COGS));
        if (isAdmin) {
            settingsList.add(
                    HMButton.get().withCaption("Surveyors").withIcon(VaadinIcons.SPECIALIST).withNavigateTo(SurveyorsView.class));
            settingsList
                    .add(HMButton.get().withCaption("Web Users").withIcon(VaadinIcons.GROUP).withNavigateTo(WebUsersView.class));
        }
        settingsList.add(
                HMButton.get().withCaption("Map Chooser").withIcon(VaadinIcons.MAP_MARKER).withNavigateTo(MapChooserView.class));

        HMSubMenu exportsList = leftMenu.add(HMSubMenu.get().withCaption("Export").withIcon(VaadinIcons.CLOUD_DOWNLOAD));
        exportsList.add(HMButton.get().withCaption("PDF").withIcon(VaadinIcons.FILE_TEXT_O).withNavigateTo(PdfExportView.class));
        exportsList.add(HMButton.get().withCaption("KMZ").withIcon(VaadinIcons.GLOBE).withNavigateTo(KmzExportView.class));

        if (isAdmin) {
//            settingsList.add(
//                    HMButton.get().withCaption("Other Settings").withIcon(VaadinIcons.COG).withNavigateTo(SettingsView.class));
            leftMenu.add(
                    HMButton.get().withCaption("Log View").withIcon(VaadinIcons.CLIPBOARD_PULSE).withNavigateTo(LogView.class));
        }

        leftMenu.add(HMButton.get().withCaption("About").withIcon(VaadinIcons.INFO_CIRCLE_O).withNavigateTo(AboutView.class));

        leftMenu.add(HMButton.get().withCaption("Logout").withIcon(VaadinIcons.EXIT_O).withClickListener(e -> {
            AuthService.INSTANCE.logout();
        }));

    }

}
