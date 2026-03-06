package com.emis.hrservice.security;





import java.util.Set;

import com.emis.hrservice.enums.ActorType;
import com.emis.hrservice.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActorContext {

  private final ActorType type;

  private final String username;
  private final String schoolCode;
  private final Set<UserRole> userRoles;
  private final String email;

  private final String serviceName;
  private final Set<String> serviceAuthorities;

  public boolean isUser() {
    return type == ActorType.USER;
  }

  public boolean isService() {
    return type == ActorType.SERVICE;
  }
}