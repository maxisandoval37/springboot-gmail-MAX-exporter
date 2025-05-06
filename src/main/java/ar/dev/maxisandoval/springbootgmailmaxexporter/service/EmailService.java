package ar.dev.maxisandoval.springbootgmailmaxexporter.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.search.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${gmail.imap.host}")
    private String imapHost;

    @Value("${gmail.imap.protocol}")
    private String imapProtocol;

    @Value("${gmail.email}")
    private String email;

    @Value("${gmail.password}")
    private String password;

    public ByteArrayInputStream downloadEmailsAsExcel(LocalDate start, LocalDate end) throws MessagingException, IOException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", imapProtocol);

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(imapProtocol);
        store.connect(imapHost, email, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE, java.sql.Date.valueOf(start));
        SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LE, java.sql.Date.valueOf(end));
        SearchTerm dateRange = new AndTerm(newerThan, olderThan);

        Message[] messages = inbox.search(dateRange);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String sheetName = "Emails_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss"));
            Sheet sheet = workbook.createSheet(sheetName);

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Fecha");
            header.createCell(1).setCellValue("De");
            header.createCell(2).setCellValue("Asunto");

            int rowIdx = 1;
            for (Message message : messages) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(message.getReceivedDate().toString());
                row.createCell(1).setCellValue(message.getFrom()[0].toString());
                row.createCell(2).setCellValue(message.getSubject());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            inbox.close(false);
            store.close();
        }
    }
}