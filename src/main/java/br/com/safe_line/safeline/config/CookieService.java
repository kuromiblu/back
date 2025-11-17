package br.com.safe_line.safeline.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void addSecureCookie(HttpServletResponse response, String cookieName, String value, int maxAge) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true); // Impede acesso via JS
        cookie.setSecure(false); // Apenas HTTPS
        cookie.setPath("/"); // Disponível em toda a aplicação
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax"); // Previne CSRF
        response.addCookie(cookie);
    }

    public void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}