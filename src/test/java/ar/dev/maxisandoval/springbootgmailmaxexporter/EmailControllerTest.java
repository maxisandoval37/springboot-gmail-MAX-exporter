package ar.dev.maxisandoval.springbootgmailmaxexporter;

import ar.dev.maxisandoval.springbootgmailmaxexporter.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Test
    void shouldReturnExcelFileWithCorrectHeaders() throws Exception {
        ByteArrayInputStream fakeExcel = new ByteArrayInputStream("Fake Excel Content".getBytes());
        when(emailService.downloadEmailsAsExcel(any(), any())).thenReturn(fakeExcel);

        mockMvc.perform(post("/export")
                        .param("startDate", "2025-05-01")
                        .param("endDate", "2025-05-05"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("emails_")))
                .andExpect(content().contentType("application/octet-stream"));
    }
}