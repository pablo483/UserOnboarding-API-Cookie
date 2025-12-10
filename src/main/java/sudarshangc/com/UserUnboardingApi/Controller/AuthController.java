package sudarshangc.com.UserUnboardingApi.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sudarshangc.com.UserUnboardingApi.Entity.RefreshToken;
import sudarshangc.com.UserUnboardingApi.Entity.User;
import sudarshangc.com.UserUnboardingApi.Service.JwtCookieService;
import sudarshangc.com.UserUnboardingApi.Service.RefreshTokenService;
import sudarshangc.com.UserUnboardingApi.Service.UserDetailsServiceImp;
import sudarshangc.com.UserUnboardingApi.Utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {//this is also authorized person can do

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImp userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtCookieService jwtCookieService;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response){

        String refreshTokenFromCookie = jwtCookieService.getRefreshTokenFromCookie(request);
        return refreshTokenService.findByToken(refreshTokenFromCookie)
                .map(refreshTokenService::verifyExpiration)
                .map(token ->{
                    User user = token.getUser();

                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

                    //generate new access token short-lived
                    String newAccessToken = jwtUtils.generateToken(user.getUserName());

                    RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken( user, userDetails.getUsername());

                    //Invalidate the old token by deleting it from the database
                    refreshTokenService.delete(token);

                    //add both new created token into cookies
                    jwtCookieService.setCookies(response,newAccessToken,newRefreshTokenEntity.getToken());
                    //  Return the new pair to the client
                    return  ResponseEntity.ok("new token and refresh token created in cookie");
                })
                .orElseThrow(()->new RuntimeException("Refresh token is not in database,please Re-login"));

    }

}


