import { Component, Input, OnInit, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Menu, Order, User } from '../../models';
import { UserStoreService } from '../../service/user-store.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-user-menu',
  templateUrl: './user-menu.component.html',
  styleUrl: './user-menu.component.css'
})
export class
  UserMenuComponent implements OnInit {

  private userStore: UserStoreService = inject(UserStoreService)
  private _snackBar = inject(MatSnackBar)

  @Input() user!: User
  menu$!: Observable<Menu[]>
  categories$!: Observable<Set<string>>

  ngOnInit(): void {
    this.menu$ = this.userStore.getMenu
    this.categories$ = this.userStore.getCategories
  }

  openSnackBar(message: string) {
    this._snackBar.open(message, undefined, { duration: 1000 });
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
      this.openSnackBar(`${item.name} added to cart.`)
    else
      this.openSnackBar(`${item.name} removed from cart.`)
  }

}
