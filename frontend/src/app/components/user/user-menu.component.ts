import { Component, Input, OnInit, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Menu, Order, User } from '../../models';
import { UserStoreService } from '../../service/user-store.service';

@Component({
  selector: 'app-user-menu',
  templateUrl: './user-menu.component.html',
  styleUrl: './user-menu.component.css'
})
export class
  UserMenuComponent implements OnInit {

  private userStore: UserStoreService = inject(UserStoreService)

  @Input() user!: User
  menu$!: Observable<Menu[]>
  categories$!: Observable<Set<string>>
  tabs: number = 0
  selectedTab: number = 0

  ngOnInit(): void {
    this.menu$ = this.userStore.getMenu
    this.categories$ = this.userStore.getCategories.pipe(
      tap(value => this.tabs = value.size - 1)
    )
  }

  filter(menu: Menu[], category: string): Menu[] {
    return menu.filter(menu => menu.category.toString() === category)
  }

  toCart(item: Menu, quantity: number) {
    const order: Order = {
      id: item.id,
      name: item.name,
      price: item.price,
      quantity: quantity
    }
    this.userStore.editOrder(order)
    if (quantity === 1)
      this.userStore.snackBar(`${item.name} added to cart.`)
    else
      this.userStore.snackBar(`${item.name} removed from cart.`)
  }

  swipe(direction: string) {
    switch (direction) {
      case 'left':
        if (this.selectedTab === this.tabs)
          break;
        this.selectedTab += 1
        break;

      case 'right':
        if (this.selectedTab === 0)
          break;
        this.selectedTab -= 1
        break;
    }
  }
}