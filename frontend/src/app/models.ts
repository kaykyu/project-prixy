export interface Login {
    email: string
    pw: string
    change?: string
}

export interface Auth {
    sub: string
    email: string
    exp: number
}

export interface Client {
    id: string
    email: string
    estName: string
    tax: Tax
    status: boolean
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
    sub: string
    client: string
    table: number
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
    comments: string
    progress: number
    status: string
}

export interface Stats {
    sales: number
    top: {_id: string, name: string, quantity: number}[]
    hourly: { _id: number, count: number }[]
}

export interface OrderEdit {
    id: string
    item: string
    progress?: number
    old?: number
    quantity?: number
}