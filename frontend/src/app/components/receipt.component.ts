import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { LineItem } from '../models';
import { ClientService } from '../service/client.service';

@Component({
  selector: 'app-receipt',
  templateUrl: './receipt.component.html',
  styleUrl: './receipt.component.css'
})
export class ReceiptComponent implements OnInit {

  public data: any = inject(MAT_DIALOG_DATA)
  private dialog = inject(MatDialogRef<ReceiptComponent>)
  private clientSvc: ClientService = inject(ClientService)

  @ViewChild('print') receipt!: ElementRef
  amount: number = 0

  ngOnInit(): void {
    this.data.items.forEach((value: LineItem) => this.amount += value.amount)
  }

  filter(items: LineItem[]): LineItem[] {
    return items.filter(value => value.quantity != 0)
  }

  getSvc(items: LineItem[]): LineItem | undefined {
    return items.find(value => value.name == 'Service Charge')
  }

  getGst(items: LineItem[]): LineItem | undefined {
    return items.find(value => value.name == 'GST')
  }

  paymentDone() {
    this.clientSvc.postPayment(this.data.order.id)
      .then(() => {
        const print = confirm('Print receipt?')
        if (print)
          this.dialog.close(this.receipt)
        else 
          this.dialog.close(true)
      })
  }
}
