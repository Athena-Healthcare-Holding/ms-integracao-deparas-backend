package io.platformbuilder.depara.dto;


import io.platformbuilder.depara.enums.StatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemResultDTOTest {

    @Test
    void deveCriarDtoComResultadoDoProcessamentoCorretamente() {

        ItemResultDTO dto = new ItemResultDTO(
                4,
                "02004015",
                "2111110310007",
                "2111110310003",
                StatusEnum.ATUALIZADO,
                "Registro atualizado com sucesso"
        );

        assertAll("Deve manter os dados do resultado do processamento",
                () -> assertEquals(4, dto.getLinhaOrigemExcel()),
                () -> assertEquals("02004015", dto.getNomeFilial()),
                () -> assertEquals("2111110310007", dto.getCodigoLegado()),
                () -> assertEquals("2111110310003", dto.getCodigoProtheus()),
                () -> assertEquals(StatusEnum.ATUALIZADO, dto.getStatus()),
                () -> assertEquals("Registro atualizado com sucesso", dto.getMensagem())
        );

    }

}
