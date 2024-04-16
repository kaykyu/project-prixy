import { Injectable } from '@angular/core';
import { ComponentStore } from '@ngrx/component-store';
import { Client, ClientSlice, KitchenOrder, Menu } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ClientStoreService extends ComponentStore<ClientSlice> {

  constructor() {
    super(INIT_SLICE)
  }

  readonly getClient = this.select<Client>(
    (slice: ClientSlice) => slice.client
  )

  readonly getOrders = this.select<KitchenOrder[]>(
    (slice: ClientSlice) => slice.orders
  )

  readonly getPending = this.select<KitchenOrder[]>(
    (slice: ClientSlice) => slice.pending
  )

  readonly getMenu = this.select<Menu[]>(
    (slice: ClientSlice) => slice.menu
  )

  readonly isAdmin = this.select<boolean>(
    (slice: ClientSlice) => slice.admin
  )

  readonly setClient = this.updater<Client>(
    (slice: ClientSlice, client: Client) => {
      return {
        client: client,
        orders: slice.orders,
        pending: slice.pending,
        menu: slice.menu,
        admin: slice.admin
      }
    }
  )

  readonly setOrders = this.updater<KitchenOrder[]>(
    (slice: ClientSlice, orders: KitchenOrder[]) => {
      return {
        client: slice.client,
        orders: orders.filter(value => value.status != 'PENDING'),
        pending: orders.filter(value => value.status == 'PENDING'),
        menu: slice.menu,
        admin: slice.admin
      }
    }
  )

  readonly setMenu = this.updater<Menu[]>(
    (slice: ClientSlice, menu: Menu[]) => {
      return {
        client: slice.client,
        orders: slice.orders,
        pending: slice.pending,
        menu: menu,
        admin: slice.admin
      }
    }
  )

  readonly setIsAdmin = this.updater<boolean>(
    (slice: ClientSlice, admin: boolean) => {
      return {
        client: slice.client,
        orders: slice.orders,
        pending: slice.pending,
        menu: slice.menu,
        admin: admin
      }
    }
  )
}

const INIT_SLICE = {
  client: {
    id: '',
    email: '',
    estName: '',
    tax: {
      svc: 0,
      gst: false
    }
  },
  orders: [],
  pending: [],
  menu: [],
  admin: true
}