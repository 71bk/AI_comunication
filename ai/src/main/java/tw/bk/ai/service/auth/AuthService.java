package tw.bk.ai.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.bk.ai.dto.auth.AuthMeResp;
import tw.bk.ai.dto.auth.LoginReq;
import tw.bk.ai.dto.auth.RegisterReq;
import tw.bk.ai.entity.User;
import tw.bk.ai.exception.AuthException;
import tw.bk.ai.exception.NotFoundException;
import tw.bk.ai.repository.UserRepository;
import tw.bk.ai.security.JwtProvider;

/**
 * 認證服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 使用者註冊
     */
    @Transactional
    public AuthMeResp register(RegisterReq req) {
        // 檢查 Email 是否已存在
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AuthException.userExists(req.getEmail());
        }

        // 建立使用者
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .displayName(req.getDisplayName())
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        // 生成 Token
        String token = jwtProvider.generateToken(user.getId(), user.getEmail());

        return AuthMeResp.from(user, token);
    }

    /**
     * 使用者登入
     */
    @Transactional(readOnly = true)
    public AuthMeResp login(LoginReq req) {
        // 驗證密碼
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        // 取得使用者
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(AuthException::invalidCredentials);

        log.info("User logged in: {}", user.getEmail());

        // 生成 Token
        String token = jwtProvider.generateToken(user.getId(), user.getEmail());

        return AuthMeResp.from(user, token);
    }

    /**
     * 取得當前使用者資訊
     */
    @Transactional(readOnly = true)
    public AuthMeResp getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        return AuthMeResp.from(user);
    }
}
