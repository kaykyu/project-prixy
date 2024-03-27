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
    private Integer svcCharge;
    private Boolean gst;
    private Boolean status;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("email", this.email)
                .add("estName", this.estName)
                .add("svcCharge", this.svcCharge != null ? this.svcCharge : 0)
                .add("gst", this.gst != null ? this.gst : false)
                .add("status", this.status)
                .build();
    }
}
