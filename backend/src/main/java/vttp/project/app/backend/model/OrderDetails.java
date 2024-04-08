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
public class OrderDetails {

    private String id;
    private String name;
    private Timestamp date;
    private String table;
    private String comments;
    private Double amount;
    private Order[] orders;
    private Boolean pending;

    public OrderDetails(String id, String name, Timestamp date, String table, String comments, Double amount) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.table = table;
        this.comments = comments;
        this.amount = amount;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("name", this.name)
                .add("date", this.date.getTime())
                .add("table", this.table)
                .add("comments", this.comments)
                .add("amount", this.amount)
                .add("orders", Json.createArrayBuilder(Arrays.stream(this.orders)
                        .map(value -> value.toJson())
                        .toList())
                        .build())
                .add("pending", this.pending)
                .build();
    }
}
