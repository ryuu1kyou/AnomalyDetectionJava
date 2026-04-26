package com.anomalydetection.web.identity;

import com.anomalydetection.application.identity.UserAppService;
import com.anomalydetection.contracts.identity.CreateUserDto;
import com.anomalydetection.contracts.identity.GetUsersInputDto;
import com.anomalydetection.contracts.identity.UpdateUserDto;
import com.anomalydetection.contracts.identity.UserDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/users")
public class UsersController {

  private final UserAppService appService;

  public UsersController(UserAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public PagedResultDto<UserDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount) {
    return appService.getList(new GetUsersInputDto(filter, skipCount, maxResultCount));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public UserDto create(@RequestBody CreateUserDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> update(@PathVariable UUID id, @RequestBody UpdateUserDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
