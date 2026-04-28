package com.anomalydetection.web.permissions;

import com.anomalydetection.application.permissions.PermissionManager;
import com.anomalydetection.contracts.permissions.GrantDto;
import com.anomalydetection.contracts.permissions.PermissionGroupDefinition;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/permissions")
public class PermissionsController {

  private final PermissionManager permissionManager;

  public PermissionsController(PermissionManager permissionManager) {
    this.permissionManager = permissionManager;
  }

  @GetMapping("/definitions")
  public List<PermissionGroupDefinition> getDefinitions() {
    return permissionManager.getDefinitions();
  }

  @GetMapping("/grants")
  public List<GrantDto> getGrants() {
    return permissionManager.getGrants();
  }

  @PostMapping("/roles/{role}/grant")
  public ResponseEntity<Void> grantToRole(@PathVariable String role,
      @RequestBody Map<String, String> body) {
    String permission = body.get("permission");
    if (permission == null) return ResponseEntity.badRequest().build();
    permissionManager.grantToRole(permission, role, null);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/roles/{role}/grants/{permission}")
  public ResponseEntity<Void> revokeFromRole(@PathVariable String role,
      @PathVariable String permission) {
    permissionManager.revokeFromRole(permission, role);
    return ResponseEntity.noContent().build();
  }
}
