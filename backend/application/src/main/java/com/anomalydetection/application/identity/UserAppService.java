package com.anomalydetection.application.identity;

import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.contracts.identity.GetUsersInputDto;
import com.anomalydetection.contracts.identity.IdentityPermissions;
import com.anomalydetection.contracts.identity.UpdateUserDto;
import com.anomalydetection.contracts.identity.UserDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.domain.identity.User;
import com.anomalydetection.domain.identity.UserRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAppService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ICurrentTenant currentTenant;

  public UserAppService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      ICurrentTenant currentTenant) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.currentTenant = currentTenant;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.USERS_VIEW + "')")
  public PagedResultDto<UserDto> getList(GetUsersInputDto input) {
    String filter = input != null && input.filter() != null ? input.filter().toLowerCase() : "";
    List<User> all = userRepository.findAll();

    var filtered = all.stream()
        .filter(u -> filter.isBlank()
            || u.getUserName().toLowerCase().contains(filter)
            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(filter)))
        .toList();

    int skip = input != null && input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input != null && input.maxResultCount() != null ? Math.max(1, input.maxResultCount()) : 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + IdentityPermissions.USERS_VIEW + "')")
  public Optional<UserDto> getById(UUID id) {
    return userRepository.findById(id).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.USERS_CREATE + "')")
  public UserDto create(CreateUserDto input) {
    var user = new User(UUID.randomUUID(), input.userName(), input.userName().toUpperCase());
    currentTenant.getTenantId().ifPresent(user::setTenantId);
    user.setEmail(input.email());
    user.setNormalizedEmail(input.email() != null ? input.email().toUpperCase() : null);
    if (input.password() != null && !input.password().isBlank()) {
      user.setPasswordHash(passwordEncoder.encode(input.password()));
    }
    user.setActive(input.isActive());
    return toDto(userRepository.save(user));
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.USERS_EDIT + "')")
  public Optional<UserDto> update(UUID id, UpdateUserDto input) {
    return userRepository.findById(id).map(user -> {
      user.setEmail(input.email());
      user.setNormalizedEmail(input.email() != null ? input.email().toUpperCase() : null);
      user.setActive(input.isActive());
      return toDto(userRepository.save(user));
    });
  }

  @PreAuthorize("hasAuthority('" + IdentityPermissions.USERS_DELETE + "')")
  public boolean delete(UUID id) {
    if (!userRepository.existsById(id)) return false;
    userRepository.deleteById(id);
    return true;
  }

  private UserDto toDto(User u) {
    return new UserDto(u.getId(), u.getTenantId(), u.getUserName(), u.getEmail(),
        u.isActive(), u.isEmailConfirmed());
  }
}
