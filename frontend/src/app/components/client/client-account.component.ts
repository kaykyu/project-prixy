import { Component, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ClientService } from '../../service/client.service';
import { Client, Login } from '../../models';
import { environment } from '../../../environments/environment';
import { MatDialog } from '@angular/material/dialog';
import { ClientStoreService } from '../../service/client-store.service';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-client-account',
  templateUrl: './client-account.component.html',
  styleUrl: './client-account.component.css'
})
export class ClientAccountComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private clientStore: ClientStoreService = inject(ClientStoreService)
  private fb: FormBuilder = inject(FormBuilder)
  private dialog: MatDialog = inject(MatDialog)

  @ViewChild('pw') pw!: TemplateRef<any>
  @ViewChild('confirmPw') cfmPw!: TemplateRef<any>
  form!: FormGroup
  emailForm!: FormGroup
  pwForm!: FormGroup
  cfmForm!: FormGroup
  clientSub: Subscription = new Subscription
  client!: Client
  editing: boolean = false
  changeEmail: boolean = false
  gst: number = environment.gst
  tableCols: string[] = ['title', 'info']
  hide: boolean = true
  hide2: boolean = true
  hide3: boolean = true
  kitchen: string = ''
  changing: string = ''

  ngOnInit(): void {
    this.clientSub = this.clientStore.getClient.subscribe({
      next: (value) => this.client = value
    })

    this.clientSvc.getKitchen()
      .then(value => this.kitchen = value.username)

    this.form = this.fb.group({
      estName: this.fb.control<string>(this.client.estName, [Validators.required, Validators.minLength(3), Validators.maxLength(20)]),
      gst: this.fb.control<boolean>(this.client.tax.gst, [Validators.required]),
      svc: this.fb.control<number>(this.client.tax.svc, [Validators.required, Validators.min(0)])
    })

    this.emailForm = this.fb.group({
      email: this.fb.control<string>(this.client.email, [Validators.email, Validators.required])
    })
  }

  ngOnDestroy(): void {
    this.clientSub.unsubscribe()
  }

  emailChangeCheck(): boolean {
    return this.emailForm.invalid || this.emailForm.value.email === this.client.email
  }

  cancel() {
    const original = {
      estName: this.client.estName,
      gst: this.client.tax.gst,
      svc: this.client.tax.svc
    }
    this.form.patchValue(original)
    this.editing = false
  }

  done() {
    this.editing = false
    const client = {
      estName: this.form.value.estName,
      tax: {
        gst: this.form.value.gst,
        svc: this.form.value.svc
      }
    }
    this.clientSvc.putClient(client)
      .then(value => {
        this.form.patchValue(value)
        this.clientStore.editClient(value)
      })
  }

  emailCancel() {
    this.changeEmail = false
    this.emailForm.patchValue(this.client)
  }

  emailDone() {
    this.changeEmail = false
    this.confirmPw()
      .then(value => {
        if (value) {
          const login: Login = {
            email: this.emailForm.value.email,
            pw: this.cfmForm.value.pw
          }
          this.clientSvc.putEmail(login)
            .then(value => {
              this.client.email = this.emailForm.value
              this.clientStore.editClient(this.client)
              localStorage.setItem('prixyToken', value.token)
              this.cfmForm.reset()
              this.hide = true
              this.clientSvc.openSnackBar('Email changed successfully')
            })
            .catch(err => {
              this.emailForm.patchValue(this.client)
              alert(!!err.error ? err.error.error : 'Something went wrong')
            })
        } else {
          alert('You have to confirm password to change email.')
          this.emailForm.patchValue(this.client)
        }
      })
  }

  confirmPw(): Promise<boolean> {
    this.cfmForm = this.fb.group({
      pw: this.fb.control<string>('', Validators.required),
    })
    return firstValueFrom(this.dialog.open(this.cfmPw).afterClosed())
  }

  changePw(account: string) {
    this.changing = account
    this.pwForm = this.fb.group({
      oldPw: this.fb.control<string>('', Validators.required),
      pw: this.fb.control<string>('', [Validators.required, Validators.pattern(new RegExp('^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$'))]),
      pw2: this.fb.control<string>('', [Validators.required, c => this.pwMatch(c)]),
    })
    this.dialog.open(this.pw)
  }

  pwMatch: ValidatorFn = (_control: AbstractControl): ValidationErrors | null => {
    const pw = this.pwForm?.controls['pw'].value
    const pw2 = this.pwForm?.controls['pw2'].value
    return pw === pw2 ? null : { notMatched: true }
  }

  pwDone() {
    var change: Login = {
      'email': this.client.email,
      'pw': this.pwForm.value.oldPw,
      'change': this.pwForm.value.pw
    }

    if (this.changing == 'kitchen')
      change.email = this.kitchen

    this.clientSvc.putPassword(change)
      .then(() => {
        this.clientSvc.openSnackBar(`Password for ${this.changing} changed successfully`)
        this.resetPwForm()
        this.dialog.closeAll()
      })
      .catch(err => alert(!!err.error ? err.error.error : 'Something went wrong'))
  }

  createKitchenAccount() {
    this.clientSvc.postKitchen()
      .then(value => {
        this.kitchen = value.username
        alert('Kitchen account created, password has been sent to your email')
      })
  }

  resetPwForm() {
    this.pwForm.reset()
    this.hide = true
    this.hide2 = true
    this.hide3 = true
  }
}
