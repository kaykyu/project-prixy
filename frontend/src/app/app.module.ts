import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './components/login.component';
import { ClientMenuComponent } from './components/client/client-menu.component';
import { ClientMainComponent } from './components/client/client-main.component';
import { UserMainComponent } from './components/user/user-main.component';
import { UserCartComponent } from './components/user/user-cart.component';
import { ErrorComponent } from './components/error.component';
import { UserMenuComponent } from './components/user/user-menu.component';
import { UserSuccessComponent } from './components/user/user-success.component';
import { LoadingComponent } from './components/loading.component';
import { ClientKitchenComponent } from './components/client/client-kitchen.component';
import { HomeComponent } from './components/home.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MaterialModule } from './material.module';
import { ClientDashboardComponent } from './components/client/client-dashboard.component';
import { ClientMenuDetailsComponent } from './components/client/client-menu-details.component';
import { ClientAccountComponent } from './components/client/client-account.component';
import { ImageCropperModule } from 'ngx-image-cropper';
import { ServiceWorkerModule } from '@angular/service-worker';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { UserTitleComponent } from './components/user/user-title.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    ClientMenuComponent,
    ClientMainComponent,
    UserMainComponent,
    UserCartComponent,
    UserMenuComponent,
    ErrorComponent,
    UserSuccessComponent,
    LoadingComponent,
    ClientKitchenComponent,
    HomeComponent,
    ClientDashboardComponent,
    ClientMenuDetailsComponent,
    ClientAccountComponent,
    UserTitleComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    MaterialModule,
    ImageCropperModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    ClipboardModule
  ],
  providers: [
    provideAnimationsAsync()
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
