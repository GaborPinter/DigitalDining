package com.DigitalDining.demo.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.model.WeeklyMenuDayView;
import com.DigitalDining.demo.model.WeeklyMenuItemView;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class WeeklyMenuPdfService {
	
	private final UserService userService;
    private final JavaMailSender mailSender;
    private final WeeklyMenuService weeklyMenuService;

    public WeeklyMenuPdfService(UserService userService,
                                JavaMailSender mailSender,
                                WeeklyMenuService weeklyMenuService) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.weeklyMenuService = weeklyMenuService;
    }
    
    public void sendWeeklyMenuPdfByEmail(String username) throws MessagingException {
        UserResponse user = userService.findByUsername(username);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("A felhasználó e-mail címe nem található.");
        }

        List<WeeklyMenuDayView> days = weeklyMenuService.getNextSevenDays();
        byte[] pdf = generatePdf(days);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(user.getEmail());
        helper.setSubject("Heti menü");
        helper.setText("Szia!\n\nCsatolva küldjük az aktuális heti menüt PDF formátumban.\n\nÜdv,\nDigital Dining");

        helper.addAttachment("heti-menu.pdf", new ByteArrayResource(pdf));

        mailSender.send(message);
    }

	public byte[] generatePdf(List<WeeklyMenuDayView> days) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 22, 22, 24, 22);
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = loadUnicodeFont();
            Font titleFont = createTitleFont(baseFont);
            Font subtitleFont = createSubtitleFont(baseFont);
            Font dayFont = createDayFont(baseFont);
            Font cellFont = createCellFont(baseFont);
            Font headerFont = createHeaderFont(baseFont);

            Paragraph title = new Paragraph("Heti menü", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);

            Paragraph subtitle = new Paragraph("A hét napjai és az A/B/C opciók táblázatos bontásban", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(12);
            document.add(subtitle);

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy. MMMM d.", new Locale("hu", "HU"));

            for (WeeklyMenuDayView day : days) {
                Paragraph dayTitle = new Paragraph(day.dayName() + " · " + day.menuDate().format(df), dayFont);
                dayTitle.setSpacingBefore(10);
                dayTitle.setSpacingAfter(6);
                document.add(dayTitle);

                if (day.subtitle() != null && !day.subtitle().isBlank()) {
                    Paragraph info = new Paragraph(day.subtitle(), subtitleFont);
                    info.setSpacingAfter(6);
                    document.add(info);
                }

                PdfPTable table = new PdfPTable(new float[]{1.1f, 2.8f, 1.0f, 2.8f, 1.0f, 2.8f, 1.0f});
                table.setWidthPercentage(100);
                table.setSpacingAfter(10);

                addHeaderCell(table, "Kategória", headerFont);
                addHeaderCell(table, "A opció", headerFont);
                addHeaderCell(table, "Ár", headerFont);
                addHeaderCell(table, "B opció", headerFont);
                addHeaderCell(table, "Ár", headerFont);
                addHeaderCell(table, "C opció", headerFont);
                addHeaderCell(table, "Ár", headerFont);

                addCategoryRow(table, "Leves", day.soups(), cellFont);
                addCategoryRow(table, "Főétel", day.mains(), cellFont);
                addCategoryRow(table, "Desszert", day.desserts(), cellFont);

                document.add(table);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("A PDF generálása sikertelen", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(235, 235, 235));
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private void addCategoryRow(PdfPTable table, String categoryName, List<WeeklyMenuItemView> items, Font cellFont) {
        table.addCell(bodyCell(categoryName, cellFont, true));

        String[] labels = {"A", "B", "C"};
        for (int i = 0; i < 3; i++) {
            WeeklyMenuItemView item = i < items.size() ? items.get(i) : null;
            table.addCell(bodyCell(item != null ? labels[i] + " · " + item.name() : "—", cellFont, false));
            table.addCell(bodyCell(item != null ? item.price().toPlainString() + " Ft" : "—", cellFont, false));
        }
    }

    private PdfPCell bodyCell(String text, Font font, boolean bold) {
        Font useFont = bold ? new Font(font.getBaseFont(), font.getSize(), Font.BOLD) : font;
        PdfPCell cell = new PdfPCell(new Phrase(text, useFont));
        cell.setPadding(7f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private BaseFont loadUnicodeFont() throws IOException, DocumentException {
        List<String> candidates = List.of(
                "src/main/resources/fonts/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/gentiumplus/GentiumPlus-Regular.ttf",
                "/usr/share/fonts/truetype/roboto/unhinted/RobotoTTF/Roboto-Regular.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"
        );

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.exists(path) && Files.isRegularFile(path) && Files.size(path) > 0) {
                return BaseFont.createFont(
                        path.toString(),
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                );
            }
        }

        throw new FileNotFoundException(
                "Nem található használható Unicode font. " +
                "Tedd be a fontot a src/main/resources/fonts mappába, " +
                "vagy telepíts fonts-dejavu-core / fonts-liberation / fonts-noto-core csomagot."
        );
    }
    
    private Font createTitleFont(BaseFont baseFont) {
        return new Font(baseFont, 20, Font.BOLD);
    }

    private Font createSubtitleFont(BaseFont baseFont) {
        return new Font(baseFont, 11, Font.NORMAL);
    }

    private Font createDayFont(BaseFont baseFont) {
        return new Font(baseFont, 14, Font.BOLD);
    }

    private Font createCellFont(BaseFont baseFont) {
        return new Font(baseFont, 10, Font.NORMAL);
    }

    private Font createHeaderFont(BaseFont baseFont) {
        return new Font(baseFont, 10, Font.BOLD);
    }
}
