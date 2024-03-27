import { Component, Input, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ClientService } from '../../service/client.service';
import { Client, Login } from '../../models';
import { environment } from '../../../environments/environment';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-client-account',
  templateUrl: './client-account.component.html',
  styleUrl: './client-account.component.css'
})
export class ClientAccountComponent implements OnInit {

  private clientSvc: ClientService = inject(ClientService)
  private authSvc: AuthService = inject(AuthService)
  private fb: FormBuilder = inject(FormBuilder)
  private dialog: MatDialog = inject(MatDialog)

  @Input() client!: Client
  @ViewChild('pw') pw!: TemplateRef<any>
  form!: FormGroup
  emailForm!: FormGroup
  pwForm!: FormGroup
  editing: boolean = false
  changeEmail: boolean = false
  gst: number = environment.gst
  tableCols: string[] = ['title', 'info']
  hide: boolean = true
  hide2: boolean = true
  hide3: boolean = true

  ngOnInit(): void {
    this.form = this.fb.group({
      estName: this.fb.control<string>(this.client.estName, [Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
      gst: this.fb.control<boolean>(this.client.gst, [Validators.required]),
      svcCharge: this.fb.control<number>(this.client.svcCharge, [Validators.required])
    })

    this.emailForm = this.fb.group({
      email: this.fb.control<string>(this.client.email, [Validators.email, Validators.required])
    })
  }

  cancel() {
    this.form.patchValue(this.client)
    this.editing = false
  }

  done() {
    this.editing = false
    const client = this.form.value
    this.clientSvc.putClient(client)
      .then(value => {
        this.form.patchValue(value)
        this.clientSvc.client.next(value)
      })
  }

  emailCancel() {
    this.changeEmail = false
    this.emailForm.patchValue(this.client)
  }

  emailDone() {
    this.changeEmail = false
    this.clientSvc.putEmail(this.emailForm.value.email)
      .then(value => this.client.email = value)
      .catch(err => {
        this.emailForm.patchValue(this.client)
        alert(err.error.error)
      })
  }

  changePw() {
    this.pwForm = this.fb.group({
      oldPw: this.fb.control<string>('', Validators.required),
      pw: this.fb.control<string>('', [Validators.required, Validators.pattern(new RegExp('^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$'))]),
      pw2: this.fb.control<string>('', [Validators.required, c => this.pwMatch(c)]),
    })
    this.dialog.open(this.pw)
  }

  pwMatch: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const pw = this.pwForm?.controls['pw'].value
    const pw2 = this.pwForm?.controls['pw2'].value
    return pw === pw2 ? null : { notMatched: true }
  }

  pwDone() {
    const change: Login = {
      'email': this.client.email,
      'pw': this.pwForm.value.oldPw,
      'change': this.pwForm.value.pw
    }
    this.authSvc.putPassword(change)
      .then(() => {
        alert('Password changed successfully.')
        this.pwForm.reset()
        this.dialog.closeAll()
      })
      .catch(err => alert(err.error.error))
  }
}
