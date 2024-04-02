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
    private String payment;
    private String charge;
    private String receipt;
    private Double amount;
    private Order[] orders;

    public CompletedOrder(String id, String clientId, Timestamp orderedDate, String table, String email, String name,
            String payment, Double amount) {
        this.id = id;
        this.clientId = clientId;
        this.orderedDate = orderedDate;
        this.table = table;
        this.email = email;
        this.name = name;
        this.payment = payment;
        this.amount = amount;
    }

    public Document toDoc() {
        return new Document("id", this.id)
                .append("client_id", this.clientId)
                .append("ordered_date", this.orderedDate)
                .append("table_id", this.table)
                .append("email", this.email)
                .append("name", this.name)
                .append("payment_id", this.payment)
                .append("charge_id", this.getCharge())
                .append("receipt", this.receipt)
                .append("amount", this.amount)
                .append("orders", Arrays.stream(this.orders)
                        .map(order -> Document.parse(order.toJson().toString()))
                        .toList());
    }
}
