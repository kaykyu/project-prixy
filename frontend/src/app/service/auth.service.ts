import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Login } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http: HttpClient = inject(HttpClient)

  signup(login: Login): Promise<any> {
    login.email = login.email.toLowerCase()
    return firstValueFrom(this.http.post<Login>('/api/auth/signup', login))
  }

  login(login: Login): Promise<any> {
    login.email = login.email.toLowerCase()
    return firstValueFrom(this.http.post<any>('/api/auth/login', login))
  }

  putPassword(change: Login): Promise<any> {
    return firstValueFrom(this.http.put('/api/auth/password', change))
  }
}
