package com.hydrologis.gss.server.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.gss.server.database.GssDatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.User;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.lowagie.text.Anchor;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class PdfExportView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    private HorizontalLayout header = new HorizontalLayout();
    private Panel panel = new Panel();

    private MenuBar.MenuItem reportsMenuItem;

    private Dao<Images, ? > imagesDAO;

    private Dao<ImageData, ? > imageDataDAO;

    private String authenticatedUsername;

    private File logoFile;

    @Override
    public void enter( ViewChangeEvent event ) {
        try {
            authenticatedUsername = AuthService.INSTANCE.getAuthenticatedUsername();
            GssDatabaseHandler dbHandler = GssDbProvider.INSTANCE.getDatabaseHandler().get();
            imagesDAO = dbHandler.getDao(Images.class);
            imageDataDAO = dbHandler.getDao(ImageData.class);

            MenuBar menuBar = new MenuBar();
            menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

            reportsMenuItem = menuBar.addItem("Surveyors", VaadinIcons.SPECIALIST, null);

            Dao<GpapUsers, ? > usersDAO = GssDbProvider.INSTANCE.getDatabaseHandler().get().getDao(GpapUsers.class);
            List<GpapUsers> users = usersDAO.queryForAll();
            reportsMenuItem.addItem("ALL", VaadinIcons.GROUP, i -> {
                try {
                    calcForUser(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            users.forEach(user -> {
                reportsMenuItem.addItem(user.name, VaadinIcons.SPECIALIST, i -> {
                    try {
                        calcForUser(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
            header.addComponent(menuBar);

            panel.addStyleName(ValoTheme.PANEL_WELL);
            panel.setSizeFull();
            VerticalLayout vLayout = new VerticalLayout(header);
            vLayout.addComponentsAndExpand(panel);
            vLayout.setSizeFull();
            addComponent(vLayout);

            setSizeFull();

            // Find the application directory
            String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
            FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/logo_login.png"));
            logoFile = resource.getSourceFile();
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void calcForUser( GpapUsers user ) throws Exception {

        Dao<Notes, ? > notesDAO = GssDbProvider.INSTANCE.getDatabaseHandler().get().getDao(Notes.class);
        List<Notes> allNotes;
        if (user == null) {
            allNotes = notesDAO.queryForAll();
        } else {
            allNotes = notesDAO.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user).query();
        }

        if (allNotes.isEmpty()) {
            Notification.show("No notes available for the PDF export.", Notification.Type.WARNING_MESSAGE);
            return;
        }

        Notification.show("Report generation started", "You'll be notified once the report is ready.",
                Notification.Type.TRAY_NOTIFICATION);
        reportsMenuItem.setEnabled(false);

        File tmpFolder = KukuratusWorkspace.getInstance().getTmpFolder().get();
        File outFile = new File(tmpFolder,
                "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ".pdf");

        new Thread(() -> {
            try {
                createPdf(allNotes, outFile);
                FileInputStream fileInputStream = new FileInputStream(outFile);
                getUI().access(() -> {
                    Embedded pdf = new Embedded(null, new FileResource(outFile));
                    pdf.setMimeType("application/pdf");
                    pdf.setType(Embedded.TYPE_BROWSER);
                    pdf.setSizeFull();
                    panel.setContent(pdf);

                    Button button = new Button("Download PDF", VaadinIcons.DOWNLOAD_ALT);
                    button.addStyleName(ValoTheme.BUTTON_PRIMARY);
                    header.addComponent(button);

                    FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
                        header.removeComponent(button);
                        reportsMenuItem.setEnabled(true);
                        return fileInputStream;
                    }, "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ".pdf"));
                    downloader.extend(button);

                    Notification.show("PDF ready for download", Notification.Type.TRAY_NOTIFICATION);
                });
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        }).start();

    }

    private void createPdf( List<Notes> allNotes, File outFile ) throws DocumentException, FileNotFoundException, Exception {
        Document document = new Document();
        document.setMargins(36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outFile));
        document.open();

        String titleStr = "Geopaparazzi Survey Server PDF Export";
        document.addTitle(titleStr);
        document.addSubject("Geopaparazzi Survey Server PDF Export");
        document.addKeywords("geopaparazzi, export, notes");

        User user = RegistryHandler.INSTANCE.getUserByUniqueName(authenticatedUsername);
        document.addAuthor("User: " + user.getName());
        document.addCreator("Geopaparazzi  Survey Server - http://www.geopaparazzi.eu");

        if (!logoFile.exists())
            logoFile = new File(
                    "/home/hydrologis/development/geopaparazzi-survey-server/kukuratus-server/gss-kukuratus/src/main/resources/images/logo_login.png");
        if (logoFile.exists()) {
            try {
                addEmptyLine(document);
                addEmptyLine(document);
                addEmptyLine(document);
                Image logo = Image.getInstance(logoFile.getAbsolutePath());
                document.add(logo);
                addEmptyLine(document);
                addEmptyLine(document);
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        } else {
            System.err.println("Not using missing logo file: " + logoFile);
            addEmptyLine(document);
            addEmptyLine(document);
            addEmptyLine(document);
            addEmptyLine(document);
            addEmptyLine(document);
            addEmptyLine(document);
            addEmptyLine(document);
        }

        Paragraph title = new Paragraph(new Phrase(10f, titleStr, FontFactory.getFont(FontFactory.COURIER, 20)));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        addEmptyLine(document);
        Paragraph author = new Paragraph(
                new Phrase(10f, "Author: " + user.getName(), FontFactory.getFont(FontFactory.COURIER, 10)));
        author.setAlignment(Element.ALIGN_CENTER);
        document.add(author);
        Paragraph dateTime = new Paragraph(
                new Phrase(10f, "Date: " + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMM),
                        FontFactory.getFont(FontFactory.COURIER, 10)));
        dateTime.setAlignment(Element.ALIGN_CENTER);
        document.add(dateTime);
        document.newPage();

        int index = 1;
        for( Notes note : allNotes ) {
            processNote(document, note, index++);
        }

        document.close();
    }

    private void addEmptyLine( Document document ) throws DocumentException {
        document.add(new Paragraph(" "));
    }

    public void processNote( Document document, Notes note, int count ) throws Exception {
        String form = note.getForm();
        if (form != null && form.length() > 0) {
            JSONObject sectionObject = new JSONObject(form);
            if (!sectionObject.has(Utilities.ATTR_SECTIONNAME)) {
                return;
            }
            String sectionName = sectionObject.getString(Utilities.ATTR_SECTIONNAME);
            Anchor anchor = new Anchor(sectionName);
            anchor.setName(sectionName);
            Chapter currentChapter = new Chapter(new Paragraph(anchor), count);
            addEmptyLine(currentChapter, 3);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setHeaderRows(0);
            infoTable.setWidthPercentage(90);
            currentChapter.add(infoTable);

            addKeyValueToTableRow(infoTable, "Timestamp", new Date(note.timestamp).toString());
            addKeyValueToTableRow(infoTable, "Latitude", note.the_geom.getY() + "");
            addKeyValueToTableRow(infoTable, "Longitude", note.the_geom.getX() + "");

            addEmptyLine(currentChapter, 3);

            List<String> formsNames = Utilities.getFormNames4Section(sectionObject);
            for( String formName : formsNames ) {
                Paragraph section = new Paragraph(formName);
                currentChapter.addSection(section);
                addEmptyLine(currentChapter, 3);

                PdfPTable currentTable = new PdfPTable(2);
                currentTable.setHeaderRows(1);
                currentTable.setWidthPercentage(90);
                currentChapter.add(currentTable);

                JSONObject form4Name = Utilities.getForm4Name(formName, sectionObject);
                JSONArray formItems = Utilities.getFormItems(form4Name);
                for( int i = 0; i < formItems.length(); i++ ) {
                    JSONObject formItem = formItems.getJSONObject(i);
                    if (!formItem.has(Utilities.TAG_KEY)) {
                        continue;
                    }

                    String type = formItem.getString(Utilities.TAG_TYPE);
                    String key = formItem.getString(Utilities.TAG_KEY);
                    String value = formItem.getString(Utilities.TAG_VALUE);

                    String label = key;
                    if (formItem.has(Utilities.TAG_LABEL)) {
                        label = formItem.getString(Utilities.TAG_LABEL);
                    }

                    if (type.equals(Utilities.TYPE_PICTURES)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(Utilities.IMAGES_SEPARATOR);
                        for( String imageId : imageIdsSplit ) {
                            if (imageId != null && imageId.trim().length() > 0) {

                                Images images = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                                ImageData imageData = imageDataDAO.queryForSameId(images.imageData);

                                String imgName = images.text;
                                byte[] imageDataArray = imageData.data;
                                Image itextImage = Image.getInstance(imageDataArray);
                                Paragraph caption = new Paragraph(imgName);
                                caption.setAlignment(Element.ALIGN_CENTER);

                                PdfPCell keyCell = new PdfPCell(new Phrase(label));
                                keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                keyCell.setPadding(10);
                                currentTable.addCell(keyCell);
                                PdfPCell valueCell = new PdfPCell();
                                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                valueCell.setPadding(10);
                                valueCell.addElement(itextImage);
                                valueCell.addElement(caption);
                                currentTable.addCell(valueCell);
                            }
                        }
                    } else if (type.equals(Utilities.TYPE_MAP)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String imageId = value.trim();
                        if (imageId != null && imageId.trim().length() > 0) {
                            Images images = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                            ImageData imageData = imageDataDAO.queryForSameId(images.imageData);

                            String imgName = images.text;
                            byte[] imageDataArray = imageData.data;
                            Image itextImage = Image.getInstance(imageDataArray);
                            Paragraph caption = new Paragraph(imgName);
                            caption.setAlignment(Element.ALIGN_CENTER);

                            PdfPCell keyCell = new PdfPCell(new Phrase(label));
                            keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            keyCell.setPadding(10);
                            currentTable.addCell(keyCell);
                            PdfPCell valueCell = new PdfPCell();
                            valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            valueCell.setPadding(10);
                            valueCell.addElement(itextImage);
                            valueCell.addElement(caption);
                            currentTable.addCell(valueCell);
                        }
                    } else if (type.equals(Utilities.TYPE_SKETCH)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(Utilities.IMAGES_SEPARATOR);
                        for( String imageId : imageIdsSplit ) {
                            if (imageId != null && imageId.trim().length() > 0) {
                                Images images = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                                ImageData imageData = imageDataDAO.queryForSameId(images.imageData);

                                String imgName = images.text;
                                byte[] imageDataArray = imageData.data;
                                Image itextImage = Image.getInstance(imageDataArray);

                                Paragraph caption = new Paragraph(imgName);
                                caption.setAlignment(Element.ALIGN_CENTER);

                                PdfPCell keyCell = new PdfPCell(new Phrase(label));
                                keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                keyCell.setPadding(10);
                                currentTable.addCell(keyCell);
                                PdfPCell valueCell = new PdfPCell();
                                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                valueCell.setPadding(10);
                                valueCell.addElement(itextImage);
                                valueCell.addElement(caption);
                                currentTable.addCell(valueCell);
                            }
                        }
                    } else {
                        addKeyValueToTableRow(currentTable, label, value);
                    }
                }
            }

            document.add(currentChapter);
            document.newPage();

        } else {
            String sectionName = note.text;
            Anchor anchor = new Anchor(sectionName);
            anchor.setName(sectionName);
            Chapter currentChapter = new Chapter(new Paragraph(anchor), count);
            addEmptyLine(currentChapter, 3);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setHeaderRows(0);
            infoTable.setWidthPercentage(90);
            currentChapter.add(infoTable);

            addKeyValueToTableRow(infoTable, "Timestamp", new Date(note.timestamp).toString());
            addKeyValueToTableRow(infoTable, "Latitude", note.the_geom.getY() + "");
            addKeyValueToTableRow(infoTable, "Longitude", note.the_geom.getX() + "");

            addEmptyLine(currentChapter, 3);
            document.add(currentChapter);
            document.newPage();
        }
    }

    private void addKeyValueToTableRow( PdfPTable table, String key, String value ) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key));
        keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        keyCell.setPadding(10);
        table.addCell(keyCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value));
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setPadding(10);
        table.addCell(valueCell);
    }

    public void addEmptyLine( Chapter element, int number ) throws DocumentException {
        for( int i = 0; i < number; i++ ) {
            Paragraph p = new Paragraph(" ");
            element.add(p);
        }
    }

}
