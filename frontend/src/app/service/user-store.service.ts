import { Injectable, OnDestroy, inject } from '@angular/core';
import { UserService } from './user.service';
import { ComponentStore, OnStoreInit } from '@ngrx/component-store';
import { Menu, Order, User, UserSlice } from '../models';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class UserStoreService extends ComponentStore<UserSlice> implements OnStoreInit, OnDestroy {

  private userSvc: UserService = inject(UserService)
  private ar: ActivatedRoute = inject(ActivatedRoute)

  constructor() {
    super(INIT_SLICE)
  }

  snackBar(message: string) {
    this.userSvc.openSnackBar(message)
  }

  ngrxOnStoreInit(): void {
    this.userSvc.checkLink(this.ar.snapshot.params['token'])
      .then(value => {
        this.setUser(value)
        return this.userSvc.getMenu(value.sub)
      })
      .then(value => {
        value.forEach(menu => menu.quantity = 0)
        return value
      })
      .then(value => this.setMenu(value))
      .then(() => {
        return firstValueFrom(this.getUser)
      })
      .then(value => {
        const storedCart: Order[] = JSON.parse(localStorage.getItem(value.sub) as string)
        if (!!storedCart) {
          storedCart.forEach(value => this.editOrder(value))
          localStorage.removeItem(value.sub)
        }
      })
      .then(() => this.linkMenuToOrders(this.getOrders))
      .catch(err => alert(err))
  }

  setUser(user: User) {
    this.patchState((_slice: UserSlice) => ({ user: user }))
  }

  setMenu(menu: Menu[]) {
    this.patchState((_slice: UserSlice) => ({ menu: menu }))
  }

  readonly getUser = this.select<User>(
    (slice: UserSlice) => slice.user
  )

  readonly getOrders = this.select<Order[]>(
    (slice: UserSlice) => slice.orders
  )

  readonly getMenu = this.select<Menu[]>(
    (slice: UserSlice) => slice.menu
  )

  readonly getCategories = this.select<Set<string>>(
    (slice: UserSlice) => new Set(slice.menu.map(menu => menu.category.toString()))
  )

  readonly processMenu = this.updater<Order>(
    (slice: UserSlice, order: Order) => {
      slice.menu.forEach(value => {
        if (order.id == value.id)
          value.quantity = order.quantity
      })
      const newSlice: UserSlice = {
        user: slice.user,
        menu: slice.menu,
        orders: slice.orders
      }
      return newSlice
    }
  )

  readonly linkMenuToOrders = this.updater<Order[]>(
    (slice: UserSlice, order: Order[]) => {
      order.forEach(value => this.processMenu(value))
      const newSlice: UserSlice = {
        user: slice.user,
        menu: slice.menu,
        orders: slice.orders
      }
      return newSlice
    })

  readonly editOrder = this.updater<Order>(
    (slice: UserSlice, order: Order) => {
      const i = slice.orders.findIndex(value => value.id === order.id)

      if (i === -1) {
        const newSlice: UserSlice = {
          user: slice.user,
          menu: slice.menu,
          orders: [...slice.orders, order]
        }
        this.linkMenuToOrders(this.getOrders)
        return newSlice

      } else {
        slice.orders[i].quantity += order.quantity
        this.linkMenuToOrders(this.getOrders)

        if (slice.orders[i].quantity === 0) {
          slice.orders.splice(i, 1)
          slice.menu.map(menu => {
            if (menu.id == order.id)
              menu.quantity = 0
          })
        }
        return slice
      }
    }
  )
}

const INIT_SLICE = {
  user: {
    iss: '',
    sub: '',
    client: '',
    table: '',
    exp: 0
  },
  menu: [],
  orders: []
}