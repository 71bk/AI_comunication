package tw.bk.ai.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tw.bk.ai.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 用戶主體
 */
@Getter
@AllArgsConstructor
public class JwtUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String displayName;
    private final User.UserStatus status;

    public static JwtUserPrincipal from(User user) {
        return new JwtUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getStatus());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != User.UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == User.UserStatus.ACTIVE;
    }
}
