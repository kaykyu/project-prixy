import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Auth, Login } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http: HttpClient = inject(HttpClient)

  checkAuth(): Promise<Auth> {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('prixyToken')
      if (!token)
        reject()
      else {
        const auth: Auth = JSON.parse(atob(token.split('.')[1]))
        if (auth.exp < Date.now() / 1000) {
          alert('Session has expired')
          localStorage.removeItem('prixyToken')
          reject()
        } else if (auth.iss !== 'Prixy') {
          localStorage.removeItem('prixyToken')
          reject()
        }
        resolve(auth)
      }
    })
  }

  signup(login: Login): Promise<any> {
    login.email = login.email.toLowerCase()
    return firstValueFrom(this.http.post<Login>('/api/auth/signup', login))
  }

  login(login: Login): Promise<any> {
    login.email = login.email.toLowerCase()
    return firstValueFrom(this.http.post<any>('/api/auth/login', login))
  }

  resetPw(email: string): Promise<any> {
    const param = new HttpParams().set('email', email.toLowerCase())
    return firstValueFrom(this.http.get('/api/auth/reset', { params: param }))
  }
}
