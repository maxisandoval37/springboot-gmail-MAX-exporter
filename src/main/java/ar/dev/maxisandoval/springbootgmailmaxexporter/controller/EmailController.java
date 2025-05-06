package ar.dev.maxisandoval.springbootgmailmaxexporter.controller;

import ar.dev.maxisandoval.springbootgmailmaxexporter.service.EmailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Controller
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/export")
    public void exportEmails(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                             @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                             HttpServletResponse response) throws Exception {

        ByteArrayInputStream excel = emailService.downloadEmailsAsExcel(start, end);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "emails_" + timestamp + ".xlsx";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        StreamUtils.copy(excel, response.getOutputStream());
    }
}