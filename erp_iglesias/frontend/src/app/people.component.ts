import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, Person, PersonPayload } from './api.service';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-people',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatSnackBarModule,
    NgIf
  ],
  template: `
    <div class="page-header">
      <div>
        <h2>Personas</h2>
        <p class="muted">Base de datos de personas de la iglesia</p>
      </div>
    </div>
    <mat-card>
      <h3>Registrar persona</h3>
      <p class="muted">Campos obligatorios: nombres y apellidos.</p>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline">
          <mat-label>Nombres</mat-label>
          <input matInput formControlName="firstName" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Apellidos</mat-label>
          <input matInput formControlName="lastName" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Documento</mat-label>
          <input matInput formControlName="document" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Teléfono</mat-label>
          <input matInput formControlName="phone" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" type="email" />
        </mat-form-field>
        <div class="card-actions">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Guardar
          </button>
        </div>
      </form>
    </mat-card>

    <mat-card *ngIf="people.length; else emptyState">
      <h3>Listado</h3>
      <div class="table-wrap">
        <table mat-table [dataSource]="people" aria-label="Listado de personas">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Nombre</th>
          <td mat-cell *matCellDef="let row">{{ row.firstName }} {{ row.lastName }}</td>
        </ng-container>
        <ng-container matColumnDef="document">
          <th mat-header-cell *matHeaderCellDef>Documento</th>
          <td mat-cell *matCellDef="let row">{{ row.document || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="contact">
          <th mat-header-cell *matHeaderCellDef>Contacto</th>
          <td mat-cell *matCellDef="let row">{{ row.phone || row.email || '-' }}</td>
        </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    </mat-card>

    <ng-template #emptyState>
      <mat-card class="empty">
        <h3>Sin personas registradas</h3>
        <p class="muted">Agrega la primera persona para empezar.</p>
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
    .empty {
      text-align: center;
    }
  `]
})
export class PeopleComponent implements OnInit {
  people: Person[] = [];
  columns = ['name', 'document', 'contact'];
  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    document: [''],
    phone: [''],
    email: ['']
  });

  constructor(private fb: FormBuilder, private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.listPeople().subscribe({
      next: (res) => (this.people = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar personas';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const payload: PersonPayload = {
      firstName: raw.firstName ?? '',
      lastName: raw.lastName ?? '',
      document: raw.document ?? '',
      phone: raw.phone ?? '',
      email: raw.email ?? ''
    };
    this.api.createPerson(payload).subscribe({
      next: () => {
        this.snack.open('Persona registrada', 'Cerrar', { duration: 3000 });
        this.form.reset();
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo registrar persona';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
