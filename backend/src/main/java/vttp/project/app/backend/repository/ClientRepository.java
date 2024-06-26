package vttp.project.app.backend.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.CompletedOrder;
import vttp.project.app.backend.model.KitchenOrder;
import vttp.project.app.backend.model.LineItem;
import vttp.project.app.backend.model.Login;
import vttp.project.app.backend.model.Menu;
import vttp.project.app.backend.model.MenuCategory;
import vttp.project.app.backend.model.Order;
import vttp.project.app.backend.model.OrderStatus;
import vttp.project.app.backend.model.Tax;

@Repository
public class ClientRepository {

    @Autowired
    private JdbcTemplate sqlTemplate;

    @Autowired
    private RedisTemplate<String, LineItem> redisTemplate;

    @Resource(name = "redisReceipts")
    private ListOperations<String, LineItem> listOps;

    public Boolean signUp(Login login, String id) {
        return sqlTemplate.update(Queries.SQL_SAVE_CLIENT, login.getEmail(), id, login.getEstName()) > 0;
    }

    public String getClientId(String email) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_CLIENT_ID, email);
        if (rs.next())
            return rs.getString("id");
        return null;
    }

    public Client getClient(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_CLIENT, id);
        if (rs.next())
            return new Client(
                    id,
                    rs.getString("email"),
                    rs.getString("est_name"),
                    new Tax(rs.getInt("service_charge"), rs.getBoolean("gst")));
        return null;
    }

    public Boolean putClient(String id, Client client) {
        return sqlTemplate.update(Queries.SQL_UPDATE_CLIENT, client.getEstName(), client.getTax().getSvc(),
                client.getTax().getGst(), id) == 1;
    }

    public Boolean putEmail(String old, String change) throws DuplicateKeyException {
        return sqlTemplate.update(Queries.SQL_UPDATE_EMAIL, change, old) == 1;
    }
    
    public List<Menu> getMenu(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_MENU, id);
        List<Menu> result = new ArrayList<>();

        while (rs.next())
            result.add(new Menu(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("image"),
                    rs.getDouble("price"),
                    MenuCategory.valueOf(rs.getString("category"))));
        return result;
    }

    public List<String> getMenuCategories(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_MENU_CATEGORIES, id);
        List<String> result = new ArrayList<>();

        while (rs.next())
            result.add(rs.getString("category"));
        return result;
    }

    public Boolean saveMenu(Menu menu, String id) {
        return sqlTemplate.update(Queries.SQL_SAVE_MENU, menu.getId(), menu.getName(), menu.getDescription(),
                menu.getImage(), menu.getPrice(), menu.getCategory().toString(), id) == 1;
    }

    public Boolean putMenuImage(String id, String image) {
        return sqlTemplate.update(Queries.SQL_UPDATE_MENU_IMAGE, image, id) == 1;
    }

    public Boolean putMenu(Menu menu) {
        return sqlTemplate.update(Queries.SQL_UPDATE_MENU, menu.getName(), menu.getDescription(), menu.getPrice(),
                menu.getCategory().toString(), menu.getId()) == 1;
    }

    public Boolean deleteMenu(String id) {
        return sqlTemplate.update(Queries.SQL_DELETE_MENU, id) == 1;
    }

    public List<KitchenOrder> getKitchenOrders(String id) {

        List<KitchenOrder> orders = new ArrayList<>();
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_BY_CLIENT, id);

        while (rs.next())
            orders.add(new KitchenOrder(
                    rs.getString("id"),
                    rs.getString("table_id"),
                    rs.getTimestamp("ordered_date"),
                    rs.getString("comments"),
                    rs.getInt("progress"),
                    rs.getString("status")));

        return orders.stream()
                .map(value -> {
                    List<Order> list = new ArrayList<>();
                    SqlRowSet rs2 = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_ITEMS, value.getId());
                    while (rs2.next())
                        list.add(new Order(
                                rs2.getString("id"),
                                rs2.getString("name"),
                                rs2.getInt("quantity"),
                                rs2.getBoolean("completed")));
                    value.setOrders(list.toArray(new Order[0]));
                    return value;
                })
                .toList();
    }

    public List<LineItem> getLineItems(String id) {
        return listOps.range(id, 0, listOps.size(id));
    }

    public Boolean updateOrderStatus(String id, OrderStatus status) {        
        Boolean result = sqlTemplate.update(Queries.SQL_UPDATE_ORDER_STATUS, status.toString(), id) == 1;
        if (result)
            redisTemplate.delete(id);
        return result;
    }

    public CompletedOrder getOrder(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_COMPLETED_ORDER, id);
        if (rs.next()) {
            return new CompletedOrder(
                    rs.getString("id"),
                    rs.getString("client_id"),
                    rs.getTimestamp("ordered_date"),
                    rs.getString("table_id"),
                    rs.getString("email"),
                    rs.getString("name"),
                    rs.getString("comments"),
                    rs.getString("payment_id"),
                    rs.getString("charge_id"),
                    rs.getString("receipt"),
                    rs.getDouble("amount"));
        }
        return null;
    }

    public CompletedOrder getOrderItems(CompletedOrder order) {

        List<Order> orders = new ArrayList<>();
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_ITEMS, order.getId());

        while (rs.next())
            orders.add(new Order(rs.getString("id"), rs.getString("name"), rs.getInt("quantity")));

        order.setOrders(orders.toArray(new Order[0]));
        return order;
    }

    public Boolean updateOrderProgress(Integer progress, String id) {
        return sqlTemplate.update(Queries.SQL_UPDATE_ORDER_PROGRESS, progress, OrderStatus.IN_PROGRESS.toString(),
                id) == 1;
    }

    public Boolean updateOrderItem(String order, String item, Boolean completed) {
        return sqlTemplate.update(Queries.SQL_UPDATE_ORDER_ITEM_COMPLETED, completed, order, item) == 1;
    }

    public Boolean updateOrderItem(String order, String item, Integer quantity) {
        return sqlTemplate.update(Queries.SQL_UPDATE_ORDER_ITEM_QUANTITY, quantity, order, item) == 1;
    }

    public Boolean removeOrderItem(String order, String item) {
        return sqlTemplate.update(Queries.SQL_DELETE_ORDER_ITEM, order, item) == 1;
    }

    public Boolean removeOrder(String id) {
        return sqlTemplate.update(Queries.SQL_DELETE_ORDER_BY_ID, id) == 1;
    }

    public Boolean updateOrderAmount(String id, Double minus) {
        
        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_AMOUNT, id);
        if (!rs.next())
            return false;        
        return sqlTemplate.update(Queries.SQL_UPDATE_ORDER_AMOUNT, rs.getDouble("amount") - minus, id) == 1;
    }

    public Boolean removeOrderItems(String id) {
        return sqlTemplate.update(Queries.SQL_DELETE_ORDER_ITEMS_BY_ID, id) > 0;
    }

    public Boolean removeCharge(String id) {
        return sqlTemplate.update(Queries.SQL_DELETE_CHARGE, id) == 1;
    }

    public KitchenOrder getProgress(String id) {

        SqlRowSet rs = sqlTemplate.queryForRowSet(Queries.SQL_GET_ORDER_PROGRESS, id);
        if (rs.next())
            return new KitchenOrder(rs.getInt("progress"), OrderStatus.valueOf(rs.getString("status")));
        return null;
    }
}
