package br.com.semblefe.perfis.api;

import br.com.semblefe.perfis.aplicacao.porta.entrada.ListarEspecialidadesCasoUso;
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
@RequestMapping("/api/v1/publico/especialidades")
@Tag(name = "Especialidades", description = "Atuações artísticas e profissionais disponíveis")
public class EspecialidadeController {

    private final ListarEspecialidadesCasoUso casoUso;

    public EspecialidadeController(ListarEspecialidadesCasoUso casoUso) {
        this.casoUso = casoUso;
    }

    @GetMapping
    @Operation(summary = "Lista as especialidades ativas")
    public ResponseEntity<List<EspecialidadeResposta>> listar() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(casoUso.executar().stream()
                        .map(EspecialidadeResposta::de)
                        .toList());
    }
}
