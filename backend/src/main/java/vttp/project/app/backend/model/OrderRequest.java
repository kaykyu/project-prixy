package vttp.project.app.backend.model;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String client;
    private String table;
    private Order[] cart;
    private Double amount;
    private String id;
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String comments;
    private String paymentId;
    private String token;

    public OrderRequest(String client, String table, Order[] cart, String id, String name, String email,
            String comments) {
        this.client = client;
        this.table = table;
        this.cart = cart;
        this.id = id;
        this.name = name;
        this.email = email;
        this.comments = comments;
    }

    public Map<String, String> toMetadata() {
        Map<String, String> map = new HashMap<>();

        map.put("client", this.client);
        map.put("table", this.table);
        map.put("cart", Json.createArrayBuilder(
                Arrays.stream(this.cart)
                        .map(value -> Json.createObjectBuilder()
                                .add("id", value.getId())
                                .add("quantity", value.getQuantity())
                                .build())
                        .toList())
                .build().toString());
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("email", this.email);
        map.put("comments", this.comments);

        return map;
    }

    public static OrderRequest fromMap(Map<String, String> map) {
        return new OrderRequest(
                map.get("client"),
                map.get("table"),
                Order.fromMetadata(Json.createReader(new StringReader(map.get("cart"))).readArray()),
                map.get("id"),
                map.get("name"),
                map.get("email"),
                map.get("comments"));
    }
}
