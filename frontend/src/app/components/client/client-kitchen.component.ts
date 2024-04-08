import { Component, ElementRef, Input, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Observable, Subscription, firstValueFrom } from 'rxjs';
import { Client, KitchenOrder, Order } from '../../models';
import { SocketService } from '../../service/socket.service';
import { MatDialog } from '@angular/material/dialog';
import { Socket } from 'socket.io-client';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReceiptComponent } from '../receipt.component';

@Component({
  selector: 'app-client-kitchen',
  templateUrl: './client-kitchen.component.html',
  styleUrl: './client-kitchen.component.css'
})
export class ClientKitchenComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private socketSvc: SocketService = inject(SocketService)
  private dialog: MatDialog = inject(MatDialog)
  private fb: FormBuilder = inject(FormBuilder)

  @Input() client!: Client
  orders$!: Observable<KitchenOrder[]>
  pending: KitchenOrder[] = []
  socketSub!: Subscription
  receiptSub!: Subscription
  tableCols: string[] = ['name', 'quantity', 'action']
  @ViewChild('print') qrReceipt!: ElementRef
  @ViewChild('link') qrTemplate!: TemplateRef<any>
  @ViewChild('pendingOrders') pendingOrders!: TemplateRef<any>
  qrCode: string = ''
  timestamp: number = 0
  orderUrl: string = ''
  socket!: Socket
  form!: FormGroup
  @ViewChild('edit') editTemplate!: TemplateRef<any>


  ngOnInit(): void {
    this.orders$ = this.clientSvc.getKitchenOrders()
    this.socketSvc.onConnect(this.client.id)
      .then(() => {
        this.socketSub = this.socketSvc.socket.asObservable().subscribe({
          next: () => this.newOrder()
        })
      })
  }

  ngOnDestroy(): void {
    this.socketSub.unsubscribe()
    this.socketSvc.ws.close()
    if (!!this.receiptSub)
      this.receiptSub.unsubscribe()
  }

  newOrder() {
    this.orders$ = this.clientSvc.getKitchenOrders()
    const audio = new Audio
    audio.src = '../../../assets/audio/ring.mp3'
    audio.play()
    this.clientSvc.openSnackBar('Order up!')
  }

  generateLink() {
    const table = prompt('Enter Table ID:')
    if (!!table) {
      const num = table.trim()
      if (!!num)
        this.clientSvc.getOrderLink(num)
          .then(value => {
            this.dialog.open(this.qrTemplate)
            this.getQR(value.token)
            this.timestamp = Date.now()
          })
      else
        alert('Please enter a valid Table ID')
    }
  }

  filterOrders(orders: KitchenOrder[]) {
    this.pending = orders.filter(value => value.status == 'PENDING')
    return orders.filter(value => value.status != 'PENDING')
  }

  viewPendingOrders() {
    this.dialog.open(this.pendingOrders)
  }

  bill(order: KitchenOrder) {
    this.clientSvc.getLineItems(order.id)
      .then(value => this.receiptSub = this.dialog.open(ReceiptComponent, { data: { order: order, name: this.client.estName, items: value } })
        .afterClosed()
        .subscribe({
          next: (value) => {
            if (value !== '')
              this.orders$ = this.clientSvc.getKitchenOrders()
            if (typeof value == 'object')
              this.printLink(value)
          }
        }))
  }

  getProgress(order: KitchenOrder, count: number): number {
    order.orders.filter(value => value.completed == true).forEach(() => count += 1)
    return count / order.orders.length * 100
  }

  completeItem(order: KitchenOrder, item: Order) {
    const progress = this.getProgress(order, 1)
    if (progress == 100)
      this.completeOrder(order.id)
    else
      this.clientSvc.completeItem({
        id: order.id,
        item: item.id,
        progress: progress
      })
        .then(() => {
          item.completed = true
          order.progress = progress
        })
        .catch(err => alert(!!err.error.error ? err.error.error : 'Something went wrong'))
  }

  editItem(order: string, item: Order) {
    this.form = this.fb.group({
      quantity: this.fb.control<number>(0, [Validators.required, Validators.min(1), Validators.max(item.quantity - 1)])
    })

    firstValueFrom(this.dialog.open(this.editTemplate).afterClosed())
      .then(value => {
        if (value) {
          this.clientSvc.editItem({
            id: order,
            item: item.id,
            old: item.quantity,
            quantity: this.form.value.quantity
          })
            .then(value => {
              if (value.refund !== '0.0')
                alert(`Refund of $${value.refund}`)
              this.clientSvc.openSnackBar(`${item.name} has been changed to ${this.form.value.quantity}`)
              item.quantity = this.form.value.quantity
            })
            .catch(() => alert('Something went wrong'))
        }
      })
  }

  deleteItem(order: KitchenOrder, i: number) {
    const item = order.orders.splice(i, 1)
    const progress = this.getProgress(order, 0)
    this.clientSvc.deleteItem({
      id: order.id,
      item: item[0].id,
      progress: progress,
      old: item[0].quantity,
      quantity: 0
    })
      .then(value => {
        if (value.refund !== '0.0')
          alert(`Refund of $${value.refund}`)
        this.orders$ = this.clientSvc.getKitchenOrders()
        this.clientSvc.openSnackBar(`${item[0].name} has been removed`)
      })
      .catch(err => alert(!!err.error.error ? err.error.error : 'Something went wrong'))
  }

  completeOrder(id: string) {
    this.clientSvc.completeOrder(id)
      .then(() => {
        this.clientSvc.openSnackBar(`OrderID: #${id.toUpperCase()} completed`)
        this.orders$ = this.clientSvc.getKitchenOrders()
      })
      .catch(err => alert(!!err.error.error ? err.error.error : 'Something went wrong'))
  }

  deleteOrder(id: string) {
    this.clientSvc.deleteOrder(id)
      .then(value => {
        if (value.refund !== '0.0')
          alert(`Refund of $${value.refund}`)
        this.orders$ = this.clientSvc.getKitchenOrders()
      })  
      .catch(err => alert(!!err.error.error ? err.error.error : 'Something went wrong'))
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

  printLink(template: ElementRef) {
    const toPrint = template.nativeElement.innerHTML
    const w = window.open()
    if (w) {
      w.document.write(toPrint)
      w.print()
      w.close()
    }
  }
}

const url = 'https://api.qrserver.com/v1/create-qr-code/'