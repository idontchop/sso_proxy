# SSO Proxy - API Gateway Configuration

## Overview

This SSO Proxy acts as both an **authentication server** and an **API gateway** to your backend microservices. It:

1. **Authenticates users** using session-based authentication (JSESSIONID cookies)
2. **Routes requests** to backend microservices
3. **Forwards authentication context** to downstream services
4. **Serves your Angular frontend** from the root URL

---

## Architecture

```
[Angular Frontend] → [SSO Proxy] → [Backend Microservices]
                         ↓
                   [Session Store]
                   [User Database]
```

---

## Gateway Configuration

You can configure gateway routes in **two ways**:

### Option 1: Java Configuration (GatewayConfig.java)

Gateway routes are configured in `src/main/java/com/idontchop/sso_proxy/config/GatewayConfig.java`

**Example: Route `/api/users/**` to a User Service on port 8082**

```java
@Bean
public RouterFunction<ServerResponse> gatewayRoutes() {
    return GatewayRouterFunctions.route("user_service")
            .route(RequestPredicates.path("/api/users/**"), 
                   HandlerFunctions.http("http://localhost:8082"))
            .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
            .filter(addRequestHeader("X-Forwarded-By", "SSO-Proxy"))
            .build();
}
```

### Multiple Routes

```java
@Bean
public RouterFunction<ServerResponse> gatewayRoutes() {
    return GatewayRouterFunctions.route("user_service")
            .route(RequestPredicates.path("/api/users/**"), 
                   HandlerFunctions.http("http://localhost:8082"))
            .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
            .build()
            .and(GatewayRouterFunctions.route("product_service")
                .route(RequestPredicates.path("/api/products/**"), 
                       HandlerFunctions.http("http://localhost:8083"))
                .filter(addRequestHeader("X-Gateway", "SSO-Proxy"))
                                .build());
}
```

### Option 2: Properties/YAML Configuration (Recommended for Simple Routes)

#### Using `application.properties`:

```properties
# Route to User Service
spring.cloud.gateway.mvc.routes[0].id=user-service
spring.cloud.gateway.mvc.routes[0].uri=http://localhost:8082
spring.cloud.gateway.mvc.routes[0].predicates[0]=Path=/api/users/**
spring.cloud.gateway.mvc.routes[0].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy

# Route to Product Service
spring.cloud.gateway.mvc.routes[1].id=product-service
spring.cloud.gateway.mvc.routes[1].uri=http://localhost:8083
spring.cloud.gateway.mvc.routes[1].predicates[0]=Path=/api/products/**
spring.cloud.gateway.mvc.routes[1].filters[0]=AddRequestHeader=X-Gateway, SSO-Proxy

# Global HTTP Client Settings
spring.cloud.gateway.mvc.httpclient.connect-timeout=3000
spring.cloud.gateway.mvc.httpclient.response-timeout=5000
```

#### Using `application.yml` (Cleaner for Multiple Routes):

```yaml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          # User Service
          - id: user-service
            uri: http://localhost:8082
            predicates:
              - Path=/api/users/**
            filters:
              - AddRequestHeader=X-Gateway, SSO-Proxy
              - AddRequestHeader=X-Forwarded-By, SSO-Proxy
          
          # Product Service
          - id: product-service
            uri: http://localhost:8083
            predicates:
              - Path=/api/products/**
            filters:
              - AddRequestHeader=X-Gateway, SSO-Proxy
          
          # Order Service with Path Rewriting
          - id: order-service
            uri: http://localhost:8084
            predicates:
              - Path=/api/orders/**
            filters:
              - RewritePath=/api/orders/(?<segment>.*), /order-api/$\{segment}
              - AddRequestHeader=X-Gateway, SSO-Proxy
        
        # Global Settings
        httpclient:
          connect-timeout: 3000
          response-timeout: 5000
```

**See `application.yml.example` for a complete configuration example.**

### Which Approach to Use?

- **Use Properties/YAML** when:
  - You have simple route configurations
  - You want to change routes without recompiling
  - Different environments need different routes
  - You prefer declarative configuration

- **Use Java Configuration** when:
  - You need complex routing logic
  - You want to add custom filters
  - You need to programmatically determine routes
  - You want type safety and IDE autocomplete

**Note:** You can use both approaches together - Java config and properties will be merged.

### Common Route Patterns

#### 1. Basic Route
```yaml
- id: my-service
  uri: http://localhost:8080
  predicates:
    - Path=/api/myservice/**
```

#### 2. Route with Method Matching
```yaml
- id: read-only-service
  uri: http://localhost:8080
  predicates:
    - Path=/api/data/**
    - Method=GET
```

#### 3. Route with Path Rewriting
```yaml
- id: legacy-api
  uri: http://localhost:8080
  predicates:
    - Path=/api/v2/**
  filters:
    - RewritePath=/api/v2/(?<segment>.*), /api/v1/$\{segment}
```

#### 4. Route with Multiple Filters
```yaml
- id: secure-service
  uri: http://localhost:8080
  predicates:
    - Path=/api/secure/**
  filters:
    - AddRequestHeader=X-Gateway, SSO-Proxy
    - AddRequestHeader=X-Secure, true
    - StripPrefix=1
```

#### 5. Route to External Service
```yaml
- id: external-api
  uri: https://api.external.com
  predicates:
    - Path=/api/external/**
  filters:
    - RewritePath=/api/external/(?<segment>.*), /$\{segment}
    - AddRequestHeader=Authorization, Bearer YOUR_TOKEN
```

---

## Available Predicates

