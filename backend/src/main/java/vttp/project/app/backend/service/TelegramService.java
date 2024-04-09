package vttp.project.app.backend.service;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import vttp.project.app.backend.model.KitchenOrder;
import vttp.project.app.backend.model.TeleUpdate;
import vttp.project.app.backend.repository.ClientRepository;

@Service
public class TelegramService {

    @Value("${telegram.api.key}")
    private String bot;

    @Autowired
    private ClientRepository clientRepo;

    private RestTemplate template = new RestTemplate();
    private Logger logger = Logger.getLogger(TelegramService.class.getName());

    private final String URL = "https://api.telegram.org/bot";
    public final String START = "Welcome to PrixyBot! \nTo get started, choose commands from Menu";
    public final String STATUS = """
            To check on order status, send message in format:
                    Check <OrderID>

            where OrderID contains 8 characters

            Eg. Check ABCD1234
            """;
    public final String ERROR = "Sorry, I don't understand your command :( \nPlease check instructions from /start";

    public Boolean handleTeleUpdate(TeleUpdate tele) {

        logger.info("Received text from >>>" + tele);
        switch (tele.getText().split(" ")[0].toLowerCase()) {
            case ("/start"):
                sendMessage(tele, START);
                break;

            case ("/status"):
                sendMessage(tele, STATUS);
                break;

            case ("check"):
                String msg = checkOrder(tele);
                if (msg != null)
                    sendMessage(tele, msg);
                else
                    sendMessage(tele, "OrderID not found \nPlease check that the format is correct");
                break;

            default:
                sendMessage(tele, ERROR);
        }
        return true;
    }

    public void sendMessage(TeleUpdate tele, String message) {

        String url = "%s%s/sendMessage?chat_id=%d&text=%s".formatted(URL, bot, tele.getId(), message);
        RequestEntity<Void> req = RequestEntity.get(url).build();
        ResponseEntity<String> resp = template.exchange(req, String.class);

        if (!Json.createReader(new StringReader(resp.getBody())).readObject().getBoolean("ok"))
            logger.warning("FAILED: %s".formatted(url));
    }

    public String checkOrder(TeleUpdate tele) {

        String[] msg = tele.getText().split(" ");
        if (msg.length < 2)
            return null;
        
        String id = msg[1];
        if (id.length() == 8) {
            KitchenOrder result = clientRepo.getProgress(id);
            if (result != null)
                switch (result.getStatus()) {
                    case PENDING:
                        return "Your order is pending for payment. Please proceed to the counter to make payment.";
                    
                    case RECEIVED:
                        return "The kitchen has received your order and is going to start preparing!";

                    case IN_PROGRESS:
                        return "Busy cooking up your order! (%d".formatted(result.getProgress()) + "% done)";
                }
        }
        return null;
    }
}
