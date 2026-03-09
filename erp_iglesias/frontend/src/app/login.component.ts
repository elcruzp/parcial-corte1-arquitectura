import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  template: `
    <div class="login">
      <mat-card>
        <div class="header">
          <div class="title">IglesiAdmin</div>
          <div class="subtitle">Gestión integral de parroquias</div>
        </div>
        <mat-divider></mat-divider>
        <h2>Ingresar</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" type="email" autocomplete="username" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Contraseña</mat-label>
            <input matInput formControlName="password" type="password" autocomplete="current-password" />
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading">
            Entrar
          </button>
        </form>
      </mat-card>
    </div>
  `,
  styles: [`
    .login {
      display: flex;
      justify-content: center;
      width: 100%;
    }
    mat-card {
      width: 380px;
      padding: 28px;
      display: grid;
      gap: 16px;
    }
    .header {
      display: grid;
      gap: 4px;
    }
    .title {
      font-size: 20px;
      font-weight: 700;
    }
    .subtitle {
      font-size: 13px;
      color: #6b7280;
    }
    form {
      display: grid;
      gap: 12px;
    }
  `]
})
export class LoginComponent {
  loading = false;
  form = this.fb.group({
    email: ['admin@parroquia.com', [Validators.required, Validators.email]],
    password: ['Admin123!', [Validators.required]]
  });

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  submit() {
    if (this.form.invalid || this.loading) {
      return;
    }
    this.loading = true;
    const { email, password } = this.form.getRawValue();
    this.api.login(email!, password!).subscribe({
      next: (res) => {
        this.auth.setAuth(res);
        this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        const message = err?.error?.message || 'Error al iniciar sesión';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }
}
