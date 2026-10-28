package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.FaceComparison;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FaceComparisonConverterTest {
    final FaceComparisonConverter converter=new FaceComparisonConverter();
    @Test void preservesSummaryWithoutImages() {
        var result=new FaceComparison("COMPLETE",2,1,List.of(new FaceComparison.Match(7L,"Nombre",98.5)));
        String stored=converter.convertToDatabaseColumn(result);
        assertThat(stored).doesNotContain("Base64","password","email");
        assertThat(converter.convertToEntityAttribute(stored)).isEqualTo(result);
    }
    @Test void oldRowsMayHaveNoSummary() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }
}
