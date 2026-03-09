import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from './api.service';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-users',
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
    <div class="page-header">
      <div>
        <h2>Usuarios</h2>
        <p class="muted">Gestiona accesos para clientes</p>
      </div>
    </div>

    <mat-card>
      <h3>Crear usuario Client</h3>
      <p class="muted">El usuario se crea con rol Client por defecto.</p>
      <mat-divider></mat-divider>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" type="email" autocomplete="username" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Contraseña</mat-label>
          <input matInput formControlName="password" type="password" autocomplete="new-password" />
        </mat-form-field>
        <div class="card-actions">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Crear
          </button>
        </div>
      </form>
    </mat-card>
  `,
  styles: [`
    mat-card {
      padding: 16px;
      max-width: 420px;
    }
    mat-divider {
      margin: 8px 0 16px;
    }
    form {
      display: grid;
      gap: 12px;
    }
  `]
})
export class UsersComponent {
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor(private fb: FormBuilder, private api: ApiService, private snack: MatSnackBar) {}

  submit() {
    if (this.form.invalid) {
      return;
    }
    const { email, password } = this.form.getRawValue();
    this.api.createUser(email!, password!).subscribe({
      next: () => {
        this.snack.open('Usuario creado', 'Cerrar', { duration: 3000 });
        this.form.reset();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo crear el usuario';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
