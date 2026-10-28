package com.cargaglobal.mobilebackend.domain.model;

import java.util.List;

/** Similarity is probabilistic, separate from liveness and never an authorization decision. */
public record FaceComparison(String status, int compared, int skipped, List<Match> matches) {
    public FaceComparison { matches = List.copyOf(matches); }
    public record Match(Long userId, String name, double similarity) {}
}
