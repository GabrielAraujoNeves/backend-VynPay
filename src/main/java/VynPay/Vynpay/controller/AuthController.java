package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.LoginRequest;
import VynPay.Vynpay.dto.request.RegisterRequest;
import VynPay.Vynpay.dto.response.AuthResponse;
import VynPay.Vynpay.dto.response.RegisterResponse;
import VynPay.Vynpay.dto.response.TokenValidationResponse;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.repository.UsuarioRepository;
import VynPay.Vynpay.security.JwtUtil;
import VynPay.Vynpay.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);

            Map<String, String> result = new HashMap<>();
            result.put("message", "Empresa cadastrada com sucesso");
            result.put("email", response.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // 🔥 NOVO ENDPOINT: Validar token do usuário
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            // Extrair token do header Authorization
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token não fornecido ou formato inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);

            // Validar token
            if (!jwtUtil.validateToken(token)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token inválido ou expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extrair email do token
            String email = jwtUtil.extractEmail(token);

            // Buscar usuário no banco
            usuario user = usuarioRepository.findByEmail(email).orElse(null);

            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Usuário não encontrado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Token válido - retornar informações do usuário
            TokenValidationResponse response = new TokenValidationResponse(
                    true,
                    "Token válido",
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.getCompany() != null ? user.getCompany().getId() : null,
                    user.getCompany() != null ? user.getCompany().getNomeEmpresa() : null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Erro ao validar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🔥 NOVO ENDPOINT: Renovar token (refresh)
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Token não fornecido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String oldToken = authHeader.substring(7);

            // Validar token antigo
            if (!jwtUtil.validateToken(oldToken)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Token inválido ou expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extrair email e role
            String email = jwtUtil.extractEmail(oldToken);
            String role = jwtUtil.extractRole(oldToken);

            // Gerar novo token
            String newToken = jwtUtil.generateToken(email, role);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);
            response.put("message", "Token renovado com sucesso");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erro ao renovar token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}