package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.request.LoginRequest;
import VynPay.Vynpay.dto.request.RegisterRequest;
import VynPay.Vynpay.dto.response.AuthResponse;
import VynPay.Vynpay.dto.response.RegisterResponse;
import VynPay.Vynpay.enun.Role;
import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.repository.CompanyRepository;
import VynPay.Vynpay.repository.UsuarioRepository;
import VynPay.Vynpay.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Verificar se email já existe
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Criar empresa
        Company company = new Company();
        company.setNomeEmpresa(request.getCompany().getNomeEmpresa());
        company.setCnpj(request.getCompany().getCnpj());
        company.setEndereco(request.getCompany().getEndereco());
        company.setCidade(request.getCompany().getCidade());
        company.setEstado(request.getCompany().getEstado());
        company.setTelefoneComercial(request.getCompany().getTelefoneComercial());
        company.setSegmentoAtuacao(request.getCompany().getSegmentoAtuacao());
        company.setSegmentoCode(request.getCompany().getSegmentoCode());
        company.setEdificacaoId(request.getCompany().getEdificacaoId());
        companyRepository.save(company);

        // Criar usuário admin
        usuario user = new usuario();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setCompany(company);
        usuarioRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new RegisterResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Transactional
    public usuario adicionarUsuario(Company company, String login, String password) {
        // Verificar se o login já existe
        if (usuarioRepository.findByUsername(login).isPresent()) {
            throw new RuntimeException("Login já existe");
        }

        usuario user = new usuario();
        user.setUsername(login);
        user.setEmail(login); // ✅ AGORA USA O PRÓPRIO LOGIN COMO EMAIL
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setCompany(company);

        return usuarioRepository.save(user);
    }

    // Lista Usuario por empresa
    public List<usuario> listarUsuarioPorEmpresa(Long companyId){
       return usuarioRepository.findByCompanyId(companyId);
    }

    // Buscar usuario por ID (verificando se pertence a empresa)
    public usuario buscarUsuarioPorId(Long userId, long companyId) {
      return usuarioRepository.findByIdAndCompanyId(userId, companyId)
              .orElseThrow(() -> new RuntimeException("Usuario nao encontrado nesta empresa"));
    }

    // Buscar Usuario por Login (username)
    public usuario buscarUsuarioPorLogin(String login, Long companyId) {
        return usuarioRepository.findByUsernameAndCompanyId(login, companyId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encotrado neste empresa"));
    }

    public usuario atualizarUsuario(Long userId, Long companyId, String novoLogin, String novaSenha, String novaRole) {
       usuario user = buscarUsuarioPorId(userId, companyId);

       if (novoLogin != null && !novoLogin.isEmpty()) {
           usuario existingUser = usuarioRepository.findByUsername(novoLogin).orElse(null);
           if (existingUser != null && !existingUser.getId().equals(userId)) {
               throw new RuntimeException("Login ja esta em uso por outro usuario");
           }
           user.setUsername(novoLogin);
           user.setEmail(novoLogin + "@" + user.getCompany().getNomeEmpresa().replace(" ", " ").toLowerCase() + ".com");
       }

        if (novaSenha != null && !novaSenha.isEmpty()) {
            user.setPassword(passwordEncoder.encode(novaSenha));
        }

        if (novaRole != null && !novaRole.isEmpty()){
            try {
                user.setRole(Role.valueOf(novaRole.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Role invalida. Use USER  ou ADMIN");
            }
        }

        return usuarioRepository.save(user);
    }

    @Transactional
    public void deletarUsuario(Long userId, Long companyId) {
        usuario user = buscarUsuarioPorId(userId, companyId);

        // Impedir que o admin seja deletado
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Não é possível deletar o administrador da empresa");
        }

        usuarioRepository.delete(user);
    }
}