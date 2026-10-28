package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.port.LivenessProvider;
import com.cargaglobal.mobilebackend.domain.model.FaceComparison;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Base64;

@Service
public class FaceComparisonUseCase {
    private final UserRepository users;
    private final LivenessProvider provider;
    private final double threshold;
    private static final int MAX_GALLERY = 20;
    public FaceComparisonUseCase(UserRepository users,LivenessProvider provider,
            @Value("${liveness.similarity-threshold:90}") double threshold) {
        if (!Double.isFinite(threshold) || threshold<0 || threshold>100) throw new IllegalArgumentException("Umbral inválido");
        this.users=users; this.provider=provider; this.threshold=threshold;
    }
    public FaceComparison execute(String reference) {
        if (reference==null || reference.isBlank()) return summary("NO_REFERENCE",0,0);
        var candidates=users.findFaceCandidates(MAX_GALLERY+1);
        if (candidates.isEmpty()) return summary("NO_PHOTOS",0,0);
        // Never silently search only part of the gallery or make unbounded billed calls.
        if (candidates.size()>MAX_GALLERY) return summary("GALLERY_LIMIT",0,0);
        var matches=new ArrayList<FaceComparison.Match>();
        int compared=0, skipped=0;
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(25);
        try {
            for (var user:candidates) {
                if (System.nanoTime()>deadline) return summary("UNAVAILABLE",compared,skipped);
                String image=normalizeImage(user.avatarBase64());
                if (image==null) { skipped++; continue; }
                Double score=provider.compare(reference,image);
                if (score==null) { skipped++; continue; }
                if (!Double.isFinite(score) || score<0 || score>100) return summary("UNAVAILABLE",compared,skipped);
                compared++;
                if (score>=threshold) matches.add(new FaceComparison.Match(user.id(),
                    (user.name()+" "+user.lastName()).trim(),score));
            }
        } catch (RuntimeException e) {
            // Do not turn an incomplete/failed search into a definitive non-match.
            return summary("UNAVAILABLE",compared,skipped);
        }
        matches.sort(java.util.Comparator.comparingDouble(FaceComparison.Match::similarity).reversed());
        return new FaceComparison(compared==0 ? "NO_VALID_PHOTOS" : "COMPLETE",compared,skipped,matches);
    }
    private FaceComparison summary(String status,int compared,int skipped) {
        return new FaceComparison(status,compared,skipped,java.util.List.of());
    }
    static String normalizeImage(String value) {
        if (value==null || value.isBlank() || value.length()>2000000) return null;
        if (value.startsWith("data:")) {
            int split=value.indexOf(",");
            if (split<0 || !value.substring(0,split).matches("data:image/(jpeg|png);base64")) return null;
            value=value.substring(split+1);
        }
        value=value.replaceAll("\\s","");
        try {
            byte[] bytes=Base64.getDecoder().decode(value);
            boolean jpeg=bytes.length>3 && (bytes[0]&255)==255 && (bytes[1]&255)==216 && (bytes[2]&255)==255;
            boolean png=bytes.length>8 && bytes[0]==(byte)137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
            return (jpeg || png) && bytes.length<=1500000 ? value:null;
        } catch (IllegalArgumentException e) { return null; }
    }
}
