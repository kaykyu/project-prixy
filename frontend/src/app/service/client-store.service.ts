import { Injectable } from '@angular/core';
import { ComponentStore } from '@ngrx/component-store';
import { Client, ClientSlice } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ClientStoreService extends ComponentStore<ClientSlice> {

  constructor() {
    super(INIT_SLICE)
  }

  readonly getClient = this.select<Client>(
    (slice: ClientSlice) => slice.client
  )

  readonly editClient = this.updater<Client>(
    (_slice: ClientSlice, client: Client) => {
      const newSlice = { client: client }
      return newSlice
    }
  )
}

const INIT_SLICE = {
  client: {
    id: '',
    email: '',
    estName: '',
    tax: {
      svc: 0,
      gst: false
    }
  }
}