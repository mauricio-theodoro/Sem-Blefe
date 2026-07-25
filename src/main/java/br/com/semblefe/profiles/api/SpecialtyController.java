package br.com.semblefe.profiles.api;

import br.com.semblefe.profiles.application.port.inbound.ListSpecialtiesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/specialties")
@Tag(name = "Specialties", description = "Atuações artísticas e profissionais disponíveis")
public class SpecialtyController {

    private final ListSpecialtiesUseCase listSpecialtiesUseCase;

    public SpecialtyController(ListSpecialtiesUseCase listSpecialtiesUseCase) {
        this.listSpecialtiesUseCase = listSpecialtiesUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista as especialidades ativas")
    public ResponseEntity<List<SpecialtyResponse>> list() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(listSpecialtiesUseCase.execute().stream()
                        .map(SpecialtyResponse::from)
                        .toList());
    }
}
