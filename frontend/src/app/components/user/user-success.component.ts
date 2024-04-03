import { Component, Input, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../service/user.service';
import { Order, User } from '../../models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-success',
  templateUrl: './user-success.component.html',
  styleUrl: './user-success.component.css'
})
export class UserSuccessComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)
  private userSvc: UserService = inject(UserService)

  @Input() user!: User
  id: string = ''
  order$!: Observable<Order[]>
  tableCols: string[] = ['item', 'quantity']

  ngOnInit(): void {
    this.id = this.ar.snapshot.params['id']
    localStorage.removeItem(this.user.sub)
    this.order$ = this.userSvc.getOrders(this.id)
  }

  copied() {
    this.userSvc.openSnackBar('Copied to clipboard')
  }

  sendReceipt() {
    this.userSvc.sendReceipt(this.id)
      .then((value) => this.userSvc.openSnackBar(`Email was send to ${value.email}`))
      .catch(() => alert('Something went wrong.'))
  }
}
