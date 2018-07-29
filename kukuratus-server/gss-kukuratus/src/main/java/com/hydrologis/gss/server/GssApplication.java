package com.hydrologis.gss.server;

import com.hydrologis.gss.server.views.DashboardPage;
import com.hydrologis.gss.server.views.MapChooserView;
import com.hydrologis.gss.server.views.MapPage;
import com.hydrologis.gss.server.views.SettingsView;
import com.hydrologis.gss.server.views.SurveyorsView;
import com.hydrologis.gss.server.views.WebUsersView;
import com.hydrologis.kukuratus.libs.KukuratusLibs;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.auth.LoginPage;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import kaesdingeling.hybridmenu.HybridMenu;
import kaesdingeling.hybridmenu.components.HMButton;
import kaesdingeling.hybridmenu.components.HMLabel;
import kaesdingeling.hybridmenu.components.HMSubMenu;
import kaesdingeling.hybridmenu.components.LeftMenu;
import kaesdingeling.hybridmenu.data.MenuConfig;
import kaesdingeling.hybridmenu.design.DesignItem;

@Theme("mytheme")
public class GssApplication extends UI {
    private static final long serialVersionUID = 1L;
    private HybridMenu hybridMenu;

    @Override
    protected void init( VaadinRequest request ) {
        KukuratusLibs.init();

        VaadinSession.getCurrent().setAttribute(AuthService.USERNAME_ATTRIBUTE, "god");
        if (AuthService.INSTANCE.isAuthenticated()) {

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
            setContent(new LoginPage());
        }

    }

    private void buildTopOnlyMenu() {
//        TopMenu topMenu = hybridMenu.getTopMenu();

//        topMenu.add(HMTextField.get(VaadinIcons.SEARCH, "Search ..."));
//
//        topMenu.add(HMButton.get().withIcon(VaadinIcons.HOME).withDescription("Home").withNavigateTo(DashboardPage.class));

//        hybridMenu.getNotificationCenter().setNotiButton(topMenu.add(HMButton.get().withDescription("Notifications")));
    }

    private void buildLeftMenu() {
        LeftMenu leftMenu = hybridMenu.getLeftMenu();

        // Find the application directory
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        System.out.println(basepath);
//        FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/logo.png"));

        leftMenu.add(HMLabel.get()// .withCaption("<b>Geopaparazzi<br/>Survey<br/>Server</b>")
                .withIcon(new ThemeResource("images/logo.png")));

//        HMButton dashBoardButton = 
        leftMenu.add(HMButton.get().withCaption("Dashboard").withIcon(VaadinIcons.DASHBOARD).withNavigateTo(DashboardPage.class));
//        hybridMenu.getBreadCrumbs().setRoot(dashBoardButton);

        leftMenu.add(HMButton.get().withCaption("Map View").withIcon(VaadinIcons.MAP_MARKER).withNavigateTo(MapPage.class));

        HMSubMenu settingsList = leftMenu.add(HMSubMenu.get().withCaption("Settings").withIcon(VaadinIcons.COGS));
        settingsList.add(
                HMButton.get().withCaption("Surveyors").withIcon(VaadinIcons.SPECIALIST).withNavigateTo(SurveyorsView.class));
        settingsList.add(HMButton.get().withCaption("Web Users").withIcon(VaadinIcons.GROUP).withNavigateTo(WebUsersView.class));
        settingsList.add(
                HMButton.get().withCaption("Map Chooser").withIcon(VaadinIcons.MAP_MARKER).withNavigateTo(MapChooserView.class));
        settingsList
                .add(HMButton.get().withCaption("Other Settings").withIcon(VaadinIcons.COG).withNavigateTo(SettingsView.class));

        leftMenu.add(HMButton.get().withCaption("Logout").withIcon(VaadinIcons.EXIT_O).withClickListener(e -> {
            AuthService.INSTANCE.logout();
        }));

    }

}
