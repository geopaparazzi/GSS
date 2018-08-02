package com.hydrologis.gss.server.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;

import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.Notes;
import com.j256.ormlite.dao.Dao;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

public class PdfExportView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    private HorizontalLayout header = new HorizontalLayout();
    private Panel panel = new Panel();

    private MenuBar.MenuItem reportsMenuItem;

    @Override
    public void enter( ViewChangeEvent event ) {
        MenuBar menuBar = new MenuBar();
        menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

        reportsMenuItem = menuBar.addItem("Surveyors", VaadinIcons.SPECIALIST, null);

        try {
            Dao<GpapUsers, ? > usersDAO = GssDbProvider.INSTANCE.getDatabaseHandler().get().getDao(GpapUsers.class);
            List<GpapUsers> users = usersDAO.queryForAll();
            reportsMenuItem.addItem("ALL", VaadinIcons.GROUP, i -> calcForUser(null));
            users.forEach(user -> {
                reportsMenuItem.addItem(user.name, VaadinIcons.SPECIALIST, i -> calcForUser(user));
            });
            header.addComponent(menuBar);

            panel.addStyleName(ValoTheme.PANEL_WELL);
            panel.setSizeFull();
            VerticalLayout vLayout = new VerticalLayout(header);
            vLayout.addComponentsAndExpand(panel);
            vLayout.setSizeFull();
            addComponent(vLayout);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void calcForUser( GpapUsers user ) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DynamicReportBuilder b = new FastReportBuilder();
            b.addAutoText("Surveyor: " + user.name + " Device: " + user.deviceId, AutoText.POSITION_HEADER,
                    AutoText.ALIGMENT_LEFT, 400);
            b.addAutoText(LocalDateTime.now().toString(), AutoText.POSITION_HEADER, AutoText.ALIGMENT_RIGHT, 200);
            b.setUseFullPageWidth(true);
//                .setPrintBackgroundOnOddRows(true)
            b.setTitle("Notes");
            

            b.addColumn(ColumnBuilder.getNew().setColumnProperty("altimetry", Double.class).setTitle("Elevation")
                    .setTextFormatter(new DecimalFormat("0.0")).build());
            b.addColumn(ColumnBuilder.getNew().setColumnProperty("text", String.class).setTitle("Note").build());
            DynamicReport report = b.build();
            try {
                Dao<Notes, ? > notesDAO = GssDbProvider.INSTANCE.getDatabaseHandler().get().getDao(Notes.class);
                List<Notes> allNotes = notesDAO.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user).query();

                JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(report, new ClassicLayoutManager(), allNotes);

                HtmlExporter exporter = new HtmlExporter();
                exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.exportReport();

                outputStream.flush();
                Label htmlLabel = new Label("", ContentMode.HTML);
                htmlLabel.setValue(outputStream.toString("UTF-8"));
                htmlLabel.setSizeFull();
                VerticalLayout vl = new VerticalLayout(htmlLabel);
                vl.setSizeFull();
                panel.setContent(vl);

                Notification.show("Report generation started", "You'll be notified once the report is ready.",
                        Notification.Type.TRAY_NOTIFICATION);
                reportsMenuItem.setEnabled(false);

                new Thread(() -> {
                    try {
                        JRPdfExporter pdfExporter = new JRPdfExporter();
                        pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                        pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                        pdfExporter.exportReport();
                        outputStream.flush();
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                        getUI().access(() -> {
                            Button button = new Button("Download PDF", VaadinIcons.DOWNLOAD_ALT);
                            button.addStyleName(ValoTheme.BUTTON_PRIMARY);
                            header.addComponent(button);

                            FileDownloader downloader = new FileDownloader(new StreamResource(() -> {
                                header.removeComponent(button);
                                reportsMenuItem.setEnabled(true);
                                return inputStream;
                            }, "gss_export_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact)
                                    + ".pdf"));
                            downloader.extend(button);

                            Notification.show("PDF ready for download", Notification.Type.TRAY_NOTIFICATION);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

//
//            DaoImages daoImages = new DaoImages();
//            if (form != null && form.length() > 0) {
//                JSONObject sectionObject = new JSONObject(form);
//                if (!sectionObject.has(FormUtilities.ATTR_SECTIONNAME)) {
//                    return;
//                }
//                String sectionName = sectionObject.getString(FormUtilities.ATTR_SECTIONNAME);
//                Anchor anchor = new Anchor(sectionName);
//                anchor.setName(sectionName);
//                Chapter currentChapter = new Chapter(new Paragraph(anchor), count);
//                addEmptyLine(currentChapter, 3);
//
//                PdfPTable infoTable = new PdfPTable(2);
//                infoTable.setHeaderRows(0);
//                infoTable.setWidthPercentage(90);
//                currentChapter.add(infoTable);
//
//                addKeyValueToTableRow(infoTable, "Timestamp", new Date(note.getTimeStamp()).toString());
//                addKeyValueToTableRow(infoTable, "Latitude", note.getLat() + "");
//                addKeyValueToTableRow(infoTable, "Longitude", note.getLon() + "");
//
//                addEmptyLine(currentChapter, 3);
//
//                List<String> formsNames = TagsManager.getFormNames4Section(sectionObject);
//                for (String formName : formsNames) {
//                    Paragraph section = new Paragraph(formName);
//                    currentChapter.addSection(section);
//                    addEmptyLine(currentChapter, 3);
//
//                    PdfPTable currentTable = new PdfPTable(2);
//                    currentTable.setHeaderRows(1);
//                    currentTable.setWidthPercentage(90);
//                    currentChapter.add(currentTable);
//
//                    JSONObject form4Name = TagsManager.getForm4Name(formName, sectionObject);
//                    JSONArray formItems = TagsManager.getFormItems(form4Name);
//                    for (int i = 0; i < formItems.length(); i++) {
//                        JSONObject formItem = formItems.getJSONObject(i);
//                        if (!formItem.has(FormUtilities.TAG_KEY)) {
//                            continue;
//                        }
//
//                        String type = formItem.getString(FormUtilities.TAG_TYPE);
//                        String key = formItem.getString(FormUtilities.TAG_KEY);
//                        String value = formItem.getString(FormUtilities.TAG_VALUE);
//
//                        String label = key;
//                        if (formItem.has(FormUtilities.TAG_LABEL)) {
//                            label = formItem.getString(FormUtilities.TAG_LABEL);
//                        }
//
//                        if (type.equals(FormUtilities.TYPE_PICTURES)) {
//                            if (value.trim().length() == 0) {
//                                continue;
//                            }
//                            String[] imageIdsSplit = value.split(Note.IMAGES_SEPARATOR);
//                            for (String imageId : imageIdsSplit) {
//                                if (imageId != null && imageId.trim().length() > 0) {
//                                    Image image = daoImages.getImage(Long.parseLong(imageId));
//                                    String imgName = image.getName();
//                                    byte[] imageData = daoImages.getImageData(Long.parseLong(imageId));
//                                    com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(imageData);
//                                    Paragraph caption = new Paragraph(imgName);
//                                    caption.setAlignment(Element.ALIGN_CENTER);
//
//                                    PdfPCell keyCell = new PdfPCell(new Phrase(label));
//                                    keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                    keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                    keyCell.setPadding(10);
//                                    currentTable.addCell(keyCell);
//                                    PdfPCell valueCell = new PdfPCell();
//                                    valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                    valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                    valueCell.setPadding(10);
//                                    valueCell.addElement(itextImage);
//                                    valueCell.addElement(caption);
//                                    currentTable.addCell(valueCell);
//                                }
//                            }
//                        } else if (type.equals(FormUtilities.TYPE_MAP)) {
//                            if (value.trim().length() == 0) {
//                                continue;
//                            }
//                            String imageId = value.trim();
//                            if (imageId != null && imageId.trim().length() > 0) {
//                                Image image = daoImages.getImage(Long.parseLong(imageId));
//                                String imgName = image.getName();
//                                byte[] imageData = daoImages.getImageData(Long.parseLong(imageId));
//                                com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(imageData);
//                                Paragraph caption = new Paragraph(imgName);
//                                caption.setAlignment(Element.ALIGN_CENTER);
//
//                                PdfPCell keyCell = new PdfPCell(new Phrase(label));
//                                keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                keyCell.setPadding(10);
//                                currentTable.addCell(keyCell);
//                                PdfPCell valueCell = new PdfPCell();
//                                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                valueCell.setPadding(10);
//                                valueCell.addElement(itextImage);
//                                valueCell.addElement(caption);
//                                currentTable.addCell(valueCell);
//                            }
//                        } else if (type.equals(FormUtilities.TYPE_SKETCH)) {
//                            if (value.trim().length() == 0) {
//                                continue;
//                            }
//                            String[] imageIdsSplit = value.split(Note.IMAGES_SEPARATOR);
//                            for (String imageId : imageIdsSplit) {
//                                if (imageId != null && imageId.trim().length() > 0) {
//                                    Image image = daoImages.getImage(Long.parseLong(imageId));
//                                    String imgName = image.getName();
//                                    byte[] imageData = daoImages.getImageData(Long.parseLong(imageId));
//                                    com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(imageData);
//                                    Paragraph caption = new Paragraph(imgName);
//                                    caption.setAlignment(Element.ALIGN_CENTER);
//
//                                    PdfPCell keyCell = new PdfPCell(new Phrase(label));
//                                    keyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                    keyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                    keyCell.setPadding(10);
//                                    currentTable.addCell(keyCell);
//                                    PdfPCell valueCell = new PdfPCell();
//                                    valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                                    valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//                                    valueCell.setPadding(10);
//                                    valueCell.addElement(itextImage);
//                                    valueCell.addElement(caption);
//                                    currentTable.addCell(valueCell);
//                                }
//                            }
//                        } else {
//                            addKeyValueToTableRow(currentTable, label, value);
//                        }
//                    }
//                }
//
//                document.add(currentChapter);
//                document.newPage();
//
//            }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JRException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
