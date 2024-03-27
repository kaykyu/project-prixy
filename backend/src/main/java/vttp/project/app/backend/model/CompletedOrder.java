package vttp.project.app.backend.model;

import java.sql.Timestamp;
import java.util.Arrays;

import org.bson.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletedOrder {

    private String id;
    private String clientId;
    private Timestamp orderedDate;
    private String table;
    private String email;
    private String name;
    private String receipt;
    private Double amount;
    private Order[] orders;

    public Document toDoc() {
        return new Document("id", this.id)
                .append("client_id", this.clientId)
                .append("ordered_date", this.orderedDate)
                .append("table_id", this.table)
                .append("email", this.email)
                .append("name", this.name)
                .append("receipt", this.receipt)
                .append("amount", this.amount)
                .append("orders", Arrays.stream(this.orders)
                        .map(order -> Document.parse(order.toJson().toString()))
                        .toList());
    }
}
