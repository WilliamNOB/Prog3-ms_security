package com.progIII.ms_security.Controllers;

import com.progIII.ms_security.Models.Permission;
import com.progIII.ms_security.Models.Session;
import com.progIII.ms_security.Models.User;
import com.progIII.ms_security.Repositories.SessionRepository;
import com.progIII.ms_security.Repositories.UserRepository;
import com.progIII.ms_security.Services.EncryptionService;
import com.progIII.ms_security.Services.JSONResponsesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.progIII.ms_security.Services.JwtService;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private JSONResponsesService jsonResponsesService;
    /*
    @Autowired
    private ValidatorsService theValidatorsService;
    */
    @Value("${ms-notifications.base-url}")
    private String baseUrlNotifications;


    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody User theUser, final HttpServletResponse response) throws IOException {
        try {
            User actualUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
            if (actualUser != null && actualUser.getPassword().equals(this.theEncryptionService.convertSHA256(theUser.getPassword()))) {

                // 2fa
                Random random = new Random();
                int token2FA = random.nextInt(9000) + 1000;
                Session newSession = new Session(token2FA, actualUser);
                this.theSessionRepository.save(newSession);
                System.out.println(newSession);

                // mandar el token2FA con el correo del usuario
                RestTemplate restTemplate = new RestTemplate();
                String urlPost = baseUrlNotifications + "send_2FAC";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String requestBody = "{\"email\":\"" + actualUser.getEmail() + "\",\"token2FA\":\"" + token2FA + "\"}";
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> res = restTemplate.postForEntity(urlPost, requestEntity, String.class);
                System.out.println(res.getBody());

                this.jsonResponsesService.setMessage("Ingreso satisfactorio, se envió el código al correo");
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(this.jsonResponsesService.getFinalJSON());
            } else if (actualUser != null) {
                this.jsonResponsesService.setMessage("Contraseña incorrecta");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(this.jsonResponsesService.getFinalJSON());
            } else {
                this.jsonResponsesService.setMessage("Acceso denegado, correo inexistente");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(this.jsonResponsesService.getFinalJSON());
            }
        } catch (Exception e) {
            this.jsonResponsesService.setData(null);
            this.jsonResponsesService.setError(e.toString());
            this.jsonResponsesService.setMessage("Error al buscar usuarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(this.jsonResponsesService.getFinalJSON());
        }
    }

    @PostMapping("2FA-login/{userId}")
    public ResponseEntity<?> factorAuthetication(@RequestBody Session theSession, @PathVariable String userId) {
        try {
            int secondFactor_token = theSession.getToken2FA();
            User theUser = theUserRepository.getUserById(userId);
            System.out.println("2FA"+secondFactor_token+"---User "+ theUser );
            Session thePrincipalSession = theSessionRepository.getSessionbyUserId(userId, secondFactor_token);

            if (thePrincipalSession != null) {
                String token = this.theJwtService.generateToken(theUser);
                thePrincipalSession.setToken(token);
                this.theSessionRepository.save(thePrincipalSession);
                this.jsonResponsesService.setData(token);
                this.jsonResponsesService.setMessage("Se ha ingresado exitosamente, el token es:");
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(this.jsonResponsesService.getFinalJSON());
            } else if (theUser != null) {
                this.jsonResponsesService.setMessage("Código de autenticación incorrecto.");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(this.jsonResponsesService.getFinalJSON());
            } else {
                this.jsonResponsesService.setMessage("Correo inexistente.");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(this.jsonResponsesService.getFinalJSON());
            }
        } catch (Exception e) {
            this.jsonResponsesService.setData(null);
            this.jsonResponsesService.setError(e.toString());
            this.jsonResponsesService.setMessage("Error al buscar usuarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(this.jsonResponsesService.getFinalJSON());
        }
    }
    @PatchMapping("password-change")
    public ResponseEntity<?> resetPassword(@RequestBody User user) {
        try {
            User current = theUserRepository.getUserByEmail(user.getEmail());
            if (current != null) {
                String genPass = generateRandomPassword(10);
                current.setPassword(theEncryptionService.convertSHA256(genPass));
                theUserRepository.save(current);
                RestTemplate restTemplate = new RestTemplate();
                String urlPost = baseUrlNotifications + "send_reset-password_code";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String requestBody = "{\"email\":\"" + user.getEmail() + "\",\"new_password\":\"" + genPass + "\"}";
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> res = restTemplate.postForEntity(urlPost, requestEntity, String.class);
                System.out.println(res.getBody());
                this.jsonResponsesService.setData(current);
                this.jsonResponsesService.setMessage("Contraseña cambiada con exito, por favor revisa tu correo");
                return ResponseEntity.status(HttpStatus.OK).body(this.jsonResponsesService.getFinalJSON());
            } else {
                this.jsonResponsesService.setMessage("El usuario no se encuentra registrado en la base de datos");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(this.jsonResponsesService.getFinalJSON());
            }
        } catch (Exception e) {
            this.jsonResponsesService.setData(null);
            this.jsonResponsesService.setError(e.toString());
            this.jsonResponsesService.setMessage("Error al generar una nueva contraseña");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(this.jsonResponsesService.getFinalJSON());
        }
    }
    public static String generateRandomPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }
    /*
    @PostMapping("permissions-validation")
    public boolean permissionsValidation(final HttpServletRequest request,
                                         @RequestBody Permission thePermission) {
        boolean success=this.theValidatorsService.validationRolePermission(request,thePermission.getUrl(),thePermission.getMethod());
        return success;
    }
    */
}
