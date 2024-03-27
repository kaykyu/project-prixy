import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Router } from '@angular/router';
import { Client } from '../../models';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-client-main',
  templateUrl: './client-main.component.html',
  styleUrl: './client-main.component.css'
})
export class ClientMainComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private router: Router = inject(Router)

  client!: Client
  client$!: Subscription
  showNav: boolean = false
  links = NAV_LINKS

  ngOnInit(): void {
    this.clientSvc.checkAuth()
      .catch(err => {
        alert(err)
        this.router.navigate(['/login'])
      })
      .then(() => this.clientSvc.getClient())
      .then(value => this.client = value)
      .catch((err) => console.error(err))
    
    this.client$ = this.clientSvc.client.asObservable().subscribe({
      next: value => this.client = value
    })
  }

  ngOnDestroy(): void {
      this.client$.unsubscribe()
  }

  onRouting(component: any) {
    component.client = this.client
    this.clientSvc.checkAuth()
      .catch(err => {
        alert(err)
        localStorage.removeItem('prixyToken')
        this.router.navigate(['/login'])
      })
  }

  logout() {
    localStorage.removeItem('prixyToken')
  }
}

const NAV_LINKS = [
  { path: 'dash', label: 'home', tip: 'Dashboard' },
  { path: 'menu', label: 'restaurant_menu', tip: 'View menu' },
  { path: 'kitchen', label: 'store', tip: 'View orders' },
  { path: 'account', label: 'manage_accounts', tip: 'View Account Details' }
]