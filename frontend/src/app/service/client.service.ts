import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Client, KitchenOrder, LineItem, Login, Menu, OrderEdit, Stats } from '../models';
import { Observable, firstValueFrom } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private http: HttpClient = inject(HttpClient)
  private snackBar: MatSnackBar = inject(MatSnackBar)

  headers(): HttpHeaders {
    return new HttpHeaders().set('Authorization', `Bearer ${localStorage.getItem('prixyToken')}`)
  }

  openSnackBar(message: string) {
    this.snackBar.open(message, undefined, { duration: 1000 })
  }

  confirmClient(change: Login): Promise<any> {
    return firstValueFrom(this.http.post('/api/auth/verify', change, { headers: this.headers() }))
  }

  getClient(): Promise<Client> {
    return firstValueFrom(this.http.get<Client>('/api/client', { headers: this.headers() }))
  }

  putEmail(email: string): Promise<string> {
    return firstValueFrom(this.http.put<string>('api/client/email', email, { headers: this.headers() }))
  }

  putClient(client: any): Promise<Client> {
    return firstValueFrom(this.http.put<Client>('/api/client', client, { headers: this.headers() }))
  }

  putPassword(change: Login): Promise<any> {
    return firstValueFrom(this.http.put('/api/auth/password', change, { headers: this.headers() }))
  }

  getKitchen(): Promise<any> {
    return firstValueFrom(this.http.get('/api/auth/kitchen', { headers: this.headers() }))
  }

  postKitchen(): Promise<any> {
    return firstValueFrom(this.http.post('/api/auth/kitchen', {}, { headers: this.headers() }))
  }

  getMenu(): Observable<Menu[]> {
    return this.http.get<Menu[]>('/api/client/menu', { headers: this.headers() })
  }

  getMenuCategory(): Promise<string[]> {
    return firstValueFrom(this.http.get<string[]>('/api/client/menu/categories', { headers: this.headers() }))
  }

  postMenu(menu: FormData): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/client/menu', menu, { headers: this.headers() }))
  }

  putMenu(menu: FormData): Promise<void> {
    return firstValueFrom(this.http.put<void>('/api/client/menu', menu, { headers: this.headers() }))
  }

  deleteMenuImage(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`/api/client/menu/${id}/image`, { headers: this.headers() }))
  }

  deleteMenu(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`/api/client/menu/${id}`, { headers: this.headers() }))
  }

  getKitchenOrders(): Observable<KitchenOrder[]> {
    return this.http.get<KitchenOrder[]>('/api/client/kitchen', { headers: this.headers() })
  }

  completeItem(edit: OrderEdit): Promise<any> {
    return firstValueFrom(this.http.post('/api/client/order/item', edit, { headers: this.headers() }))
  }

  editItem(edit: OrderEdit): Promise<any> {
    return firstValueFrom(this.http.put('/api/client/order/item', edit, { headers: this.headers() }))
  }

  deleteItem(edit: OrderEdit): Promise<any> {
    return firstValueFrom(this.http.post('/api/client/order/item/delete', edit, { headers: this.headers() }))
  }

  completeOrder(id: string): Promise<any> {
    return firstValueFrom(this.http.post('/api/client/order/complete', id, { headers: this.headers() }))
  }

  deleteOrder(id: string): Promise<any> {
    return firstValueFrom(this.http.post('/api/client/order/delete', id, { headers: this.headers() }))
  }

  getOrderLink(table: string): Promise<any> {
    const param = new HttpParams().set('table', table)
    return firstValueFrom(this.http.get<any>('/api/client/orderLink', { headers: this.headers(), params: param }))
  }

  getLineItems(id: string): Promise<LineItem[]> {
    const param = new HttpParams().set('order', id)
    return firstValueFrom(this.http.get<LineItem[]>('/api/client/bill', { headers: this.headers(), params: param }))
  }

  postPayment(id: string): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/client/payment', id, { headers: this.headers() }))
  }

  getStats(q: number): Promise<Stats> {
    const param = new HttpParams().set('q', q)
    return firstValueFrom(this.http.get<Stats>('/api/client/stats', { headers: this.headers(), params: param }))
  }

  getRecords(q: number): Promise<any> {
    const param = new HttpParams().set('q', q)
    return firstValueFrom(this.http.get<any>('/api/client/records', { headers: this.headers(), params: param }))
  }
}

