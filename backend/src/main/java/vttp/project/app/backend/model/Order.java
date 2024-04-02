package vttp.project.app.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String name;
    private Integer quantity;
    private Boolean completed;

    public Order(String id, String name, Integer quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public static Order[] fromJson(JsonArray array) {
        return array.stream()
                .map(value -> {
                    JsonObject jObject = value.asJsonObject();
                    return new Order(
                            jObject.getString("id"),
                            jObject.getString("name"),
                            jObject.getInt("quantity"));
                })
                .toArray(size -> new Order[size]);
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", this.id)
                .add("name", this.name)
                .add("quantity", this.quantity)
                .add("completed", this.completed != null ? this.completed : false)
                .build();
    }
}
