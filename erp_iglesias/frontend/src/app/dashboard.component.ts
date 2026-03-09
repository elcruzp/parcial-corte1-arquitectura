import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService, Dashboard } from './api.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [NgIf, MatCardModule, MatSnackBarModule, MatIconModule],
  template: `
    <div class="page-header">
      <div>
        <h2>Dashboard</h2>
        <p class="muted">Resumen operativo de la parroquia</p>
      </div>
    </div>

    <div class="grid" *ngIf="data">
      <mat-card class="stat-card">
        <mat-icon aria-hidden="true">people</mat-icon>
        <div>
          <div class="label">Total personas</div>
          <div class="hint">Registradas en la iglesia</div>
          <div class="value">{{ data.totalPeople }}</div>
        </div>
      </mat-card>
      <mat-card class="stat-card">
        <mat-icon aria-hidden="true">school</mat-icon>
        <div>
          <div class="label">Cursos activos</div>
          <div class="hint">Disponibles para inscripción</div>
          <div class="value">{{ data.activeCourses }}</div>
        </div>
      </mat-card>
      <mat-card class="stat-card">
        <mat-icon aria-hidden="true">volunteer_activism</mat-icon>
        <div>
          <div class="label">Ofrendas del mes</div>
          <div class="hint">Registradas en el mes actual</div>
          <div class="value">{{ data.offeringsMonth }}</div>
        </div>
      </mat-card>
      <mat-card class="stat-card">
        <mat-icon aria-hidden="true">payments</mat-icon>
        <div>
          <div class="label">Pagos pendientes</div>
          <div class="hint">En estado iniciado</div>
          <div class="value">{{ data.pendingPayments }}</div>
        </div>
      </mat-card>
    </div>
  `,
  styles: [`
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
    }
    .stat-card {
      padding: 18px;
      display: flex;
      gap: 12px;
      align-items: center;
    }
    .label {
      font-size: 13px;
      color: #6b7280;
    }
    .hint {
      font-size: 12px;
      color: #9aa3b2;
    }
    .value {
      font-size: 24px;
      font-weight: 600;
    }
  `]
})
export class DashboardComponent implements OnInit {
  data: Dashboard | null = null;

  constructor(private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.api.dashboard().subscribe({
      next: (res) => (this.data = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar el dashboard';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
