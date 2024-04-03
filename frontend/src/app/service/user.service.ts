import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';
import { Menu, Order, OrderRequest, Tax, User } from '../models';
import { loadStripe } from '@stripe/stripe-js';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private http: HttpClient = inject(HttpClient)
  private router: Router = inject(Router)
  private stripePromise!: Promise<any>
  private snackBar: MatSnackBar = inject(MatSnackBar)

  checkLink(link: string): Promise<User> {
    return new Promise((resolve, reject) => {
      const user: User = JSON.parse(atob(link))
      if (user.exp < Date.now() / 1000)
        reject('Link has expired')
      else if (user.iss !== "Prixy")
        reject('Link is invalid')
      resolve(user)
    })
  }

  openSnackBar(message: string) {
    this.snackBar.open(message, undefined, { duration: 1000 });
  }

  getMenu(client: string): Promise<Menu[]> {
    return firstValueFrom(this.http.get<Menu[]>(`/api/user/${client}/menu`))
  }

  setKey(): void {
    firstValueFrom(this.http.get<any>('/api/user/key'))
      .then(value => this.stripePromise = loadStripe(value.key))
  }

  async makeOrder(req: OrderRequest): Promise<any> {
    const stripe = await this.stripePromise

    return this.http.post('/api/user/order', req).subscribe({
      next: (value: any) => {
        this.router.navigate(['loading'], { queryParams: { 'msg': 'Redirecting you to payment merchant...' } })
        if (!!stripe)
          stripe.redirectToCheckout({
            sessionId: value.id
          })
      },
      error: (err) => console.log(err)
    })
  }

  getTax(client: string): Promise<Tax> {
    return firstValueFrom(this.http.get<Tax>(`/api/user/${client}/tax`))
  }

  sendReceipt(email: string): Promise<any> {
    return firstValueFrom(this.http.post<any>('/api/user/receipt', email))
  }

  getOrders(id: string): Observable<Order[]> {
    return this.http.get<Order[]>(`/api/user/orders/${id}`)
  }

  // getClientId(client: string): Promise<any> {
  //   return firstValueFrom(this.http.get<any>(`/api/user/${client}/id`))
  // }
}
