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
    private Order[] pending;
    private Timestamp timestamp;
    private String comments;
    private Integer progress;
    private OrderStatus status;    

    public KitchenOrder(Integer progress, OrderStatus status) {
        this.progress = progress;
        this.status = status;
    }

    public KitchenOrder(String id, String table, Timestamp timestamp, String comments, Integer progress, String status) {
        this.id = id;
        this.table = table;
        this.timestamp = timestamp;
        this.comments = comments;
        this.progress = progress;
        this.status = OrderStatus.valueOf(status);
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("table", this.table)
                .add("orders", Json.createArrayBuilder(Arrays.stream(this.orders).map(value -> value.toJson()).toList()).build())
                .add("date", this.timestamp.toString())
                .add("comments", this.comments != null ? this.comments : "")
                .add("progress", this.progress)
                .add("status", this.status.toString())
                .build();
    }
}
