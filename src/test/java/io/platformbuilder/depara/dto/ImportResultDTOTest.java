package io.platformbuilder.depara.dto;

import io.platformbuilder.depara.enums.StatusEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportResultDTOTest {

    @Test
    void deveCriarDtoCalculandoTotaisECopiandoListasCorretamente() {

        ItemResultDTO itemAtualizado = new ItemResultDTO(
                1,
                "02004015",
                "LEG001",
                "PRO001",
                StatusEnum.ATUALIZADO,
                "Registro atualizado com sucesso"
        );

        ItemResultDTO itemInserido1 = new ItemResultDTO(
                2,
                "02004016",
                "LEG002",
                "PRO002",
                StatusEnum.INSERIDO,
                "Registro inserido com sucesso"
        );

        ItemResultDTO itemInserido2 = new ItemResultDTO(
                3,
                "02004017",
                "LEG003",
                "PRO003",
                StatusEnum.INSERIDO,
                "Registro inserido com sucesso"
        );

        ItemResultDTO itemErro = new ItemResultDTO(
                4,
                "02004018",
                "LEG004",
                "PRO004",
                StatusEnum.ERRO,
                "Erro ao processar registro"
        );

        List<ItemResultDTO> atualizados = new ArrayList<>();
        atualizados.add(itemAtualizado);

        List<ItemResultDTO> inseridos = new ArrayList<>();
        inseridos.add(itemInserido1);
        inseridos.add(itemInserido2);

        List<ItemResultDTO> erros = new ArrayList<>();
        erros.add(itemErro);

        ImportResultDTO dto = new ImportResultDTO(
                atualizados,
                inseridos,
                erros,
                10
        );

        assertAll("Deve calcular totais e manter os itens corretamente",
                () -> assertEquals(10, dto.getTotalRecebidos()),
                () -> assertEquals(1, dto.getTotalAtualizados()),
                () -> assertEquals(2, dto.getTotalInseridos()),
                () -> assertEquals(1, dto.getTotalErros()),

                () -> assertEquals(1, dto.getAtualizados().size()),
                () -> assertEquals(2, dto.getInseridos().size()),
                () -> assertEquals(1, dto.getErros().size()),

                () -> assertSame(itemAtualizado, dto.getAtualizados().get(0)),
                () -> assertSame(itemInserido1, dto.getInseridos().get(0)),
                () -> assertSame(itemInserido2, dto.getInseridos().get(1)),
                () -> assertSame(itemErro, dto.getErros().get(0))
        );

    }

    @Test
    void deveFazerCopiaDefensivaDasListasRecebidas() {

        List<ItemResultDTO> atualizados = new ArrayList<>();
        List<ItemResultDTO> inseridos = new ArrayList<>();
        List<ItemResultDTO> erros = new ArrayList<>();

        ImportResultDTO dto = new ImportResultDTO(
                atualizados,
                inseridos,
                erros,
                5
        );

        inseridos.add(new ItemResultDTO(
                1,
                "02004015",
                "LEG001",
                "PRO001",
                StatusEnum.INSERIDO,
                "Registro inserido com sucesso"
        ));

        assertAll("Alterações nas listas originais não devem afetar o DTO",
                () -> assertEquals(0, dto.getTotalInseridos()),
                () -> assertTrue(dto.getInseridos().isEmpty())
        );

    }

    @Test
    void deveRetornarListasImutaveis() {

        ImportResultDTO dto = new ImportResultDTO(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0
        );

        assertAll("As listas expostas pelo DTO devem ser imutáveis",
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> dto.getAtualizados().add(null)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> dto.getInseridos().add(null)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> dto.getErros().add(null))
        );

    }

}
