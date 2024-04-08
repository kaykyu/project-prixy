package vttp.project.app.backend.model;

import java.io.Serializable;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItem implements Serializable {
    
    private String name;
    private Integer quantity;
    private Double amount;

    public LineItem(String name, Double amount) {
        this.name = name;
        this.quantity = 0;
        this.amount = amount;
    }

    public static Double getTotal(List<LineItem> list) {
        Double total = 0.0;
        for (LineItem item : list)
            total += item.getAmount();
        return total;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("name", this.name)
                .add("quantity", this.quantity)
                .add("amount", this.amount)
                .build();
    }

}
