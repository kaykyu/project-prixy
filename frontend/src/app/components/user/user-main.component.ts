import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { UserStoreService } from '../../service/user-store.service';
import { provideComponentStore } from '@ngrx/component-store';
import { Subscription } from 'rxjs';
import { Order, User } from '../../models';

@Component({
  selector: 'app-user-main',
  templateUrl: './user-main.component.html',
  styleUrl: './user-main.component.css',
  providers: [provideComponentStore(UserStoreService)]
})
export class UserMainComponent implements OnInit, OnDestroy{

  private userStore: UserStoreService = inject(UserStoreService)

  links = NAV_LINKS
  user$!: Subscription
  user!: User
  storedCart!: Order[]

  ngOnInit(): void {
    this.user$ = this.userStore.getUser.subscribe({
      next: value => this.user = value,
      error: err => console.error(err)
    })
  }

  ngOnDestroy(): void {
      this.user$.unsubscribe()
  }
  
  onRouting(component: any) {
    component.user = this.user
  }
}

const NAV_LINKS = [
  { path: 'menu', label: 'restaurant_menu', tip: 'View menu' },
  { path: 'cart', label: 'shopping_cart', tip: 'View cart' },
]