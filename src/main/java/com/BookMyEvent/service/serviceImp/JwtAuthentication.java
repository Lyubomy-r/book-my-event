package com.BookMyEvent.service.serviceImp;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthentication extends OncePerRequestFilter {
    @Value("${jwt.signing.key}")
    private String signingKey;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            var jwt = extractJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && validateToken(jwt)) {
                var username = getUsernameFromToken(jwt);
                var role = getRoleFromToken(jwt);

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(role));

                var authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }

    private String getRoleFromToken(String token) {
        var claims = Jwts.parser()
                .setSigningKey("signingKey")
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

        private String extractJwtFromRequest(HttpServletRequest request) {
            var bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        }

        private boolean validateToken(String token) {
            try {
                // Парсим токен и проверяем подпись
                var claims = Jwts.parser()
                        .setSigningKey(signingKey)
                        .parseClaimsJws(token)
                        .getBody();

                // Проверка срока действия токена
                if (claims.getExpiration().before(new Date())) {
                    return false;
                }

                // Дополнительно: можно проверить роли или другую информацию из токена
                var role = claims.get("role", String.class);
                if (role == null || (!role.equals("USER") && !role.equals("ADMIN"))) {
                    return false;
                }

                return true;
            } catch (SignatureException e) {
                // Подпись токена некорректна
                return false;
            } catch (Exception e) {
                // Любая другая ошибка при проверке токена
                return false;
            }
        }

        private String getUsernameFromToken(String token) {
            return Jwts.parser().setSigningKey("SecretKeyToGenJWTs").parseClaimsJws(token).getBody().getSubject();
        }
    }