import { TitleCasePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrl: './error.component.css'
})
export class ErrorComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)
  private tcPipe: TitleCasePipe = inject(TitleCasePipe)

  message: string = 'Something went wrong. Please try again later.'

  ngOnInit(): void {
    const error = this.ar.snapshot.queryParams['error']
    if (error == 'expired')
      this.message = 'Your link has expired.'
  }
}
