package com.codequest.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final JwtService jwt;
    private final boolean secure;
    public AuthController(AuthService auth, JwtService jwt, @Value("${app.secure-cookie}") boolean secure) { this.auth = auth; this.jwt = jwt; this.secure = secure; }
    public record RegisterRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=8,max=64) String password, @NotBlank @Size(min=2,max=30) String displayName) {}
    public record LoginRequest(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=64) String password) {}
    @GetMapping("/csrf") public Map<String, String> csrf(CsrfToken token) { return Map.of("headerName", token.getHeaderName(), "token", token.getToken()); }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public PlayerView register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        var user = auth.register(request.email(), request.password(), request.displayName()); setCookie(response, jwt.issue(user.id()), jwt.ttl()); return user;
    }
    @PostMapping("/login") public PlayerView login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var user = auth.login(request.email(), request.password()); setCookie(response, jwt.issue(user.id()), jwt.ttl()); return user;
    }
    @GetMapping("/me") public PlayerView me(Authentication authentication) { return auth.me((Long) authentication.getPrincipal()); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) { setCookie(response, "", 0); }
    private void setCookie(HttpServletResponse response, String value, long maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(JwtCookieFilter.COOKIE, value).httpOnly(true).secure(secure).sameSite("Lax").path("/").maxAge(maxAge).build().toString());
    }
}
