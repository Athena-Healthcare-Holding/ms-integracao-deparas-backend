package io.platformbuilder.depara.dto;

import io.platformbuilder.depara.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "ItemResultDTO",
        description = "Representa o resultado do processamento de um registro individual."
)
public final class ItemResultDTO {

    @Schema(description = "Número da linha no arquivo Excel.", example = "4")
    private final Integer linhaOrigemExcel;

    @Schema(description = "Código da filial.", example = "02004015")
    private final String nomeFilial;

    @Schema(description = "Código legado.", example = "2111110310007")
    private final String codigoLegado;

    @Schema(description = "Código Protheus.", example = "2111110310003")
    private final String codigoProtheus;

    @Schema(
            description = "Status do processamento.",
            example = "ATUALIZADO",
            allowableValues = {"INSERIDO", "ATUALIZADO", "ERRO"}
    )
    private final StatusEnum status;

    @Schema(description = "Mensagem do processamento.", example = "Registro atualizado com sucesso")
    private final String mensagem;

}
