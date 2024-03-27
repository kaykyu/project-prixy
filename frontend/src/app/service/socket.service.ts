import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SocketService {

  private ws!: WebSocket

  socket: Subject<void> = new Subject

  onWebSocketOpen() {
    console.log('Connected to Websocket')
  }

  onWebSocketMessage(event: MessageEvent<any>) {
    console.log('Received from Websocket')
    this.socket.next()
  }

  onWebSocketclose() {
    console.log('Disconnected from Websocket')
    this.ws.close();
  }

  async onConnect(id: string): Promise<any> {
    return new Promise(resolve => {
      console.log("Connecting...")
      this.ws = new WebSocket(`ws://${window.location.host}/websocket/${id}`)
      this.ws.onopen = this.onWebSocketOpen.bind(this)
      this.ws.onmessage = this.onWebSocketMessage.bind(this)
      this.ws.onclose = this.onWebSocketclose.bind(this)
      resolve(this.ws)
    })
  }

  onSend() {
    console.log('Pinging Websocket')
    this.ws.send('');
  }
}