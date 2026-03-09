import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, Course, Enrollment, EnrollmentPayload, Person } from './api.service';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-enrollments',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatSnackBarModule,
    NgIf
  ],
  template: `
    <div class="page-header">
      <div>
        <h2>Inscripciones</h2>
        <p class="muted">Asigna personas a cursos y gestiona pagos</p>
      </div>
    </div>
    <mat-card>
      <h3>Asignar persona a curso</h3>
      <p class="muted">Genera una pre-inscripción con pago pendiente.</p>
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
          <mat-label>Curso</mat-label>
          <mat-select formControlName="courseId">
            <mat-option *ngFor="let c of courses" [value]="c.id">
              {{ c.name }} - {{ c.price | number: '1.2-2' }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <div class="card-actions">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Crear inscripción
          </button>
        </div>
      </form>
    </mat-card>

    <mat-card *ngIf="enrollments.length; else emptyState">
      <h3>Listado</h3>
      <div class="table-wrap">
        <table mat-table [dataSource]="enrollments" aria-label="Listado de inscripciones">
        <ng-container matColumnDef="person">
          <th mat-header-cell *matHeaderCellDef>Persona</th>
          <td mat-cell *matCellDef="let row">{{ row.personName }}</td>
        </ng-container>
        <ng-container matColumnDef="course">
          <th mat-header-cell *matHeaderCellDef>Curso</th>
          <td mat-cell *matCellDef="let row">{{ row.courseName }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Estado</th>
          <td mat-cell *matCellDef="let row">
            <span class="status" [class.ok]="row.status === 'PAGADA'">{{ row.status }}</span>
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
        <h3>Sin inscripciones</h3>
        <p class="muted">Registra personas y cursos para crear inscripciones.</p>
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
export class EnrollmentsComponent implements OnInit {
  people: Person[] = [];
  courses: Course[] = [];
  enrollments: Enrollment[] = [];
  columns = ['person', 'course', 'status', 'payment'];
  form = this.fb.group({
    personId: [null, Validators.required],
    courseId: [null, Validators.required]
  });

  constructor(private fb: FormBuilder, private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.listPeople().subscribe({ next: (res) => (this.people = res) });
    this.api.listCourses().subscribe({ next: (res) => (this.courses = res) });
    this.api.listEnrollments().subscribe({
      next: (res) => (this.enrollments = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar inscripciones';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const payload: EnrollmentPayload = {
      personId: raw.personId ?? 0,
      courseId: raw.courseId ?? 0
    };
    this.api.createEnrollment(payload).subscribe({
      next: () => {
        this.snack.open('Inscripción creada', 'Cerrar', { duration: 3000 });
        this.form.reset();
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo crear inscripción';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
