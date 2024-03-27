import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ClientService } from '../service/client.service';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  private fb: FormBuilder = inject(FormBuilder)
  private clientSvc: ClientService = inject(ClientService)
  private router: Router = inject(Router)
  private authSvc: AuthService = inject(AuthService)

  form!: FormGroup
  signupForm!: FormGroup
  isSignup: boolean = false
  hide: boolean = true
  hide2: boolean = true
  isLogin: boolean = false

  ngOnInit(): void {
    this.clientSvc.checkAuth()
      .then(() => this.router.navigate(['/main']))
      .catch(err => console.log(err))

    this.isSignup = false
    this.form = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      pw: this.fb.control<string>('', [Validators.required])
    })
  }

  newSignup() {
    this.isSignup = true
    this.hide = true
    this.signupForm = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      pw: this.fb.control<string>('', [Validators.required, Validators.pattern(new RegExp('^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$'))]),
      pw2: this.fb.control<string>('', [Validators.required, c => this.pwMatch(c)]),
      estName: this.fb.control<string>('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)])
    })
  }

  pwMatch: ValidatorFn = (_control: AbstractControl): ValidationErrors | null => {
    const pw = this.signupForm?.controls['pw'].value
    const pw2 = this.signupForm?.controls['pw2'].value
    return pw === pw2 ? null : { notMatched: true }
  }

  signup() {
    this.authSvc.signup(this.signupForm.value)
      .then(() => {
        alert('Account created successfully.\nPlease log in.')
        this.isSignup = false
      })
      .catch((err) => alert(err.error.error))
  }

  login() {
    this.isLogin = true
    this.authSvc.login(this.form.value)
      .then(value => localStorage.setItem('prixyToken', value.token))
      .then(() => this.router.navigate(['/main']))
      .catch(err => {
        this.isLogin = false
        alert(err.error.error)
      })
  }
}