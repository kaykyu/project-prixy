package vttp.project.app.backend.model;

import java.sql.Timestamp;
import java.util.Arrays;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KitchenOrder {
    
    private String id;
    private String table;
    private Order[] orders;
    private Timestamp timestamp;

    public KitchenOrder(String id, String table, Timestamp timestamp) {
        this.id = id;
        this.table = table;
        this.timestamp = timestamp;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("table", this.table)
                .add("orders", Json.createArrayBuilder(Arrays.stream(this.orders).map(value -> value.toJson()).toList()).build())
                .add("date", this.timestamp.toString())
                .build();
    }
}
