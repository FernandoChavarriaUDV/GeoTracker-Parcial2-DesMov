package com.cargaglobal.mobilebackend.location;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserIdOrderByIdDesc(Long userId);
}
