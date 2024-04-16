export interface Login {
    email: string
    pw: string
    change?: string
}

export interface Auth {
    iss: string
    sub: string
    email: string
    exp: number
    role: string
}

export interface Client {
    id: string
    email: string
    estName: string
    tax: Tax
}

export interface ClientSlice {
    client: Client
    orders: KitchenOrder[],
    pending: KitchenOrder[],
    menu: Menu[],
    admin: boolean
}

export interface Tax {
    svc: number
    gst: boolean
}

export enum MenuCategory {
    'STARTER', 'MAIN', 'DESSERT', 'BEVERAGES', 'OTHERS'
}

export interface Menu {
    id: string
    name: string
    description: string
    image: string
    price: number
    category: MenuCategory
    quantity?: number
}

export interface Order {
    id: string
    name: string
    price: number
    quantity: number
    completed?: boolean
}

export interface User {
    iss: string
    sub: string
    client: string
    table: string
    exp: number
}

export interface UserSlice {
    user: User
    menu: Menu[]
    orders: Order[]
}

export interface KitchenOrder {
    id: string
    table: number
    date: Date
    orders: Order[]
    pending: Order[]
    comments: string
    progress: number
    status: string
}

export interface OrderRequest {
    client: string
    table: string
    name: string
    email: string
    comments: string
    cart: { id: string, quantity: number }[]
    token: string
}

export interface OrderDetails {
    id: string
    name: string
    date: Date
    table: string
    comments: string
    amount: number
    orders: Order[]
    pending: boolean
}

export interface Stats {
    sales: number
    top: {_id: string, name: string, quantity: number}[]
    hourly: { _id: number, count: number }[]
    first: string
    last: string
}

export interface OrderEdit {
    id: string
    item: string
    progress?: number
    old?: number
    quantity?: number
}

export interface LineItem {
    name: string
    quantity: number
    amount: number
}