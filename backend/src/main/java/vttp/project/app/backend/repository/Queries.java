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
            insert into orders(id, client_id, table_id, email, name, receipt, amount)
            value (?, ?, ?, ?, ?, ?, ?)
            """;
            
    public static final String SQL_SAVE_ORDER_RECEIPT = """
            update orders
            set receipt = ?
            where id = ?
            """;
            
    public static final String SQL_SAVE_ORDER_ITEMS = """
            insert into order_items(item_id, item_name, quantity, order_id)
            value (?, ?, ?, ?)
            """;

    public static final String SQL_GET_RECEIPT = """
            select email, receipt from orders
            where id = ?
            """;

    public static final String SQL_GET_ORDER_ITEMS = """
            select item_id, item_name, quantity from order_items
            where order_id = ?
            """;

    public static final String SQL_GET_ORDER_BY_CLIENT = """
            select * from orders
            where client_id = ?
            order by ordered_date
            """;
            
    public static final String SQL_GET_ORDER_BY_ID = """
            select * from orders where id = ?
            """;
            
    public static final String SQL_DELETE_ORDER_BY_ID = """
            delete from orders where id = ?
            """;
            
    public static final String SQL_DELETE_ORDER_ITEMS_BY_ID = """
            delete from order_items where order_id = ?
                    """;
            
     public static final String SQL_UPDATE_KITCHEN_STATUS = """
            update clients
            set status = ?
            where id = ?
            """;           
}