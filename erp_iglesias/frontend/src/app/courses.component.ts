import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, Course, CoursePayload } from './api.service';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-courses',
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
        <h2>Cursos</h2>
        <p class="muted">Catálogo de cursos disponibles</p>
      </div>
    </div>
    <mat-card>
      <h3>Crear curso</h3>
      <p class="muted">Define el precio y la descripción.</p>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline">
          <mat-label>Nombre</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Descripción</mat-label>
          <input matInput formControlName="description" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Precio</mat-label>
          <input matInput formControlName="price" type="number" />
        </mat-form-field>
        <div class="card-actions">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">
            Guardar
          </button>
        </div>
      </form>
    </mat-card>

    <mat-card *ngIf="courses.length; else emptyState">
      <h3>Listado</h3>
      <div class="table-wrap">
        <table mat-table [dataSource]="courses" aria-label="Listado de cursos">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Curso</th>
          <td mat-cell *matCellDef="let row">{{ row.name }}</td>
        </ng-container>
        <ng-container matColumnDef="price">
          <th mat-header-cell *matHeaderCellDef>Precio</th>
          <td mat-cell *matCellDef="let row">{{ row.price | number: '1.2-2' }}</td>
        </ng-container>
        <ng-container matColumnDef="active">
          <th mat-header-cell *matHeaderCellDef>Activo</th>
          <td mat-cell *matCellDef="let row">
            <span class="status" [class.ok]="row.active">{{ row.active ? 'Sí' : 'No' }}</span>
          </td>
        </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    </mat-card>

    <ng-template #emptyState>
      <mat-card class="empty">
        <h3>Sin cursos</h3>
        <p class="muted">Crea el primer curso para habilitar inscripciones.</p>
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
export class CoursesComponent implements OnInit {
  courses: Course[] = [];
  columns = ['name', 'price', 'active'];
  form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    price: [0, [Validators.required, Validators.min(0)]]
  });

  constructor(private fb: FormBuilder, private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.listCourses().subscribe({
      next: (res) => (this.courses = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar cursos';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }
    const raw = this.form.getRawValue();
    const payload: CoursePayload = {
      name: raw.name ?? '',
      description: raw.description ?? '',
      price: raw.price ?? 0
    };
    this.api.createCourse(payload).subscribe({
      next: () => {
        this.snack.open('Curso creado', 'Cerrar', { duration: 3000 });
        this.form.reset({ name: '', description: '', price: 0 });
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo crear curso';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
