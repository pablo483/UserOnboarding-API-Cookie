package sudarshangc.com.UserUnboardingApi.Filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sudarshangc.com.UserUnboardingApi.Service.UserDetailsServiceImp;
import sudarshangc.com.UserUnboardingApi.Utils.JwtUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImp userDetailsService;

    //extract access token(jwt) from cookies
    public String extractJwtFromCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies==null){
            return null;
        }
        for (Cookie cookie:cookies){
            if (cookie.getName().equals("accessToken")){
                return cookie.getValue();
            }
        }
        return null;
    }
    //this is for the postman

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String jwt=null;
        String username=null;

        if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer") ) {
            jwt = authorizationHeader.substring(7);


        }
        if (jwt==null){
             jwt = extractJwtFromCookie(request);
        }
        if (jwt!=null){
             username = jwtUtils.extractUserName(jwt);
        }

        if (username!=null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtils.validateToken(jwt)){
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request,response);

    }
}
