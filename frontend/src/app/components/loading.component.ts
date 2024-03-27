import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrl: './loading.component.css'
})
export class LoadingComponent implements OnInit {

  private ar: ActivatedRoute = inject(ActivatedRoute)

  message!: string

  ngOnInit(): void {
    this.message = this.ar.snapshot.queryParams['msg']
  }
}
