# Authentication Redirect Configuration

## Overview
This SSO Proxy now redirects unauthenticated users to the `index.html` page instead of returning a 401 error for gateway routes.

## How It Works

### Custom Authentication Entry Point
A custom `AuthenticationEntryPoint` has been configured in `SecurityConfig.java` that:

1. **For Authentication API Requests** (`/api/auth/**`):
   - Returns a `401 Unauthorized` response with JSON
   - Response format: `{"message":"Authentication required","status":401}`
   - This allows the JavaScript frontend to handle auth failures programmatically

2. **For Gateway Routes and Other Requests**:
   - Redirects the user to `/index.html`
   - User sees the login page with helpful test buttons
   - User can authenticate and retry their request

## Configuration Details

### SecurityConfig.java Changes
```java
.exceptionHandling(exception -> exception
    .authenticationEntryPoint(customAuthenticationEntryPoint())
)

@Bean
public AuthenticationEntryPoint customAuthenticationEntryPoint() {
    return (request, response, authException) -> {
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/auth/")) {
            // Return 401 JSON for auth endpoints
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Authentication required\",\"status\":401}");
        } else {
            // Redirect to index.html for gateway routes
            response.sendRedirect("/index.html");
        }
    };
}
```

## User Experience

### Before This Change
1. User tries to access gateway route without authentication
2. Receives a raw `401 Unauthorized` error
3. No clear path to authenticate

### After This Change
1. User tries to access gateway route without authentication (e.g., `/api/users/1`)
2. Browser redirects to `/index.html`
3. User sees the login page with:
   - Current authentication status
   - "Login as 'user'" button
   - Gateway test buttons to retry their request
4. User clicks login, authenticates, and can now access gateway routes

## Example Flow

```
Browser: GET /api/users/1 (no session)
   ↓
Gateway: Checks authentication → FAILS
   ↓
SecurityConfig: customAuthenticationEntryPoint()
   ↓
Browser: HTTP 302 Redirect → /index.html
   ↓
User sees login page
   ↓
User clicks "Login as 'user'"
   ↓
Browser: Session created (JSESSIONID cookie)
   ↓
User clicks "Test User Service" button
   ↓
Browser: GET /api/users/1 (with session cookie)
   ↓
Gateway: Authentication succeeds → Forwards to backend
```

## Testing

### Test Unauthenticated Access
1. Open browser in incognito/private mode
2. Navigate directly to `http://localhost:8080/api/users/1`
3. Should redirect to `http://localhost:8080/index.html`
4. Status shows "❌ Not logged in"

### Test Authenticated Access
1. Click "Login as 'user'" button
2. Status updates to "✅ Logged in as: user"
3. Click "Test User Service" button
4. Gateway forwards request to backend (may show connection error if backend not running)

### Test Auth API Endpoints
1. In incognito mode, try: `http://localhost:8080/api/auth/user`
2. Should return JSON: `{"message":"Authentication required","status":401}`
3. No redirect occurs (allows JavaScript to handle the error)

## Notes

- This behavior is specific to **browser-based** requests
- API clients (Postman, curl, etc.) should still handle 401 responses appropriately
- The `/api/auth/**` endpoints maintain their JSON error responses for programmatic access
- Session cookies (`JSESSIONID`) are automatically included in subsequent requests
