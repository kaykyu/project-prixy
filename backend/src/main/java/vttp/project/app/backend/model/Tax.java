package vttp.project.app.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tax {
    
    private Integer svcCharge;
    private Boolean gst;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("svc", this.svcCharge)
                .add("gst", this.gst)
                .build();
    }
}
