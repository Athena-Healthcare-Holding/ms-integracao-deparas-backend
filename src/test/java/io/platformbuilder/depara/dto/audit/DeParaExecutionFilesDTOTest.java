package io.platformbuilder.depara.dto.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeParaExecutionFilesDTOTest {

    @Test
    void deveCriarDtoComTodosOsCaminhosDeArquivosCorretamente() {

        DeParaExecutionFilesDTO dto = new DeParaExecutionFilesDTO(
                "/tmp/depara/exec-123",
                "/tmp/depara/exec-123/audit.json",
                "/tmp/depara/exec-123/rollback.sql",
                "/tmp/depara/exec-123/insert.sql",
                "/tmp/depara/exec-123/update.sql"
        );

        assertAll("Deve manter todos os caminhos informados no construtor",
                () -> assertEquals("/tmp/depara/exec-123", dto.getExecutionFolder()),
                () -> assertEquals("/tmp/depara/exec-123/audit.json", dto.getJsonFilePath()),
                () -> assertEquals("/tmp/depara/exec-123/rollback.sql", dto.getRollbackSqlFilePath()),
                () -> assertEquals("/tmp/depara/exec-123/insert.sql", dto.getInsertSqlFilePath()),
                () -> assertEquals("/tmp/depara/exec-123/update.sql", dto.getUpdateSqlFilePath())
        );

    }

}
