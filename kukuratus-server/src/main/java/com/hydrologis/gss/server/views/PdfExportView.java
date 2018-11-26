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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gss.server.utils.Messages;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.registry.User;
import com.hydrologis.kukuratus.libs.reports.SimpleTablePdfReport;
import com.hydrologis.kukuratus.libs.spi.DbProvider;
import com.hydrologis.kukuratus.libs.spi.ExportPage;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class PdfExportView extends VerticalLayout implements View, ExportPage {
    private static final long serialVersionUID = 1L;

    private HorizontalLayout header = new HorizontalLayout();
    private Panel panel = new Panel();

    private MenuBar.MenuItem reportsMenuItem;

    private Dao<Images, ? > imagesDAO;

    private Dao<ImageData, ? > imageDataDAO;

    private String authenticatedUsername;

    @Override
    public void enter( ViewChangeEvent event ) {
        try {
            authenticatedUsername = AuthService.getAuthenticatedUsername();
            DbProvider dbProvider = SpiHandler.getDbProviderSingleton();
            DatabaseHandler dbHandler = dbProvider.getDatabaseHandler().get();
            imagesDAO = dbHandler.getDao(Images.class);
            imageDataDAO = dbHandler.getDao(ImageData.class);

            MenuBar menuBar = new MenuBar();
            menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

            reportsMenuItem = menuBar.addItem(Messages.getString("PdfExportView.surveyors"), VaadinIcons.SPECIALIST, null); //$NON-NLS-1$

            Dao<GpapUsers, ? > usersDAO = dbProvider.getDatabaseHandler().get().getDao(GpapUsers.class);
            List<GpapUsers> users = usersDAO.queryForAll();
            reportsMenuItem.addItem(Messages.getString("PdfExportView.ALL"), VaadinIcons.GROUP, i -> { //$NON-NLS-1$
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

        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    @SuppressWarnings("deprecation")
    private void calcForUser( GpapUsers user ) throws Exception {

        Dao<Notes, ? > notesDAO = SpiHandler.getDbProviderSingleton().getDatabaseHandler().get().getDao(Notes.class);
        List<Notes> allNotes;
        if (user == null) {
            allNotes = notesDAO.queryForAll();
        } else {
            allNotes = notesDAO.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user).query();
        }

        if (allNotes.isEmpty()) {
            Notification.show(Messages.getString("PdfExportView.no_notes"), Notification.Type.WARNING_MESSAGE); //$NON-NLS-1$
            return;
        }

        Notification.show(Messages.getString("PdfExportView.report_gen_started"), Messages.getString("PdfExportView.will_notify"), //$NON-NLS-1$ //$NON-NLS-2$
                Notification.Type.TRAY_NOTIFICATION);
        reportsMenuItem.setEnabled(false);

        File tmpFolder = KukuratusWorkspace.getInstance().getTmpFolder();
        File outFile = new File(tmpFolder,
                "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$

        new Thread(() -> {
            try {
                createPdf(allNotes, outFile);
                FileInputStream fileInputStream = new FileInputStream(outFile);
                getUI().access(() -> {
                    Embedded pdf = new Embedded(null, new FileResource(outFile));
                    pdf.setMimeType("application/pdf"); //$NON-NLS-1$
                    pdf.setType(Embedded.TYPE_BROWSER);
                    pdf.setSizeFull();
                    panel.setContent(pdf);

                    Button button = new Button(Messages.getString("PdfExportView.download_pdf"), VaadinIcons.DOWNLOAD_ALT); //$NON-NLS-1$
                    button.addStyleName(ValoTheme.BUTTON_PRIMARY);
                    header.addComponent(button);

                    FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
                        header.removeComponent(button);
                        reportsMenuItem.setEnabled(true);
                        return fileInputStream;
                    }, "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact) + ".pdf")); //$NON-NLS-1$ //$NON-NLS-2$
                    downloader.extend(button);

                    Notification.show(Messages.getString("PdfExportView.pdf_ready"), Notification.Type.TRAY_NOTIFICATION); //$NON-NLS-1$
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

        String titleStr = Messages.getString("PdfExportView.gss_pdf_export"); //$NON-NLS-1$
        document.addTitle(titleStr);
        document.addSubject(Messages.getString("PdfExportView.gss_pdf_export")); //$NON-NLS-1$
        document.addKeywords(Messages.getString("PdfExportView.keywords")); //$NON-NLS-1$

        User user = RegistryHandler.INSTANCE.getUserByUniqueName(authenticatedUsername);
        document.addAuthor(Messages.getString("PdfExportView.user") + user.getName()); //$NON-NLS-1$
        document.addCreator(Messages.getString("PdfExportView.gss_info")); //$NON-NLS-1$

        InputStream is = SimpleTablePdfReport.class.getClassLoader().getResourceAsStream("/images/logo_login.png"); //$NON-NLS-1$
        if (is != null) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while( (nRead = is.read(data, 0, data.length)) != -1 ) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] byteArray = buffer.toByteArray();

                addEmptyLine(document);
                addEmptyLine(document);
                addEmptyLine(document);
                Image logo = Image.getInstance(byteArray);
                logo.scalePercent(300 * 72f / 580);
                logo.setAlignment(Image.MIDDLE);
                document.add(logo);
                addEmptyLine(document);
                addEmptyLine(document);
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        } else {
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
        Paragraph author = new Paragraph(new Phrase(10f, Messages.getString("PdfExportView.author") + user.getName(), //$NON-NLS-1$
                FontFactory.getFont(FontFactory.COURIER, 10)));
        author.setAlignment(Element.ALIGN_CENTER);
        document.add(author);
        Paragraph dateTime = new Paragraph(new Phrase(10f,
                Messages.getString("PdfExportView.date") + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMM), //$NON-NLS-1$
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
        document.add(new Paragraph(" ")); //$NON-NLS-1$
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

            addKeyValueToTableRow(infoTable, "Timestamp", new Date(note.timestamp).toString()); //$NON-NLS-1$
            addKeyValueToTableRow(infoTable, "Latitude", note.the_geom.getY() + ""); //$NON-NLS-1$ //$NON-NLS-2$
            addKeyValueToTableRow(infoTable, "Longitude", note.the_geom.getX() + ""); //$NON-NLS-1$ //$NON-NLS-2$

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
                            try {
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
                            } catch (Exception e) {
                                KukuratusLogger.logError(this, e);
                            }
                        }
                    } else if (type.equals(Utilities.TYPE_MAP)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        try {
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
                        } catch (Exception e) {
                            KukuratusLogger.logError(this, e);
                        }
                    } else if (type.equals(Utilities.TYPE_SKETCH)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(Utilities.IMAGES_SEPARATOR);
                        for( String imageId : imageIdsSplit ) {
                            try {
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
                            } catch (Exception e) {
                                KukuratusLogger.logError(this, e);
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

            addKeyValueToTableRow(infoTable, "Timestamp", new Date(note.timestamp).toString()); //$NON-NLS-1$
            addKeyValueToTableRow(infoTable, "Latitude", note.the_geom.getY() + ""); //$NON-NLS-1$ //$NON-NLS-2$
            addKeyValueToTableRow(infoTable, "Longitude", note.the_geom.getX() + ""); //$NON-NLS-1$ //$NON-NLS-2$

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
            Paragraph p = new Paragraph(" "); //$NON-NLS-1$
            element.add(p);
        }
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.FILE_TEXT_O;
    }

    @Override
    public String getLabel() {
        return Messages.getString("PdfExportView.pdf_label"); //$NON-NLS-1$
    }

    @Override
    public String getPagePath() {
        return "pdfexport"; //$NON-NLS-1$
    }

    @Override
    public int getOrder() {
        return 1;
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
        return false;
    }
}
