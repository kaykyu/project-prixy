import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrl: './error.component.css'
})
export class ErrorComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)

  message: string = ''

  ngOnInit(): void {
    const error = this.ar.snapshot.queryParams['error']
    if (error == 'expired')
      this.message = 'Your link has expired.'
    else if (error == 'invalidlink')
      this.message = 'Your link is invalid. Please check with the staff.'
    else if (error == 'order')
      this.message = 'Something went wrong. Please check with the staff about your order.'
  }
}
