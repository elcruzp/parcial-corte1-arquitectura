import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, Offering, OfferingPayload, Person } from './api.service';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-offerings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatSnackBarModule,
    NgIf
  ],
  template: `
    <div class="page-header">
      <div>
        <h2>Ofrendas</h2>
        <p class="muted">Registro de ofrendas y pagos asociados</p>
      </div>
    </div>
    <mat-card>
      <h3>Registrar ofrenda</h3>
      <p class="muted">Se generará un pago automáticamente.</p>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline">
          <mat-label>Persona</mat-label>
          <mat-select formControlName="personId">
            <mat-option *ngFor="let p of people" [value]="p.id">
              {{ p.firstName }} {{ p.lastName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Monto</mat-label>
          <input matInput formControlName="amount" type="number" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Concepto</mat-label>
          <input matInput formControlName="concept" />
        </mat-form-field>
        <div class="card-actions">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Crear ofrenda
          </button>
        </div>
      </form>
    </mat-card>

    <mat-card *ngIf="offerings.length; else emptyState">
      <h3>Listado</h3>
      <div class="table-wrap">
        <table mat-table [dataSource]="offerings" aria-label="Listado de ofrendas">
        <ng-container matColumnDef="person">
          <th mat-header-cell *matHeaderCellDef>Persona</th>
          <td mat-cell *matCellDef="let row">{{ row.personName }}</td>
        </ng-container>
        <ng-container matColumnDef="concept">
          <th mat-header-cell *matHeaderCellDef>Concepto</th>
          <td mat-cell *matCellDef="let row">{{ row.concept }}</td>
        </ng-container>
        <ng-container matColumnDef="amount">
          <th mat-header-cell *matHeaderCellDef>Monto</th>
          <td mat-cell *matCellDef="let row">{{ row.amount }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Estado</th>
          <td mat-cell *matCellDef="let row">
            <span class="status" [class.ok]="row.status === 'REGISTRADA'">{{ row.status }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="payment">
          <th mat-header-cell *matHeaderCellDef>Pago</th>
          <td mat-cell *matCellDef="let row">{{ row.paymentStatus || '-' }}</td>
        </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    </mat-card>

    <ng-template #emptyState>
      <mat-card class="empty">
        <h3>Sin ofrendas</h3>
        <p class="muted">Registra una ofrenda para generar su pago.</p>
      </mat-card>
    </ng-template>
  `,
  styles: [`
    mat-card {
      margin-bottom: 16px;
      padding: 16px;
    }
    form {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px;
    }
    .table-wrap {
      width: 100%;
      overflow-x: auto;
    }
    .status {
      display: inline-flex;
      padding: 2px 8px;
      border-radius: 999px;
      font-size: 12px;
      background: #fee2e2;
      color: #991b1b;
    }
    .status.ok {
      background: #dcfce7;
      color: #166534;
    }
    .empty {
      text-align: center;
    }
  `]
})
export class OfferingsComponent implements OnInit {
  people: Person[] = [];
  offerings: Offering[] = [];
  columns = ['person', 'concept', 'amount', 'status', 'payment'];
  form = this.fb.group({
    personId: [null, Validators.required],
    amount: [0, [Validators.required, Validators.min(1)]],
    concept: ['', Validators.required]
  });

  constructor(private fb: FormBuilder, private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.listPeople().subscribe({ next: (res) => (this.people = res) });
    this.api.listOfferings().subscribe({
      next: (res) => (this.offerings = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar ofrendas';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const payload: OfferingPayload = {
      personId: raw.personId ?? 0,
      amount: raw.amount ?? 0,
      concept: raw.concept ?? ''
    };
    this.api.createOffering(payload).subscribe({
      next: () => {
        this.snack.open('Ofrenda creada', 'Cerrar', { duration: 3000 });
        this.form.reset();
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo crear ofrenda';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
