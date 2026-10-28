package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.port.LivenessProvider;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FaceComparisonUseCaseTest {
    final UserRepository users=mock(UserRepository.class);
    final LivenessProvider provider=mock(LivenessProvider.class);
    final FaceComparisonUseCase service=new FaceComparisonUseCase(users,provider,90);
    // Minimal magic bytes are sufficient for this unit test; AWS validates actual images.
    final String image="/9j/AA==";
    User user(long id,String photo) {
        return new User(id,"Nombre","Apellido","u"+id,"private@example.com","hash",null,null,null,photo);
    }
    @Test void missingReferenceDoesNotQueryGallery() {
        assertThat(service.execute(null).status()).isEqualTo("NO_REFERENCE");
        verifyNoInteractions(users,provider);
    }
    @Test void emptyGalleryDoesNotInvokeAws() {
        when(users.findFaceCandidates(21)).thenReturn(List.of());
        assertThat(service.execute(image).status()).isEqualTo("NO_PHOTOS");
        verifyNoInteractions(provider);
    }
    @Test void excessiveGalleryIsNotSilentlyTruncated() {
        when(users.findFaceCandidates(21)).thenReturn(LongStream.range(0,21).mapToObj(i->user(i,image)).toList());
        assertThat(service.execute(image).status()).isEqualTo("GALLERY_LIMIT");
        verifyNoInteractions(provider);
    }
    @Test void comparesAllValidPhotosAndFiltersBelowThreshold() {
        when(users.findFaceCandidates(21)).thenReturn(List.of(user(1,image),user(2,image),user(3,"invalid")));
        when(provider.compare(image,image)).thenReturn(95.0,80.0);
        var result=service.execute(image);
        assertThat(result.status()).isEqualTo("COMPLETE");
        assertThat(result.compared()).isEqualTo(2);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.matches()).hasSize(1);
        assertThat(result.matches().getFirst().userId()).isEqualTo(1L);
        verify(provider,times(2)).compare(image,image);
    }
    @Test void awsFailureDoesNotExposePartialMatchesAsComplete() {
        when(users.findFaceCandidates(21)).thenReturn(List.of(user(1,image),user(2,image)));
        when(provider.compare(image,image)).thenReturn(99.0).thenThrow(new IllegalStateException());
        var result=service.execute(image);
        assertThat(result.status()).isEqualTo("UNAVAILABLE");
        assertThat(result.matches()).isEmpty();
    }
    @Test void invalidScoresFailClosed() {
        when(users.findFaceCandidates(21)).thenReturn(List.of(user(1,image)));
        when(provider.compare(image,image)).thenReturn(Double.NaN);
        assertThat(service.execute(image).status()).isEqualTo("UNAVAILABLE");
    }
    @Test void noFaceIsSkippedNotReportedAsComparison() {
        when(users.findFaceCandidates(21)).thenReturn(List.of(user(1,image)));
        when(provider.compare(image,image)).thenReturn(null);
        assertThat(service.execute(image).status()).isEqualTo("NO_VALID_PHOTOS");
    }
    @Test void normalizesOnlySupportedImages() {
        assertThat(FaceComparisonUseCase.normalizeImage("data:image/jpeg;base64,"+image)).isEqualTo(image);
        assertThat(FaceComparisonUseCase.normalizeImage("data:image/svg+xml;base64,"+image)).isNull();
        assertThat(FaceComparisonUseCase.normalizeImage("not base64")).isNull();
    }
}
