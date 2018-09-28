package com.hydrologis.gss.server.utils;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public abstract class KukuratusWindows extends VerticalLayout {
    private Window mainWindow;
    private boolean modal;
    private String message;
    private String result;

    public KukuratusWindows( String message, boolean modal ) {
        this.message = message;
        this.modal = modal;
        mainWindow = new Window();
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public String getMessage() {
        return message;
    }

    public String getResult() {
        return result;
    }

    public void centerWithSize( String width, String height ) {
        mainWindow.center();
        if (width != null) {
            mainWindow.setWidth(width);
        }
        if (height != null) {
            mainWindow.setHeight(height);
        }
    }

    public void open( Component parentcomponent ) {
        mainWindow.setContent(this);
        mainWindow.setModal(modal);
        setMargin(true);
        addWidgets(this);
        parentcomponent.getUI().addWindow(mainWindow);
    }

    /**
     * Leaves or removes the close X in the header.
     * 
     * @param closable
     */
    public void setClosable( boolean closable ) {
        mainWindow.setClosable(closable);
    }

    /**
     * Leaves or removes the maximize + button in the header.
     * 
     * @param resizable
     */
    public void setResizable( boolean resizable ) {
        mainWindow.setResizable(resizable);
    }

    /**
     * Add here the widgets to the internal vertical layout.
     * 
     * @param layout the layout to add to.
     */
    public abstract void addWidgets( VerticalLayout layout );

    /**
     * Open a dialog with delete and cancel button.
     * 
     * @param parent parent component.
     * @param message the message for the user.
     * @param width optional width.
     * @param height optional height.
     * @param onDeleteRunnable a runnable to execut on delete button pushed.
     */
    public static void openCancelDeleteWindow( Component parent, String message, String width, String height,
            Runnable onDeleteRunnable ) {
        if (width == null) {
            width = "30%";
        }
        KukuratusWindows window = new KukuratusWindows(message, true){
            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                cancelButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                Button deleteButton = new Button("Delete", VaadinIcons.TRASH);
                deleteButton.addClickListener(e -> onActionButtonPushed());
                deleteButton.setStyleName(ValoTheme.BUTTON_DANGER);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, deleteButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                onDeleteRunnable.run();
                getMainWindow().close();
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }
        };
        window.centerWithSize(width, height);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void inputWindow( Component parent, String message, String width, String height,
            TextRunnable onCommitRunnable ) {
        if (width == null) {
            width = "30%";
        }
        KukuratusWindows window = new KukuratusWindows(message, true){
            private TextField inputText;

            @Override
            public void addWidgets( VerticalLayout layout ) {
                Label label = new Label(getMessage(), ContentMode.HTML);
                layout.addComponent(label);
                layout.setComponentAlignment(label, Alignment.TOP_CENTER);

                inputText = new TextField();
                layout.addComponent(inputText);
                layout.setComponentAlignment(inputText, Alignment.BOTTOM_CENTER);

                Button cancelButton = new Button("Cancel", VaadinIcons.CLOSE);
                cancelButton.addClickListener(e -> onCancelButtonPushed());
                Button okButton = new Button("Ok", VaadinIcons.CHECK);
                okButton.addClickListener(e -> onActionButtonPushed());
                okButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
                HorizontalLayout hLayout = new HorizontalLayout(cancelButton, okButton);
                layout.addComponent(hLayout);
                hLayout.setMargin(true);
                layout.setComponentAlignment(hLayout, Alignment.BOTTOM_CENTER);
            }

            public void onActionButtonPushed() {
                onCommitRunnable.setText(inputText.getValue());
                onCommitRunnable.run();
                getMainWindow().close();
            }

            public void onCancelButtonPushed() {
                getMainWindow().close();
            }
        };
        window.centerWithSize(width, height);
        window.setClosable(false);
        window.setResizable(false);
        window.open(parent);
    }

    public static void openInfoNotification( String message ) {
        Notification.show(message, Notification.Type.HUMANIZED_MESSAGE);
    }

    public static void openWarningNotification( String message ) {
        Notification.show(message, Notification.Type.WARNING_MESSAGE);
    }

    public static void openErrorNotification( String message ) {
        Notification.show(message, Notification.Type.ERROR_MESSAGE);
    }

    public static void openTrayNotification( String message ) {
        Notification.show(message, Notification.Type.TRAY_NOTIFICATION);
    }
}
