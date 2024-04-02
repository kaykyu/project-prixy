import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SocketService {

  ws!: WebSocket

  socket: Subject<void> = new Subject

  onMessage(_event: MessageEvent<any>) {
    console.log('Received from Websocket')
    this.socket.next()
  }

  async onConnect(client: string) {
    console.log("Connecting...")
    this.ws = new WebSocket(`wss://${window.location.host}/websocket`)

    this.ws.onmessage = this.onMessage.bind(this)
    this.ws.onerror = (error) => console.error(error)

    this.ws.onopen = (_event) => {
      console.log('Connected to Websocket')
      const msg = {client: client}
      this.ws.send(JSON.stringify(msg))
    }

    this.ws.onclose = (event) => {
      console.log('Disconnected from Websocket', event.reason)
      if (window.location.hash.endsWith('kitchen'))
        this.onConnect(client)
    }
  }
}