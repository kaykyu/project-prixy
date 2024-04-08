package vttp.project.app.backend.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import vttp.project.app.backend.model.Charge;
import vttp.project.app.backend.model.LineItem;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderDetails;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.OrderStatus;
import vttp.project.app.backend.model.Tax;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate sqlTemplate;

    @Resource(name = "redisReceipts")
    private ListOperations<String, LineItem> listOps;

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

    public Boolean saveOrder(OrderRequest request, OrderStatus status) {
        return sqlTemplate.update(Queries.SQL_SAVE_ORDER, request.getId(), request.getClient(), request.getTable(),
                request.getEmail(), request.getName(), request.getComments(), request.getPaymentId(),
                request.getAmount(), status.toString()) == 1;
    }

    public Boolean saveOrderItems(OrderRequest request) {
        List<Boolean> saved = new ArrayList<>();

        for (Order order : request.getCart())
            saved.add(sqlTemplate.update(Queries.SQL_SAVE_ORDER_ITEMS, order.getId(), order.getQuantity(),
                    request.getId()) == 1);

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

    public Order[] getOrderItems(String id) {

        List<Order> orders = new ArrayList<>();
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_ITEMS, id);

        while (rs.next())
            orders.add(new Order(rs.getString("id"), rs.getString("name"), rs.getInt("quantity")));
        return orders.toArray(new Order[0]);
    }

    public OrderDetails getOrderDetails(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_DETAILS, id);
        if (rs.next()) {
            OrderDetails details = new OrderDetails(rs.getString("id"), rs.getString("est_name"),
                    rs.getTimestamp("ordered_date"), rs.getString("table_id"), rs.getString("comments"),
                    rs.getDouble("amount"));
            
            details.setPending(rs.getString("status").equals(OrderStatus.PENDING.toString()));
            return details;
        }
        return null;
    }

    public Menu getMenu(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_MENU_BY_ID, id);
        if (rs.next())
            return new Menu(rs.getString("name"), rs.getDouble("price"));
        return null;
    }

    public String getChargeId(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_CHARGE_BY_ORDER, id);
        if (rs.next())
            return rs.getString("id");
        return null;
    }

    public void saveLineItems(String id, List<LineItem> items) {
        items.forEach(value -> listOps.rightPush(id, value));
    }
}
