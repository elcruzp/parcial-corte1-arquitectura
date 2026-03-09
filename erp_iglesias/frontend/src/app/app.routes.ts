import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';
import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';
import { ChurchComponent } from './church.component';
import { UsersComponent } from './users.component';
import { PeopleComponent } from './people.component';
import { CoursesComponent } from './courses.component';
import { EnrollmentsComponent } from './enrollments.component';
import { OfferingsComponent } from './offerings.component';
import { PaymentsComponent } from './payments.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'church', component: ChurchComponent, canActivate: [authGuard] },
  { path: 'users', component: UsersComponent, canActivate: [authGuard] },
  { path: 'people', component: PeopleComponent, canActivate: [authGuard] },
  { path: 'courses', component: CoursesComponent, canActivate: [authGuard] },
  { path: 'enrollments', component: EnrollmentsComponent, canActivate: [authGuard] },
  { path: 'offerings', component: OfferingsComponent, canActivate: [authGuard] },
  { path: 'payments', component: PaymentsComponent, canActivate: [authGuard] }
];
