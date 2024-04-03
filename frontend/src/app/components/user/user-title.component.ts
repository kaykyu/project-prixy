import { Component, Input } from '@angular/core';
import { User } from '../../models';

@Component({
  selector: 'app-user-title',
  templateUrl: './user-title.component.html',
  styleUrl: './user-title.component.css'
})
export class UserTitleComponent {

  @Input() user!: User
}
