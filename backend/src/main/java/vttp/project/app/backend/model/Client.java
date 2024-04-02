package vttp.project.app.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    
    private String id;
    private String email;
    private String estName;
    private Tax tax;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("email", this.email)
                .add("estName", this.estName)
                .add("tax", this.tax.toJson())
                .build();
    }
}
