import { Component, OnInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService, Payment } from './api.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [MatCardModule, MatTableModule, MatButtonModule, MatSnackBarModule, NgIf],
  template: `
    <div class="page-header">
      <div>
        <h2>Pagos</h2>
        <p class="muted">Gestiona confirmaciones y reintentos</p>
      </div>
    </div>

    <mat-card *ngIf="payments.length; else emptyState">
      <div class="table-wrap">
        <table mat-table [dataSource]="payments" aria-label="Listado de pagos">
        <ng-container matColumnDef="id">
          <th mat-header-cell *matHeaderCellDef>ID</th>
          <td mat-cell *matCellDef="let row">{{ row.id }}</td>
        </ng-container>
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Tipo</th>
          <td mat-cell *matCellDef="let row">{{ row.type }}</td>
        </ng-container>
        <ng-container matColumnDef="amount">
          <th mat-header-cell *matHeaderCellDef>Monto</th>
          <td mat-cell *matCellDef="let row">{{ row.amount }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Estado</th>
          <td mat-cell *matCellDef="let row">
            <span class="status" [class.ok]="row.status === 'CONFIRMADO'" [class.warn]="row.status === 'INICIADO'">
              {{ row.status }}
            </span>
          </td>
        </ng-container>
        <ng-container matColumnDef="attempts">
          <th mat-header-cell *matHeaderCellDef>Intentos</th>
          <td mat-cell *matCellDef="let row">{{ row.attempts }}</td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Acciones</th>
          <td mat-cell *matCellDef="let row">
            <button mat-button color="primary" (click)="confirm(row)" [disabled]="row.status === 'CONFIRMADO'" aria-label="Confirmar pago">
              Confirmar
            </button>
            <button mat-button color="warn" (click)="fail(row)" [disabled]="row.status === 'CONFIRMADO'" aria-label="Marcar pago como fallido">
              Fallar
            </button>
            <button mat-button (click)="retry(row)" [disabled]="row.status !== 'FALLIDO' || row.attempts >= 3" aria-label="Reintentar pago">
              Reintentar
            </button>
          </td>
        </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    </mat-card>

    <ng-template #emptyState>
      <mat-card class="empty">
        <h3>Sin pagos</h3>
        <p class="muted">Cuando existan inscripciones u ofrendas aparecerán aquí.</p>
      </mat-card>
    </ng-template>
  `,
  styles: [`
    mat-card {
      padding: 16px;
    }
    .table-wrap {
      width: 100%;
      overflow-x: auto;
    }
    button {
      margin-right: 4px;
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
    .status.warn {
      background: #ffedd5;
      color: #9a3412;
    }
    .empty {
      text-align: center;
    }
  `]
})
export class PaymentsComponent implements OnInit {
  payments: Payment[] = [];
  columns = ['id', 'type', 'amount', 'status', 'attempts', 'actions'];

  constructor(private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.listPayments().subscribe({
      next: (res) => (this.payments = res),
      error: (err) => {
        const message = err?.error?.message || 'No se pudo cargar pagos';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  confirm(payment: Payment) {
    this.api.confirmPayment(payment.id).subscribe({
      next: () => {
        this.snack.open('Pago confirmado', 'Cerrar', { duration: 3000 });
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo confirmar';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  fail(payment: Payment) {
    this.api.failPayment(payment.id).subscribe({
      next: () => {
        this.snack.open('Pago fallido', 'Cerrar', { duration: 3000 });
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo fallar';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }

  retry(payment: Payment) {
    this.api.retryPayment(payment.id).subscribe({
      next: () => {
        this.snack.open('Pago reiniciado', 'Cerrar', { duration: 3000 });
        this.load();
      },
      error: (err) => {
        const message = err?.error?.message || 'No se pudo reintentar';
        this.snack.open(message, 'Cerrar', { duration: 3000 });
      }
    });
  }
}
