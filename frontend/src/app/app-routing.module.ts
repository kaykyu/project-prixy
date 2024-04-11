import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { ClientMenuComponent } from './components/client/client-menu.component';
import { ClientMainComponent } from './components/client/client-main.component';
import { UserMainComponent } from './components/user/user-main.component';
import { UserCartComponent } from './components/user/user-cart.component';
import { ErrorComponent } from './components/error.component';
import { UserMenuComponent } from './components/user/user-menu.component';
import { SuccessComponent } from './components/success.component';
import { LoadingComponent } from './components/loading.component';
import { HomeComponent } from './components/home.component';
import { ClientKitchenComponent } from './components/client/client-kitchen.component';
import { ClientDashboardComponent } from './components/client/client-dashboard.component';
import { ClientAccountComponent } from './components/client/client-account.component';
import { checkAuth, isLoggedIn, isTokenValid } from './guards';
import { UserTitleComponent } from './components/user/user-title.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [isLoggedIn] },
  {
    path: 'main', component: ClientMainComponent, canActivateChild: [checkAuth], children: [
      { path: 'menu', component: ClientMenuComponent },
      { path: 'kitchen', component: ClientKitchenComponent },
      { path: 'dash', component: ClientDashboardComponent },
      { path: 'account', component: ClientAccountComponent },
      { path: '**', redirectTo: 'dash', pathMatch: 'full' }
    ]
  },
  {
    path: 'order/:token', component: UserMainComponent, canActivate: [isTokenValid], children: [
      { path: 'cart', component: UserCartComponent },
      { path: 'menu', component: UserMenuComponent },
      { path: '', component: UserTitleComponent },
      { path: '**', redirectTo: '', pathMatch: 'full' }
    ]
  },
  { path: 'success/:id', component: SuccessComponent },
  { path: 'error', component: ErrorComponent },
  { path: 'loading', component: LoadingComponent },
  { path: '', component: HomeComponent },
  { path: '**', redirectTo: 'error', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
