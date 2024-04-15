import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../service/user.service';
import { OrderDetails } from '../models';

@Component({
  selector: 'app-success',
  templateUrl: './success.component.html',
  styleUrl: './success.component.css'
})
export class SuccessComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)
  private userSvc: UserService = inject(UserService)
  private router: Router = inject(Router)

  id: string = ''
  details!: OrderDetails
  tableCols: string[] = ['item', 'quantity']

  ngOnInit(): void {
    this.id = this.ar.snapshot.params['id']
    this.userSvc.getOrders(this.id)
      .then(value => {
        if (!!value) {
          this.details = value
          localStorage.removeItem(value.id)
        }
      })
      .catch(() => this.router.navigateByUrl('/error?error=order'))
  }

  copied() {
    this.userSvc.openSnackBar('Copied to clipboard')
  }

  sendReceipt() {
    this.userSvc.sendReceipt(this.id)
      .then((value) => this.userSvc.openSnackBar(`Email was send to ${value.email}`))
      .catch(() => alert('Something went wrong'))
  }
}
