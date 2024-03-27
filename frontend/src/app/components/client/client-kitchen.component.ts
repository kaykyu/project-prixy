import { Component, ElementRef, Input, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Observable, Subscription } from 'rxjs';
import { Client, KitchenOrder } from '../../models';
import { SocketService } from '../../service/socket.service';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-client-kitchen',
  templateUrl: './client-kitchen.component.html',
  styleUrl: './client-kitchen.component.css'
})
export class ClientKitchenComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private socketSvc: SocketService = inject(SocketService)
  private dialog: MatDialog = inject(MatDialog)

  @Input() client!: Client
  orders$!: Observable<KitchenOrder[]>
  socketSub!: Subscription
  kitchenStatus!: boolean
  tableCols: string[] = ['name', 'quantity', 'action']
  @ViewChild('print') doc!: ElementRef
  @ViewChild('link') template!: TemplateRef<any>
  qrCode: string = ''
  timestamp: number = 0
  orderUrl: string = ''


  ngOnInit(): void {
    this.kitchenStatus = this.client.status

    this.orders$ = this.clientSvc.getKitchenOrders()
    this.socketSvc.onConnect(this.client.id)
      .then(() => this.socketSub = this.socketSvc.socket.asObservable().subscribe({
        next: () => this.orders$ = this.clientSvc.getKitchenOrders()
      }))
  }

  ngOnDestroy(): void {
    this.socketSub.unsubscribe()
    this.socketSvc.onWebSocketclose()
  }

  toggle(event: MatSlideToggleChange) {
    this.kitchenStatus = event.checked
    this.clientSvc.postKitchenStatus(this.kitchenStatus)
      .then(() => alert(`Kitchen is now ${this.kitchenStatus ? 'open' : 'closed'}`))
      .catch(() => alert('Something went wrong'))
  }

  generateLink() {
    const num = prompt('Enter Table ID:')
    if (!!num)
      this.clientSvc.getOrderLink(num)
        .then(value => {
          this.dialog.open(this.template)
          this.getQR(value.token)
          this.timestamp = Date.now()
        })
  }

  completeItem(order: KitchenOrder, item: string) {
    if (!order.progress)
      order.progress = 0
    const count = order.orders.length
    if (count === 1)
      this.completeOrder(order.id)
    order.progress += (1 / count) * 100
    order.orders.filter(value => value.name === item).forEach(value => value.completed = true)
  }

  completeOrder(id: string) {
    this.clientSvc.completeOrder(id)
      .then(() => this.orders$ = this.clientSvc.getKitchenOrders())
  }

  getQR(link: string) {
    this.orderUrl = `http://${window.location.host}/#/order/${link}`
    const imageUrl = `${url}?data=${encodeURIComponent(this.orderUrl)}&size=200x200`
    this.getBase64ImageFromUrl(imageUrl)
      .then(value => this.qrCode = value as string)
      .catch(err => console.error(err))
  }

  async getBase64ImageFromUrl(url: string) {
    var res = await fetch(url)
    var blob = await res.blob()

    return new Promise((resolve, reject) => {
      var reader = new FileReader()
      reader.addEventListener("load", function () {
        resolve(reader.result)
      }, false)

      reader.onerror = () => {
        return reject(this)
      }
      reader.readAsDataURL(blob)
    })
  }

  printLink() {
    const toPrint = this.doc.nativeElement.innerHTML
    const w = window.open()
    if (w) {
      w.document.write(toPrint)
      w.print()
      w.close()
    }
  }
}

const url = 'https://api.qrserver.com/v1/create-qr-code/'