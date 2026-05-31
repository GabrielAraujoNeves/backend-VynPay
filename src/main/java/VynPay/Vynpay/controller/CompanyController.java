package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.AddUserRequest;
import VynPay.Vynpay.dto.request.UpdateUserRequest;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.repository.CompanyRepository;
import VynPay.Vynpay.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/company")
@CrossOrigin(origins = "*")
public class CompanyController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(
            @AuthenticationPrincipal usuario admin,
            @RequestBody AddUserRequest request
    ) {
        try {
            var company = companyRepository.findById(admin.getCompany().getId())
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            usuario novoUsuario = authService.adicionarUsuario(
                    company,
                    request.getLogin(),
                    request.getPassword()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário adicionado com sucesso");
            response.put("id", novoUsuario.getId());
            response.put("username", novoUsuario.getUsername());
            //response.put("password", novoUsuario.getPassword());
            //response.put("email", novoUsuario.getEmail());
            response.put("role", novoUsuario.getRole().name());
            response.put("companyId", novoUsuario.getCompany().getId());
            //response.put("companyName", novoUsuario.getCompany().getNomeEmpresa());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@AuthenticationPrincipal usuario admin) {
        try {
            List<usuario> users = authService.listarUsuarioPorEmpresa(admin.getCompany().getId());

            // Criar resposta com senha criptografada e informações da empresa
            List<Map<String, Object>> usersResponse = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("password", user.getPassword()); // ✅ Mostra a senha criptografada
                userMap.put("email", user.getEmail());
                userMap.put("role", user.getRole().name());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("updatedAt", user.getUpdatedAt());

                // ✅ Mostra informações da empresa
                Map<String, Object> companyInfo = new HashMap<>();
                companyInfo.put("id", user.getCompany().getId());
                companyInfo.put("name", user.getCompany().getNomeEmpresa());
                companyInfo.put("cnpj", user.getCompany().getCnpj());
                userMap.put("company", companyInfo);

                return userMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", users.size());
            response.put("users", usersResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long userId
    ) {
        try {
            usuario user = authService.buscarUsuarioPorId(userId, admin.getCompany().getId());

            // Criar resposta com senha criptografada
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("username", user.getUsername());
            userResponse.put("password", user.getPassword()); // ✅ Mostra a senha criptografada
            userResponse.put("email", user.getEmail());
            userResponse.put("role", user.getRole().name());
            userResponse.put("createdAt", user.getCreatedAt());
            userResponse.put("updatedAt", user.getUpdatedAt());

            // ✅ Mostra informações da empresa
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", user.getCompany().getId());
            companyInfo.put("name", user.getCompany().getNomeEmpresa());
            companyInfo.put("cnpj", user.getCompany().getCnpj());
            userResponse.put("company", companyInfo);

            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> getUserByLogin(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String login
    ) {
        try {
            usuario user = authService.buscarUsuarioPorLogin(login, admin.getCompany().getId());

            // Criar resposta com senha criptografada
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("username", user.getUsername());
            userResponse.put("password", user.getPassword()); // ✅ Mostra a senha criptografada
            userResponse.put("email", user.getEmail());
            userResponse.put("role", user.getRole().name());
            userResponse.put("createdAt", user.getCreatedAt());
            userResponse.put("updatedAt", user.getUpdatedAt());

            // ✅ Mostra informações da empresa
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", user.getCompany().getId());
            companyInfo.put("name", user.getCompany().getNomeEmpresa());
            companyInfo.put("cnpj", user.getCompany().getCnpj());
            userResponse.put("company", companyInfo);

            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ) {
        try {
            usuario userAtualizado = authService.atualizarUsuario(
                    userId,
                    admin.getCompany().getId(),
                    request.getLogin(),
                    request.getPassword(),
                    request.getRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário atualizado com sucesso");
            response.put("id", userAtualizado.getId());
            response.put("username", userAtualizado.getUsername());
            response.put("password", userAtualizado.getPassword()); // ✅ Mostra a nova senha criptografada
            response.put("email", userAtualizado.getEmail());
            response.put("role", userAtualizado.getRole().name());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long userId
    ) {
        try {
            authService.deletarUsuario(userId, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuário deletado com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}