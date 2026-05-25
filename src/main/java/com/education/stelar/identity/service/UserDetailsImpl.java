package com.education.stelar.identity.service;

import com.education.stelar.identity.entity.Permission;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.entity.UserTenant;
import com.education.stelar.kernel.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserDetailsImpl implements UserDetails, UserPrincipal {

    private final UUID id;
    private final UUID tenantId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String profilePictureUrl;
    private final boolean emailVerified;
    private final boolean activeAccount;
    private final String roleName;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(
            UUID id, UUID tenantId, String firstName, String lastName,
            String email, String password, String profilePictureUrl,
            boolean emailVerified, boolean activeAccount, String roleName,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.tenantId = tenantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profilePictureUrl = profilePictureUrl;
        this.emailVerified = emailVerified;
        this.activeAccount = activeAccount;
        this.roleName = roleName;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user, UserTenant userTenant) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (userTenant.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userTenant.getRole().getName()));

            userTenant.getRole().getPermissions().stream()
                    .filter(Permission::isActive)
                    .map(p -> new SimpleGrantedAuthority(p.getCode()))
                    .forEach(authorities::add);
        }

        return new UserDetailsImpl(
                user.getId(),
                userTenant.getTenantId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getProfilePictureUrl(),
                user.isEmailVerified(),
                userTenant.isActive(),
                userTenant.getRoleName(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activeAccount && emailVerified;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
