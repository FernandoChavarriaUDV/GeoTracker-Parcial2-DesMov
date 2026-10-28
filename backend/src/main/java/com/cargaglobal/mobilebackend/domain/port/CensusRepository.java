package com.cargaglobal.mobilebackend.domain.port;

import com.cargaglobal.mobilebackend.domain.model.Census;
import com.cargaglobal.mobilebackend.domain.model.CensusOrigin;
import java.util.List;

public interface CensusRepository {
    Census save(Census census);
    List<Census> findAfter(long afterId, int limit, CensusOrigin origin);
}
