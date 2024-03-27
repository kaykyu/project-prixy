package vttp.project.app.backend.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderRequest;
import vttp.project.app.backend.model.Tax;

@Repository
public class UserRepository {

    @Resource(name = "redisOrders")
    private ValueOperations<String, String> orderValueOps;

    @Autowired
    private JdbcTemplate sqlTemplate;

    public Tax getTaxes(String id) {
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_TAXES, id);
        if (rs.next())
            return new Tax(rs.getInt("service_charge"), rs.getBoolean("gst"));
        return null;
    }

    public void saveReceipt(String id, String url) {
        orderValueOps.set(id, url, Duration.ofDays(7));
    }

    public String getReceipt(String piId, String id) {
        String url = orderValueOps.get(piId);
        if (sqlTemplate.update(Queries.SQL_SAVE_ORDER_RECEIPT, url, id) == 1)
            return url;
        return null;
    }

    public Boolean saveOrder(OrderRequest request) {
        return sqlTemplate.update(Queries.SQL_SAVE_ORDER, request.getId(), request.getClient(), request.getTable(),
                request.getEmail(), request.getName(), request.getReceipt(), request.getAmount()) == 1;
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

    public OrderRequest getReceiptUrl(String id) {
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_RECEIPT, id);
        if (rs.next())
            return new OrderRequest(rs.getString("email"), rs.getString("receipt"));
        return null;
    }
    
    public List<Order> getOrderItems(String id) {
        List<Order> orders = new ArrayList<>();
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_ITEMS, id);

        while (rs.next()) 
            orders.add(new Order(rs.getString("item_id"), rs.getString("item_name"), rs.getInt("quantity")));        
        return orders;
    }
}
