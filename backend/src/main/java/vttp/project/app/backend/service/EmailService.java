package vttp.project.app.backend.service;

import java.io.IOException;
import java.util.logging.Logger;

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

import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.UserRole;

@Service
public class EmailService {

    @Value("${sendgrid.email.apikey}")
    private String sendgridKey;

    @Value("${webapp.host.url}")
    private String url;

    private Logger logger = Logger.getLogger(EmailService.class.getName());

    private final String PW_EMAIL = """
            The password for your %s account is <b>%s</b>. Please change it within 24 hours or the account will be deactivated.

            <hr>
            <i>This is an auto-generated email by Prixy. Do not reply to this email.</i>
            """;

    private final String RESET_PW = """
            The reset password for your account is <b>%s</b>. Please change it after you have logged in.

            <hr>
            <i>This is an auto-generated email by Prixy. Do not reply to this email.</i>
            """;

    public void sendReceipt(String email, String url) throws IOException {
        logger.info(">>> Sending receipt to %s...".formatted(email));

        RestTemplate template = new RestTemplate();
        ResponseEntity<String> receipt = template.exchange(RequestEntity.get(url).build(), String.class);

        Email from = new Email("kaykyu8080@gmail.com", "Prixy");
        String subject = "Prixy Receipt";
        Email to = new Email(email);
        Content content = new Content("text/html", receipt.getBody());
        Mail mail = new Mail(from, subject, to, content);

        sendEmail(mail);
    }

    public void sendPw(Client client, String pw, UserRole role) throws IOException {
        logger.info(">>> Sending verification email to %s...".formatted(client.getEmail()));

        Email from = new Email("kaykyu8080@gmail.com", "Prixy");
        String subject = "%s account for %s".formatted(role.toString(), client.getEstName());
        Email to = new Email(client.getEmail());
        Content content = new Content("text/html", PW_EMAIL.formatted(role.toString(), pw));
        Mail mail = new Mail(from, subject, to, content);

        sendEmail(mail);
    }

    public void sendResetPw(String email, String pw) throws IOException {
        logger.info(">>> Sending reset email to %s".formatted(email));

        Email from = new Email("kaykyu8080@gmail.com", "Prixy");
        String subject = "Password reset for Prixy";
        Email to = new Email(email);
        Content content = new Content("text/html", RESET_PW.formatted(pw));
        Mail mail = new Mail(from, subject, to, content);

        sendEmail(mail);
    }

    public void sendEmail(Mail mail) throws IOException {

        SendGrid sg = new SendGrid(sendgridKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);

        switch (response.getStatusCode()) {
            case 202:
                logger.info(">>> %s email sent".formatted(mail.getSubject()));
                break;
            default:
                throw new IOException("Error %d: sending %s".formatted(response.getStatusCode(), mail.getSubject()));
        }
    }
}
