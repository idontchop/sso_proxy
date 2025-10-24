# Angular Example Components

## Complete Login Component with Styling

```typescript
// login.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-container">
        <div class="login-header">
          <h1>🔐 SSO Proxy</h1>
          <p>Please sign in to continue</p>
        </div>
        
        <div *ngIf="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>
        
        <form (ngSubmit)="onLogin()" #loginForm="ngForm">
          <div class="form-group">
            <label for="username">Username</label>
            <input 
              type="text" 
              id="username" 
              name="username"
              [(ngModel)]="credentials.username" 
              required 
              placeholder="Enter your username"
              #usernameInput="ngModel">
            <div *ngIf="usernameInput.invalid && usernameInput.touched" class="error-text">
              Username is required
            </div>
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input 
              type="password" 
              id="password" 
              name="password"
              [(ngModel)]="credentials.password" 
              required 
              placeholder="Enter your password"
              #passwordInput="ngModel">
            <div *ngIf="passwordInput.invalid && passwordInput.touched" class="error-text">
              Password is required
            </div>
          </div>
          
          <button 
            type="submit" 
            class="btn-primary" 
            [disabled]="loginForm.invalid || loading">
            <span *ngIf="!loading">Sign In</span>
            <span *ngIf="loading">Signing in...</span>
          </button>
        </form>
        
        <div class="test-credentials">
          <p><strong>Test credentials:</strong></p>
          <p>Username: <code>admin</code> | Password: <code>password</code></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }
    
    .login-container {
      background: white;
      padding: 2rem;
      border-radius: 10px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      width: 100%;
      max-width: 400px;
    }
    
    .login-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    
    .login-header h1 {
      margin: 0;
      color: #333;
      font-size: 2rem;
    }
    
    .login-header p {
      margin: 0.5rem 0 0;
      color: #666;
    }
    
    .form-group {
      margin-bottom: 1.5rem;
    }
    
    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      color: #333;
      font-weight: 500;
    }
    
    .form-group input {
      width: 100%;
      padding: 0.75rem;
      border: 2px solid #e1e1e1;
      border-radius: 5px;
      font-size: 1rem;
      transition: border-color 0.3s ease;
      box-sizing: border-box;
    }
    
    .form-group input:focus {
      outline: none;
      border-color: #667eea;
    }
    
    .form-group input.ng-invalid.ng-touched {
      border-color: #dc3545;
    }
    
    .error-text {
      color: #dc3545;
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }
    
    .btn-primary {
      width: 100%;
      padding: 0.75rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 5px;
      font-size: 1rem;
      font-weight: 500;
      cursor: pointer;
      transition: transform 0.2s ease;
    }
    
    .btn-primary:hover:not(:disabled) {
      transform: translateY(-2px);
    }
    
    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    
    .alert {
      padding: 0.75rem;
      margin-bottom: 1rem;
      border-radius: 5px;
      font-size: 0.9rem;
    }
    
    .alert-error {
      background-color: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
    }
    
    .test-credentials {
      margin-top: 2rem;
      padding-top: 1rem;
      border-top: 1px solid #e1e1e1;
      text-align: center;
      font-size: 0.875rem;
      color: #666;
    }
    
    .test-credentials code {
      background: #f8f9fa;
      padding: 0.2rem 0.5rem;
      border-radius: 3px;
      font-family: monospace;
    }
  `]
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onLogin(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Login successful', response);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Login failed', error);
        this.errorMessage = 'Invalid username or password. Please try again.';
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
```

## Dashboard Component Example

```typescript
// dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard">
      <nav class="navbar">
        <h1>SSO Proxy Dashboard</h1>
        <div class="user-info">
          <span *ngIf="user">Welcome, {{ user.username }}!</span>
          <button class="btn-logout" (click)="onLogout()">Logout</button>
        </div>
      </nav>
      
      <div class="container">
        <div class="welcome-card">
          <h2>Authentication Successful</h2>
          <p>You have successfully logged into the SSO Proxy system.</p>
          <span class="status">Active Session</span>
        </div>
        
        <div *ngIf="user" class="user-details">
          <h3>Your Profile</h3>
          <p><strong>Username:</strong> {{ user.username }}</p>
          <p><strong>Email:</strong> {{ user.email }}</p>
          <p><strong>Role:</strong> {{ user.role }}</p>
          <p><strong>Status:</strong> 
            <span [class.active]="user.enabled" [class.inactive]="!user.enabled">
              {{ user.enabled ? 'Active' : 'Inactive' }}
            </span>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    .navbar {
      background: rgba(255, 255, 255, 0.95);
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }
    
    .navbar h1 {
      margin: 0;
      color: #333;
      font-size: 1.5rem;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    
    .user-info span {
      color: #333;
      font-weight: 500;
    }
    
    .btn-logout {
      padding: 0.5rem 1rem;
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 0.9rem;
      transition: background-color 0.3s ease;
    }
    
    .btn-logout:hover {
      background: #c82333;
    }
    
    .container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 2rem;
    }
    
    .welcome-card, .user-details {
      background: white;
      padding: 2rem;
      border-radius: 10px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      margin-bottom: 2rem;
    }
    
    .welcome-card {
      text-align: center;
    }
    
    .welcome-card h2 {
      margin: 0 0 1rem;
      color: #333;
    }
    
    .welcome-card p {
      margin: 0 0 1rem;
      color: #666;
    }
    
    .status {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      background: #28a745;
      color: white;
      border-radius: 15px;
      font-size: 0.8rem;
      font-weight: 500;
    }
    
    .user-details h3 {
      margin: 0 0 1rem;
      color: #333;
    }
    
    .user-details p {
      margin: 0.5rem 0;
      color: #666;
    }
    
    .active {
      color: #28a745;
      font-weight: 600;
    }
    
    .inactive {
      color: #dc3545;
      font-weight: 600;
    }
  `]
})
export class DashboardComponent implements OnInit {
  user: any = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.user$.subscribe(user => {
      this.user = user;
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
```

## App Routes Configuration

```typescript
// app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/dashboard' }
];
```

## Functional Auth Interceptor (Angular 17+)

```typescript
// auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }
  
  return next(req);
};
```

## Testing with HttpClient

```typescript
// Test the API directly from Angular
this.http.post('http://localhost:8080/api/auth/login', {
  username: 'admin',
  password: 'password'
}).subscribe(response => {
  console.log('Login response:', response);
});
```
