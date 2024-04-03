drop database if exists prixy;

create database prixy;

use prixy;

create table clients (
    email varchar(128) not null,
    id char(8) not null,
    est_name varchar(128) not null,
    service_charge int default 0,
    gst boolean default false,

    primary key(email),
    key(id),
    key(est_name)
);

create table menu (
    id char(8) not null,
    name varchar(128) not null,
    description text,
    image text,
    price decimal(5,2) not null,   
    category ENUM('STARTER', 'MAIN', 'DESSERT', 'BEVERAGES', 'OTHERS') not null,
    client_id char(8) not null,

    primary key(id),
    constraint fk_clientId foreign key(client_id) references clients(id)
);

create table orders (
    id char(8) not null,
    client_id char(8) not null,
    ordered_date timestamp default current_timestamp,
    table_id varchar(8) not null, 
    email varchar(128) not null,
    name varchar(128),
    comments text,
    payment_id varchar(32),
    amount decimal(6,2) not null,
    progress int not null default 0,
    status ENUM('RECEIVED','IN_PROGRESS', 'OUT_FOR_DELIVERY') not null default 'RECEIVED',

   primary key(id),
   key (payment_id),
   constraint fk_client_id foreign key(client_id) references clients(id)
);

create table order_items (
    id int auto_increment,
    item_id char(8) not null,
    quantity int,
    order_id char(8) not null,
    completed boolean not null default false,

    primary key (id),
    key (item_id, order_id),
    constraint fk_item_id foreign key(item_id) references menu(id)
);

create table stripe_charge (
    id varchar(32),
    payment_id varchar(32),
    receipt text,

    primary key(id)
)