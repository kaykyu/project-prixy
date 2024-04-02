package vttp.project.app.backend.repository;

public class Queries {

    public static final String SQL_SAVE_CLIENT = """
            insert into clients(email, id, est_name)
            value (?, ?, ?)
            """;

    public static final String SQL_GET_CLIENT_ID = """
            select id from clients where email = ?
            """;

    public static final String SQL_GET_CLIENT = """
            select * from clients where id = ?
            """;
            
    public static final String SQL_UPDATE_EMAIL = """
            update clients
            set email = ?
            where id = ?
            """;
            
    public static final String SQL_UPDATE_CLIENT = """
            update clients
            set est_name = ?, service_charge = ?, gst = ?
            where id = ?
            """;

    public static final String SQL_GET_MENU = """
            select * from menu where client_id = ?
            order by category
            """;

    public static final String SQL_GET_MENU_CATEGORIES = """
            select distinct(category) from menu where client_id = ?
            order by category
            """;
            
    public static final String SQL_SAVE_MENU = """
            insert into menu(id, name, description, image, price, category, client_id)
            value (?, ?, ?, ?, ?, ?, ?)
            """;
            
    public static final String SQL_UPDATE_MENU = """
            update menu
            set name = ?, description = ?, price = ?, category = ?
            where id = ?
            """;
            
    public static final String SQL_DELETE_MENU = """
            delete from menu where id = ?
            """;
                        
    public static final String SQL_GET_TAXES = """
            select service_charge, gst from clients where id = ?
            """;

    public static final String SQL_SAVE_ORDER = """
            insert into orders(id, client_id, table_id, email, name, comments, payment_id, amount)
            value (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
    public static final String SQL_SAVE_ORDER_ITEMS = """
            insert into order_items(item_id, item_name, quantity, order_id)
            value (?, ?, ?, ?)
            """;

    public static final String SQL_GET_RECEIPT = """
            select orders.email, stripe_charge.receipt from orders
            inner join stripe_charge on orders.payment_id = stripe_charge.payment_id
            where orders.id = ?
            """;

    public static final String SQL_GET_ORDER_ITEMS = """
            select item_id, item_name, quantity, completed from order_items
            where order_id = ?
            """;

    public static final String SQL_GET_ORDER_BY_CLIENT = """
            select * from orders
            where client_id = ?
            order by ordered_date
            """;
            
    public static final String SQL_UPDATE_ORDER_PROGRESS = """
            update orders
            set progress = ?, status = ?
            where id = ?
            """;
            
     public static final String SQL_UPDATE_ORDER_ITEM_COMPLETED = """
            update order_items
            set completed = ?
            where order_id = ? and item_id = ?
            """;
            
     public static final String SQL_UPDATE_ORDER_ITEM_QUANTITY = """
            update order_items
            set quantity = ?
            where order_id = ? and item_id = ?
            """;
                                                
      public static final String SQL_DELETE_ORDER_ITEM = """
            delete from order_items
            where order_id = ? and item_id = ?
            """;                      
                       
    public static final String SQL_GET_ORDER_BY_ID = """
            select * from orders where id = ?
            """;
            
    public static final String SQL_GET_CHARGE_BY_PAYMENT = """
            select * from stripe_charge where payment_id = ?
            """;
            
    public static final String SQL_DELETE_ORDER_BY_ID = """
            delete from orders where id = ?
            """;
            
    public static final String SQL_DELETE_CHARGE = """
            delete from stripe_charge where id = ?
            """;
            
    public static final String SQL_DELETE_ORDER_ITEMS_BY_ID = """
            delete from order_items where order_id = ?
                    """;
                      
    public static final String SQL_GET_MENU_PRICE_BY_ID = """
            select id, price from menu where id = ?
            """;
            
    public static final String SQL_GET_ORDER_PROGRESS = """
            select progress, status from orders where id = ?
            """;
            
    public static final String SQL_SAVE_CHARGE = """
            insert into stripe_charge(id, payment_id, receipt)
            value (?, ?, ?)
            """;
            
    public static final String SQL_GET_CHARGE_BY_ORDER = """
            select stripe_charge.id from stripe_charge
            inner join orders on orders.payment_id = stripe_charge.payment_id
            where orders.id = ?
            """;
            
    public static final String SQL_GET_EMAIL = """
            select email from orders where payment_id = ?
            """;
}