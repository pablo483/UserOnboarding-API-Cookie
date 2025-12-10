package sudarshangc.com.UserUnboardingApi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sudarshangc.com.UserUnboardingApi.DTO.LoginRequest;
import sudarshangc.com.UserUnboardingApi.DTO.LoginResponse;
import sudarshangc.com.UserUnboardingApi.DTO.RegistrationRequest;
import sudarshangc.com.UserUnboardingApi.Entity.RefreshToken;
import sudarshangc.com.UserUnboardingApi.Entity.User;
import sudarshangc.com.UserUnboardingApi.Exception.InvalidTokenException;
import sudarshangc.com.UserUnboardingApi.Service.*;
import sudarshangc.com.UserUnboardingApi.Utils.JwtUtils;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/public")
public class PublicController {//this class means what anybody is allowed to do

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImp userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private JwtCookieService jwtCookieService;


    @PostMapping("/register")
    public ResponseEntity<?> signUpUser(@RequestBody RegistrationRequest request){

        verificationTokenService.register(request);


        return new ResponseEntity<>("registration succesfully created ,please check you email for activation first",HttpStatus.OK);
    }

    @GetMapping("/verify-account")
    @Transactional
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token){
        try{
            verificationTokenService.verifyAccount(token);
            return ResponseEntity.ok("Account Succesfully Verified,You Can Now Log-in");
        }catch (InvalidTokenException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_GATEWAY);
        }
    }

    //create and user
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletResponse response) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserName());
        String accessToken = jwtUtils.generateToken(userDetails.getUsername());

        User user = userService.findByUserName(request.getUserName());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userDetails.getUsername());

        //add both token into cookies
        jwtCookieService.setCookies(response,accessToken,refreshToken.getToken());

        LoginResponse loginResponse=new LoginResponse(user.getUserName(), user.getEmail(), "Login Successful,Token are saved in Cookie");

        return ResponseEntity.ok(loginResponse);

    }




//lOG OUT USER(By deleteing cookies)
    @DeleteMapping("/log-out")
    public ResponseEntity<?> LogOut(HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);

        //delete refresh token from db
        userService.logOutUser(user);
        //delete cookies from browser
        jwtCookieService.clearTokenCookies(response);
        return new ResponseEntity<>("logOut SuccessFull", HttpStatus.OK);
    }

}
