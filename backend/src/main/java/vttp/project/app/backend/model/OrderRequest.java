package vttp.project.app.backend.model;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.json.Json;
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
    private String email;
    private String receipt;

    public OrderRequest(String email, String receipt) {
        this.email = email;
        this.receipt = receipt;
    }

    public OrderRequest(Order[] cart, Double amount, String name, String email) {
        this.cart = cart;
        this.amount = amount;
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.email = email;
    }

    public OrderRequest(String client, String table, Order[] cart, Double amount, String id, String name, String email) {
        this.client = client;
        this.table = table;
        this.cart = cart;
        this.amount = amount;
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Map<String, String> toMetadata() {
        Map<String, String> map = new HashMap<>();

        map.put("client", this.client);
        map.put("table", this.table);
        map.put("cart", Json.createArrayBuilder(
                Arrays.stream(this.cart)
                        .map(value -> value.toJson())
                        .toList())
                .build().toString());
        map.put("amount", this.amount.toString());
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("email", this.email);

        return map;
    }

    public static OrderRequest fromMap(Map<String, String> map) {
        return new OrderRequest(
                map.get("client"),
                map.get("table"),
                Order.fromJson(Json.createReader(new StringReader(map.get("cart"))).readArray()),
                Double.parseDouble(map.get("amount")),
                map.get("id"),
                map.get("name"),
                map.get("email"));
    }

    public Long getLongAmount() {
        Double amt = this.amount * 100;
        return amt.longValue();
    }
}