- `Path=/api/users/**` - Match path patterns
- `Method=GET,POST` - Match HTTP methods
- `Header=X-Request-Id, \d+` - Match headers
- `Query=token, \d+` - Match query parameters
- `Host=**.example.com` - Match host patterns
- `Cookie=chocolate, ch.p` - Match cookies

## Available Filters

- `AddRequestHeader=X-Custom, Value` - Add request header
- `AddResponseHeader=X-Custom, Value` - Add response header
- `RewritePath=/old/(.*), /new/$\{segment}` - Rewrite path
- `StripPrefix=1` - Remove path segments
- `SetStatus=401` - Set response status
- `RedirectTo=302, https://example.com` - Redirect

See [Spring Cloud Gateway MVC Documentation](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-mvc.html) for all available predicates and filters.

---

## Request Flow
}
```

### Adding User Info to Forwarded Requests

You can forward the authenticated user's information to backend services:

```java
.filter((request, next) -> {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
        ServerRequest modifiedRequest = ServerRequest.from(request)
                .header("X-User-Name", auth.getName())
                .header("X-User-Roles", auth.getAuthorities().toString())
                .build();
        return next.handle(modifiedRequest);
    }
    return next.handle(request);
})
```

---

## Request Flow

### 1. User Logs In
```
POST /api/auth/login
{
  "username": "user",
  "password": "password"
}

Response:
{
  "sessionId": "ABC123...",
  "username": "user",
  "email": "user@example.com",
  "role": "ROLE_USER"
}

Set-Cookie: JSESSIONID=ABC123...
```

### 2. User Accesses Backend Service
```
GET /api/users/profile
Cookie: JSESSIONID=ABC123...

SSO Proxy:
1. Validates session (checks if user is authenticated)
2. Routes to http://localhost:8082/api/users/profile
3. Adds headers:
   - X-Gateway: SSO-Proxy
   - X-User-Name: user
   - X-User-Roles: ROLE_USER
```

### 3. Backend Service Receives Request
```
GET /api/users/profile
Headers:
  X-Gateway: SSO-Proxy
  X-User-Name: user
  X-User-Roles: ROLE_USER

Backend can trust these headers since they come from the gateway
```

---

## Security Considerations

### 1. Protected Routes
All routes through the gateway require authentication by default (configured in `SecurityConfig.java`).

To allow public access to specific paths:
```java
.requestMatchers("/api/public/**").permitAll()
```

### 2. Trust Between Gateway and Backend Services

**Important:** Backend services should:
- Only accept requests from the gateway (use network segmentation or IP whitelisting)
- Trust the `X-User-*` headers since they come from the authenticated gateway
- NOT expose their ports directly to the internet

### 3. CORS Configuration

CORS is configured to allow your Angular app:
```java
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, 
             allowCredentials = "true")
```

Update these origins for production!

---

## Example Microservices Setup

### User Service (Port 8082)
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("X-User-Name") String username) {
        // Username is provided by the gateway, already authenticated
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }
}
```

### Product Service (Port 8083)
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public List<Product> getAllProducts(@RequestHeader(value = "X-User-Roles", required = false) String roles) {
        // Check roles if needed
        if (roles != null && roles.contains("ROLE_ADMIN")) {
            return productService.getAllProductsIncludingInternal();
        }
        return productService.getPublicProducts();
    }
}
```

---

## Testing the Gateway

### 1. Test Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' \
  -c cookies.txt

# Check session
curl http://localhost:8080/api/auth/user \
  -b cookies.txt
```

### 2. Test Gateway Route (after configuring a route)
```bash
# Access backend service through gateway
curl http://localhost:8080/api/users/profile \
  -b cookies.txt
```

### 3. Test from Angular
```typescript
// Login
this.http.post('/api/auth/login', { username: 'user', password: 'password' }, 
  { withCredentials: true }).subscribe();

// Access backend through gateway
this.http.get('/api/users/profile', { withCredentials: true }).subscribe();
```

**Important:** Always use `withCredentials: true` to send session cookies!

---

## Production Deployment

### 1. Update Backend URLs
Change from localhost to actual service URLs:
```java
HandlerFunctions.http("http://user-service:8082")  // Docker
HandlerFunctions.http("https://user-service.example.com")  // Production
```

### 2. Update CORS Origins
```java
@CrossOrigin(origins = "https://your-production-domain.com", 
             allowCredentials = "true")
```

### 3. Use Secure Sessions
In `application.properties`:
```properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

### 4. Configure Load Balancing
For multiple gateway instances, use:
- Redis for session storage (Spring Session Data Redis)
- Sticky sessions in your load balancer

---

## Troubleshooting

### Sessions Not Working
- Check `withCredentials: true` in Angular requests
- Verify CORS `allowCredentials = "true"`
- Check cookie domain/path settings

### Routes Not Forwarding
- Verify the path pattern matches (e.g., `/api/users/**`)
- Check backend service is running
- Enable debug logging: `logging.level.org.springframework.cloud.gateway=DEBUG`

### Authentication Issues
- Check `SecurityConfig.java` permits the route
- Verify user is logged in: `GET /api/auth/user`
- Check session timeout settings

---

## Next Steps

1. **Configure your backend service routes** in `GatewayConfig.java`
2. **Update backend services** to trust gateway headers
3. **Test the complete flow** from Angular → Gateway → Backend
4. **Set up production configuration** (HTTPS, secure cookies, etc.)

For more details, see:
- `ANGULAR_INTEGRATION.md` - Angular setup guide
- `README_ANGULAR_SETUP.md` - Detailed Angular configuration
