package com.cargaglobal.mobilebackend.application.port;

import com.cargaglobal.mobilebackend.domain.model.User;

public interface TokenService {
    String create(User user);
}
