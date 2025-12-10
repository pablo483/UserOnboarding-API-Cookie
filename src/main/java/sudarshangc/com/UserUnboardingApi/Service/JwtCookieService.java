package sudarshangc.com.UserUnboardingApi.Service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import sudarshangc.com.UserUnboardingApi.Entity.RefreshToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class JwtCookieService {

    //create cookies and store access token and refresh token inside cookie
    public void setCookies(HttpServletResponse response,String accessToken, String refreshToken){

        ResponseCookie accessCookie=ResponseCookie.from("accessToken",accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(900)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie=ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh-token")
                .maxAge(60*60*24*7)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE,accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshCookie.toString());
    }

    //clear cookies by setting maxAge to 0
    public void clearTokenCookies(HttpServletResponse response){

        ResponseCookie accessCookie=ResponseCookie.from("accessToken","")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie=ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE,accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshCookie.toString());
    }

    //extract refresh token from cookie
    public String getRefreshTokenFromCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies==null){
            return null;
        }
        for (Cookie cookie:cookies){
            if (cookie.getName().equals("refreshToken")){
                return cookie.getValue();
            }
        }
        return null;
    }
}
