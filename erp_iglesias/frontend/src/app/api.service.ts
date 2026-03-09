import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, { email, password });
  }

  getChurch() {
    return this.http.get<Church>(`${this.baseUrl}/church`);
  }

  createChurch(name: string, address: string) {
    return this.http.post<Church>(`${this.baseUrl}/church`, { name, address });
  }

  createUser(email: string, password: string) {
    return this.http.post<User>(`${this.baseUrl}/users`, { email, password });
  }

  listPeople() {
    return this.http.get<Person[]>(`${this.baseUrl}/people`);
  }

  createPerson(payload: PersonPayload) {
    return this.http.post<Person>(`${this.baseUrl}/people`, payload);
  }

  listCourses() {
    return this.http.get<Course[]>(`${this.baseUrl}/courses`);
  }

  createCourse(payload: CoursePayload) {
    return this.http.post<Course>(`${this.baseUrl}/courses`, payload);
  }

  listEnrollments() {
    return this.http.get<Enrollment[]>(`${this.baseUrl}/enrollments`);
  }

  createEnrollment(payload: EnrollmentPayload) {
    return this.http.post<Enrollment>(`${this.baseUrl}/enrollments`, payload);
  }

  listOfferings() {
    return this.http.get<Offering[]>(`${this.baseUrl}/offerings`);
  }

  createOffering(payload: OfferingPayload) {
    return this.http.post<Offering>(`${this.baseUrl}/offerings`, payload);
  }

  listPayments(status?: string) {
    const params = status ? `?status=${status}` : '';
    return this.http.get<Payment[]>(`${this.baseUrl}/payments${params}`);
  }

  confirmPayment(id: number) {
    return this.http.post<Payment>(`${this.baseUrl}/payments/${id}/confirm`, {});
  }

  failPayment(id: number) {
    return this.http.post<Payment>(`${this.baseUrl}/payments/${id}/fail`, {});
  }

  retryPayment(id: number) {
    return this.http.post<Payment>(`${this.baseUrl}/payments/${id}/retry`, {});
  }

  dashboard() {
    return this.http.get<Dashboard>(`${this.baseUrl}/dashboard`);
  }
}

export interface LoginResponse {
  token: string;
  email: string;
  role: string;
}

export interface Church {
  id: number;
  name: string;
  address: string;
}

export interface User {
  id: number;
  email: string;
  role: string;
}

export interface Person {
  id: number;
  firstName: string;
  lastName: string;
  document?: string;
  phone?: string;
  email?: string;
}

export interface PersonPayload {
  firstName: string;
  lastName: string;
  document?: string;
  phone?: string;
  email?: string;
}

export interface Course {
  id: number;
  name: string;
  description?: string;
  price: number;
  active: boolean;
}

export interface CoursePayload {
  name: string;
  description?: string;
  price: number;
}

export interface Enrollment {
  id: number;
  personId: number;
  personName: string;
  courseId: number;
  courseName: string;
  status: string;
  paymentId?: number;
  paymentStatus?: string;
}

export interface EnrollmentPayload {
  personId: number;
  courseId: number;
}

export interface Offering {
  id: number;
  personId: number;
  personName: string;
  concept: string;
  amount: string;
  status: string;
  paymentId?: number;
  paymentStatus?: string;
}

export interface OfferingPayload {
  personId: number;
  amount: number;
  concept: string;
}

export interface Payment {
  id: number;
  type: string;
  status: string;
  amount: string;
  attempts: number;
  referenceId: number;
}

export interface Dashboard {
  totalPeople: number;
  activeCourses: number;
  offeringsMonth: number;
  pendingPayments: number;
}
