# SSO Proxy - Angular Integration Guide

This Spring Boot backend is configured to serve an Angular application with JWT authentication.

## 🎯 Architecture

- **Backend:** Spring Boot 3.5.7 with Spring Security + JWT
- **Frontend:** Angular (served as static resources)
- **Authentication:** Stateless JWT tokens
- **Database:** H2 (dev) / SQL Server (prod)

## 🚀 Quick Start

### 1. Start the Spring Boot Backend

```bash
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### 2. Test the API Endpoints

Use Postman, curl, or your Angular app to test:

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@example.com",
  "role": "USER"
}
```

**Get User Info (with JWT):**
```bash
curl -X GET http://localhost:8080/api/auth/user \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📦 Integrating Your Angular App

### Option A: Production Build (Recommended)

1. **Build your Angular app:**
   ```bash
   cd your-angular-app
   ng build --configuration production
   ```

2. **Copy build files to Spring Boot:**
   ```bash
   # The dist folder structure varies by Angular version
   # For Angular 17+:
   cp -r dist/your-app/browser/* ../sso-proxy/src/main/resources/static/
   
   # For older Angular versions:
   cp -r dist/your-app/* ../sso-proxy/src/main/resources/static/
   ```

3. **Restart Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access your app:**
   Open `http://localhost:8080` - Your Angular app is now served by Spring Boot!

### Option B: Development with Separate Servers

For development, run Angular dev server separately:

1. **Start Spring Boot backend:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Start Angular dev server:**
   ```bash
   cd your-angular-app
   ng serve
   ```

3. **Angular runs on:** `http://localhost:4200`
4. **Backend API on:** `http://localhost:8080`

CORS is already configured for `http://localhost:4200`.

## 🔐 Angular Authentication Setup

### 1. Create Auth Service

```typescript
// auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  username: string;
  email: string;
  role: string;
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'auth_token';
  private userSubject = new BehaviorSubject<UserInfo | null>(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUser();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem(this.tokenKey, response.token);
          this.loadUser();
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.userSubject.next(null);
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.apiUrl}/user`);
  }

  private loadUser(): void {
    if (this.isLoggedIn()) {
      this.getCurrentUser().subscribe({
        next: (user) => this.userSubject.next(user),
        error: () => this.logout()
      });
    }
  }
}
```

### 2. Create HTTP Interceptor

```typescript
// auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      const cloned = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      });
      return next.handle(cloned);
    }
    
    return next.handle(req);
  }
}
```

### 3. Create Auth Guard

```typescript
// auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      return true;
    }
    
    this.router.navigate(['/login']);
    return false;
  }
}
```

### 4. Register in app.config.ts (Angular 17+)

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
```

### 5. Create Login Component

```typescript
// login.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h2>Login</h2>
      <form (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="username">Username:</label>
          <input type="text" id="username" [(ngModel)]="credentials.username" 
                 name="username" required>
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" [(ngModel)]="credentials.password" 
                 name="password" required>
        </div>
        <button type="submit">Login</button>
        <div *ngIf="errorMessage" class="error">{{ errorMessage }}</div>
      </form>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 400px;
      margin: 50px auto;
      padding: 20px;
      border: 1px solid #ddd;
      border-radius: 5px;
    }
    .form-group {
      margin-bottom: 15px;
    }
    label {
      display: block;
      margin-bottom: 5px;
    }
    input {
      width: 100%;
      padding: 8px;
      box-sizing: border-box;
    }
    button {
      width: 100%;
      padding: 10px;
      background: #007bff;
      color: white;
      border: none;
      cursor: pointer;
    }
    .error {
      color: red;
      margin-top: 10px;
    }
  `]
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.authService.login(this.credentials).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => this.errorMessage = 'Invalid username or password'
    });
  }
}
```

## 🔒 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | Login with username/password | No |
| POST | `/api/auth/logout` | Logout current user | No |
| GET | `/api/auth/user` | Get current user info | Yes |
| GET | `/api/auth/validate` | Validate JWT token | Yes |

### Request/Response Examples

**Login Request:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMzA0ODgwMCwiZXhwIjoxNjMzMTM1MjAwfQ...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@example.com",
  "role": "USER"
}
```

**User Info Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "USER",
  "enabled": true
}
```

## 🔑 Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | password | USER |
| user | password | USER |

## ⚙️ Configuration

### JWT Settings (application.properties)

```properties
# JWT token secret (change in production!)
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437

# JWT expiration time in milliseconds (24 hours)
jwt.expiration=86400000
```

### CORS Settings

CORS is configured for:
- `http://localhost:4200` (Angular dev server)
- `http://localhost:8080` (Production)

## 🎨 Angular Routing

Spring Boot is configured to forward all non-API routes to Angular's `index.html`, so Angular routing works seamlessly!

Routes like `/dashboard`, `/profile`, etc. will be handled by Angular.

## 📝 Notes

- **JWT Token:** Stored in localStorage by default (consider httpOnly cookies for production)
- **Token Expiration:** 24 hours (configurable in application.properties)
- **Session Management:** Stateless (no server-side sessions)
- **CSRF:** Disabled (using JWT tokens)
- **Database:** H2 in-memory (data resets on restart)

## 🚢 Production Deployment

1. Update `jwt.secret` in application.properties
2. Switch to SQL Server database
3. Build Angular with `--configuration production`
4. Set `spring.jpa.hibernate.ddl-auto=validate` or `none`
5. Enable HTTPS
6. Consider using httpOnly cookies instead of localStorage for JWT

---

**Happy Coding! 🎉**
