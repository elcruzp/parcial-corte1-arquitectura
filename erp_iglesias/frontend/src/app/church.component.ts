import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { ApiService, Church } from './api.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-church',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatDividerModule,
    MatIconModule,
    NgIf
  ],
  template: `
    <div class="page-header">
      <div>
        <h2>Iglesia</h2>
        <p class="muted">Datos base de la parroquia</p>
      </div>
    </div>

    <div class="two-col">
      <mat-card class="info-card" *ngIf="church">
        <div class="info-head">
          <mat-icon aria-hidden="true">church</mat-icon>
          <h3>{{ church.name }}</h3>
        </div>
        <p class="muted">{{ church.address || 'Sin dirección' }}</p>
        <mat-divider></mat-divider>
        <div class="hint">Solo debe existir una iglesia registrada.</div>
      </mat-card>

      <mat-card>
        <h3>Registrar iglesia</h3>
        <p class="muted">Completa los campos y guarda.</p>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput formControlName="name" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Dirección</mat-label>
            <input matInput formControlName="address" />
          </mat-form-field>
          <div class="card-actions">
            <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
              Guardar
            </button>
          </div>
        </form>
      </mat-card>
    </div>
  `,
  styles: [`
    mat-card {
      margin-bottom: 16px;
      padding: 16px;
    }
    .two-col {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      align-items: start;
    }
    .info-head {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }
    .hint {
      font-size: 12px;
      color: #6b7280;
      margin-top: 12px;
    }
    form {
      display: grid;
      gap: 12px;
      max-width: 420px;
    }
  `]
})
export class ChurchComponent implements OnInit {
  church: Church | null = null;
  form = this.fb.group({
    name: ['', Validators.required],
    address: ['']
  });

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {}

  ngOnInit() {
    this.api.getChurch().subscribe({
      next: (res) => (this.church = res),
      error: () => {
        this.church = null;
      }
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }
    const { name, address } = this.form.getRawValue();
    this.api.createChurch(name!, address || '').subscribe({
      next: (res) => {
        this.church = res;
        this.snack.open('Iglesia registrada', 'Cerrar', { duration: 3000 });
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo registrar la iglesia';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
