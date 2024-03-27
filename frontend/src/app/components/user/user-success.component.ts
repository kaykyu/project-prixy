import { Component, Input, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../../service/user.service';
import { Order, User } from '../../models';
import { Observable } from 'rxjs';
import { SocketService } from '../../service/socket.service';

@Component({
  selector: 'app-success',
  templateUrl: './user-success.component.html',
  styleUrl: './user-success.component.css'
})
export class UserSuccessComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)
  private userSvc: UserService = inject(UserService)
  private socketSvc: SocketService = inject(SocketService)

  @Input() user!: User
  id: string = ''
  order$!: Observable<Order[]>
  tableCols: string[] = ['item', 'quantity']

  ngOnInit(): void {
    this.id = this.ar.snapshot.params['id']
    localStorage.removeItem(this.user.client)
    this.order$ = this.userSvc.getOrders(this.id)
    this.socketSvc.onConnect(this.user.sub)
      .then((value) => value.onopen = () => this.socketSvc.onSend())
      .catch((err) => console.error(err))
  }

  sendReceipt() {
    this.userSvc.sendReceipt(this.id)
      .then((value) => alert(`Receipt was sent to ${value.email}`))
      .catch(() => alert('Something went wrong.'))
  }
}
