package vttp.project.app.backend.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    @Value("${sendgrid.email.apikey}")
    private String sendgridKey;

    public void sendReceipt(String email, String url) throws IOException {
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> receipt = template.exchange(RequestEntity.get(url).build(), String.class);

        Email from = new Email("kaykyu8080@gmail.com", "Prixy");
        String subject = "Prixy Receipt";
        Email to = new Email(email);
        Content content = new Content("text/html", receipt.getBody());
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendgridKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);

        switch (response.getStatusCode()) {
            case 202:
                break;
            default:
                throw new IOException("Error sending receipt email.");
        }
    }
}
