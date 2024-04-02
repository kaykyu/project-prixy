package vttp.project.app.backend.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import vttp.project.app.backend.model.Charge;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.Tax;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate sqlTemplate;

    public String getEstName(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_CLIENT, id);
        if (rs.next())
            return rs.getString("est_name");
        return null;
    }

    public Tax getTaxes(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_TAXES, id);
        if (rs.next())
            return new Tax(rs.getInt("service_charge"), rs.getBoolean("gst"));
        return null;
    }

    public Boolean saveCharge(Charge charge) {
        return sqlTemplate.update(Queries.SQL_SAVE_CHARGE, charge.getId(), charge.getPayment(),
                charge.getReceipt()) == 1;
    }

    public Boolean saveOrder(OrderRequest request) {
        return sqlTemplate.update(Queries.SQL_SAVE_ORDER, request.getId(), request.getClient(), request.getTable(),
                request.getEmail(), request.getName(), request.getComments(), request.getPaymentId(), request.getAmount()) == 1;
    }

    public Boolean saveOrderItems(OrderRequest request) {
        List<Boolean> saved = new ArrayList<>();

        for (Order order : request.getCart())
            saved.add(sqlTemplate.update(Queries.SQL_SAVE_ORDER_ITEMS, order.getId(), order.getName(),
                    order.getQuantity(), request.getId()) == 1);

        for (Boolean save : saved)
            if (save == false)
                return false;
        return true;
    }

    public String[] getReceiptUrl(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_RECEIPT, id);
        if (rs.next())
            return new String[] { rs.getString("email"), rs.getString("receipt") };
        return null;
    }

    public List<Order> getOrderItems(String id) {

        List<Order> orders = new ArrayList<>();
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_ITEMS, id);

        while (rs.next())
            orders.add(new Order(rs.getString("item_id"), rs.getString("item_name"), rs.getInt("quantity")));
        return orders;
    }

    public String getEmail(String payment) {
        
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_EMAIL, payment);
        if (rs.next())
            return rs.getString("email");
        return null;
    } 
}
