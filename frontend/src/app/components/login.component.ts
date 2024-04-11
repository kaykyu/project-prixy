import { Component, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  private fb: FormBuilder = inject(FormBuilder)
  private router: Router = inject(Router)
  private authSvc: AuthService = inject(AuthService)
  private dialog: MatDialog = inject(MatDialog)

  @ViewChild('email') input!: TemplateRef<any>
  form!: FormGroup
  signupForm!: FormGroup
  emailForm!: FormGroup
  isSignup: boolean = false
  hide: boolean = true
  hide2: boolean = true
  isLogin: boolean = false

  ngOnInit(): void {
    this.isSignup = false
    this.form = this.fb.group({
      email: this.fb.control<string>('', [Validators.required]),
      pw: this.fb.control<string>('', [Validators.required])
    })
  }

  newSignup() {
    this.isSignup = true
    this.hide = true
    this.signupForm = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      estName: this.fb.control<string>('', [Validators.required, Validators.minLength(3), Validators.maxLength(20)])
    })
  }

  signup() {
    this.authSvc.signup(this.signupForm.value)
      .then(() => {
        alert('A password has been sent to your email. Please check your email and sign in within 24 hours.')
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

  forgotPw() {
    this.emailForm = this.fb.group({
      email: this.fb.control<string>(this.form.value.email, [Validators.required, Validators.email])
    })
    this.dialog.open(this.input)
  }

  resetPw() {
    this.authSvc.resetPw(this.emailForm.value.email)
      .then(() => {
        alert('New password has been sent to your email.')
        this.dialog.closeAll()
      })
      .catch(err => alert(err.error ? err.error.error : 'Something went wrong'))
  }
}