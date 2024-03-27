import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { ClientMenuComponent } from './components/client/client-menu.component';
import { ClientMainComponent } from './components/client/client-main.component';
import { UserMainComponent } from './components/user/user-main.component';
import { UserCartComponent } from './components/user/user-cart.component';
import { ErrorComponent } from './components/error.component';
import { UserMenuComponent } from './components/user/user-menu.component';
import { UserSuccessComponent } from './components/user/user-success.component';
import { LoadingComponent } from './components/loading.component';
import { HomeComponent } from './components/home.component';
import { ClientKitchenComponent } from './components/client/client-kitchen.component';
import { ClientDashboardComponent } from './components/client/client-dashboard.component';
import { ClientAccountComponent } from './components/client/client-account.component';
import { isTokenValid } from './guards';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'main', component: ClientMainComponent, children: [
      { path: 'menu', component: ClientMenuComponent },
      { path: 'kitchen', component: ClientKitchenComponent},
      { path: 'dash', component: ClientDashboardComponent },
      {path: 'account', component: ClientAccountComponent},
      { path: '**', redirectTo: 'dash', pathMatch: 'full' }
    ]
  },
  {
    path: 'order/:token', component: UserMainComponent, canActivate: [isTokenValid], children: [
      { path: 'cart', component: UserCartComponent },
      { path: 'success/:id', component: UserSuccessComponent },
      { path: 'menu', component: UserMenuComponent },
      { path: '**', redirectTo: 'menu', pathMatch: 'full' }
    ]
  },
  { path: 'error', component: ErrorComponent },
  { path: 'loading', component: LoadingComponent },
  { path: '', component: HomeComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
