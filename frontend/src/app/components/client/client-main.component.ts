import { Component, OnDestroy, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { ClientService } from '../../service/client.service';
import { Client, Login } from '../../models';
import { Observable, Subscription } from 'rxjs';
import { ClientStoreService } from '../../service/client-store.service';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-client-main',
  templateUrl: './client-main.component.html',
  styleUrl: './client-main.component.css'
})
export class ClientMainComponent implements OnInit, OnDestroy {

  private clientSvc: ClientService = inject(ClientService)
  private clientStore: ClientStoreService = inject(ClientStoreService)
  private fb: FormBuilder = inject(FormBuilder)
  private dialog: MatDialog = inject(MatDialog)

  @ViewChild('pw') changePw!: TemplateRef<any>
  client$!: Observable<Client>
  showNav: boolean = false
  links = NAV_LINKS
  pwForm!: FormGroup
  hide: boolean = true
  hide2: boolean = true
  hide3: boolean = true
  roleSub: Subscription = new Subscription
  isAdmin$!: Observable<boolean>

  ngOnInit(): void {
    this.clientInit()
      .catch(value => {
        this.formInit()
        this.dialog.open(this.changePw, { data: value.email })
      })
    
    this.isAdmin$ = this.clientStore.isAdmin
  }

  ngOnDestroy(): void {
    this.roleSub.unsubscribe()
  }

  async clientInit() {
    return this.clientSvc.getClient()
      .then(value => this.clientStore.setClient(value))
      .then(() => this.client$ = this.clientStore.getClient)
  }

  formInit() {
    this.pwForm = this.fb.group({
      oldPw: this.fb.control<string>('', Validators.required),
      pw: this.fb.control<string>('', [Validators.required, Validators.pattern(new RegExp('^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$'))]),
      pw2: this.fb.control<string>('', [Validators.required, c => this.pwMatch(c)]),
    })
  }

  pwMatch: ValidatorFn = (_control: AbstractControl): ValidationErrors | null => {
    const pw = this.pwForm?.controls['pw'].value
    const pw2 = this.pwForm?.controls['pw2'].value
    return pw === pw2 ? null : { notMatched: true }
  }

  pwDone() {
    const change: Login = {
      email: '',
      pw: this.pwForm.value.oldPw,
      change: this.pwForm.value.pw
    }

    this.clientSvc.confirmClient(change)
      .then(value => {
        localStorage.setItem('prixyToken', value.token)
        this.pwForm.reset()
        this.dialog.closeAll()
        this.clientInit()
        this.clientSvc.openSnackBar('Password changed successfully.')
      })
      .catch(err => alert(!!err.error ? err.error.error : 'Something went wrong'))
  }


  logout() {
    localStorage.removeItem('prixyToken')
  }
}

const NAV_LINKS = [
  { path: 'dash', label: 'home', tip: 'Dashboard' },
  { path: 'menu', label: 'restaurant_menu', tip: 'View menu' },
  { path: 'kitchen', label: 'store', tip: 'View orders' },
  { path: 'account', label: 'manage_accounts', tip: 'View Account Details' }
]