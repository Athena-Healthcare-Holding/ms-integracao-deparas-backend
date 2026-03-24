package io.platformbuilder.depara.dto.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeParaExecutionItemDTOTest {

    @Test
    void deveCriarDtoComTodosOsDadosDoItemCorretamente() {

        DeParaExecutionItemDTO dto = new DeParaExecutionItemDTO(
                15,
                "Filial São Paulo",
                "LEG123",
                "PROT456",
                "INSERT INTO TABELA ...",
                "UPDATE TABELA SET ...",
                "DELETE FROM TABELA ..."
        );

        assertAll("Deve manter todos os dados informados no construtor",
                () -> assertEquals(15, dto.getLinhaOrigemExcel()),
                () -> assertEquals("Filial São Paulo", dto.getNomeFilial()),
                () -> assertEquals("LEG123", dto.getCodigoLegado()),
                () -> assertEquals("PROT456", dto.getCodigoProtheus()),
                () -> assertEquals("INSERT INTO TABELA ...", dto.getInsertOracle()),
                () -> assertEquals("UPDATE TABELA SET ...", dto.getUpdateOracle()),
                () -> assertEquals("DELETE FROM TABELA ...", dto.getDeleteOracle())
        );

    }

}
