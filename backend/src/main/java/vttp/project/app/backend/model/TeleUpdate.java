package vttp.project.app.backend.model;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeleUpdate {

    private Integer id;
    private String user;
    private String text;

    public static TeleUpdate fromPayload(String payload) {
        JsonObject jObj = Json.createReader(new StringReader(payload)).readObject().getJsonObject("message");
        return new TeleUpdate(
                jObj.getJsonObject("from").getInt("id"), 
                jObj.getJsonObject("from").getString("username"), 
                jObj.getString("text").toLowerCase().trim());
    }

}
