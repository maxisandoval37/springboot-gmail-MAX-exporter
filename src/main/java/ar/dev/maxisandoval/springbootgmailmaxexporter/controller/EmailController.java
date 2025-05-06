package ar.dev.maxisandoval.springbootgmailmaxexporter.controller;

import ar.dev.maxisandoval.springbootgmailmaxexporter.service.EmailService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.mail.AuthenticationFailedException;
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
    public ResponseEntity<?> exportEmails(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                          @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            return buildExcelResponse(emailService.downloadEmailsAsExcel(start, end));
        } catch (AuthenticationFailedException e) {
            return buildErrorResponse("Credenciales inválidas. Verificá tu email y contraseña.", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return buildErrorResponse("Error al procesar la solicitud.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<InputStreamResource> buildExcelResponse(ByteArrayInputStream excel) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=emails_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(excel));
    }

    private ResponseEntity<String> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(message);
    }
}