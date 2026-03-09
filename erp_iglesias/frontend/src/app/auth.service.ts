import { Injectable, signal } from '@angular/core';

export interface AuthState {
  token: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'iglesia.auth';
  readonly state = signal<AuthState | null>(this.load());

  setAuth(state: AuthState) {
    this.state.set(state);
    localStorage.setItem(this.storageKey, JSON.stringify(state));
  }

  clear() {
    this.state.set(null);
    localStorage.removeItem(this.storageKey);
  }

  get token() {
    const current = this.state();
    return current ? current.token : '';
  }

  get role() {
    const current = this.state();
    return current ? current.role : '';
  }

  get email() {
    const current = this.state();
    return current ? current.email : '';
  }

  get isLoggedIn() {
    return this.state() !== null;
  }

  private load(): AuthState | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthState;
    } catch {
      return null;
    }
  }
}
