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
    
    private Integer svc;
    private Boolean gst;

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("svc", this.svc)
                .add("gst", this.gst)
                .build();
    }
}
