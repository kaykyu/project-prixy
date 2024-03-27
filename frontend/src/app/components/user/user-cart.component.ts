import { Component, Input, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { UserStoreService } from '../../service/user-store.service';
import { Subscription, tap } from 'rxjs';
import { Order, Tax, User } from '../../models';
import { UserService } from '../../service/user.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-user-cart',
  templateUrl: './user-cart.component.html',
  styleUrl: './user-cart.component.css',
})
export class UserCartComponent implements OnInit, OnDestroy {

  private ar: ActivatedRoute = inject(ActivatedRoute)
  private userStore: UserStoreService = inject(UserStoreService)
  private userSvc: UserService = inject(UserService)
  private fb: FormBuilder = inject(FormBuilder)

  @Input() user!: User
  form!: FormGroup
  cart$!: Subscription
  cart: Order[] = []
  sum!: number
  tax!: Tax
  svcFee!: number
  gstAmount: number = environment.gst
  tableCols: string[] = ['item', 'quantity', 'amount']
  initDone: boolean = false

  ngOnInit(): void {
    this.userSvc.setKey()

    this.cartInit()
      .then(() => this.userSvc.getTax(this.user.sub))
      .then(value => {
        this.tax = value
        this.svcFee = value.svc / 100 * this.sum
      })
      .then(() => this.sum += this.svcFee)
      .then(() => {
        if (this.tax.gst)
          this.sum *= 1 + (this.gstAmount / 100)
      })
      .then(() => this.initDone = true)
      .catch((err) => console.error(err))
  }

  ngOnDestroy(): void {
    this.cart$.unsubscribe()
    this.userStore.destroy$
  }

  async cartInit() {
    this.cart$ = this.userStore.getOrders.pipe(
      tap(value => {
        this.sum = 0
        value.forEach(value => this.sum += (value.quantity * value.price))
        this.cart = value
      }))
      .subscribe()
  }

  getDetails() {
    this.form = this.fb.group({
      name: this.fb.control<string>('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
      email: this.fb.control<string>('', [Validators.email, Validators.required])
    })
  }

  processOrder() {
    localStorage.setItem(this.user.sub, JSON.stringify(this.cart))

    const req = {
      token: this.ar.snapshot.parent?.params['token'],
      cart: this.cart,
      amount: this.sum.toFixed(2),
      name: this.form.value.name,
      email: this.form.value.email
    }
    this.userSvc.makeOrder(req)
  }
}

