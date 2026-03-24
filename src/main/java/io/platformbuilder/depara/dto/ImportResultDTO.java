package io.platformbuilder.depara.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Schema(
        name = "ImportResultDTO",
        description = "Resultado do processamento da importação do arquivo Excel de de-para."
)
public final class ImportResultDTO {

    @Schema(description = "Quantidade total de registros recebidos no arquivo Excel.", example = "50")
    private final int totalRecebidos;

    @Schema(description = "Quantidade total de registros inseridos com sucesso na base de dados.", example = "45")
    private final int totalInseridos;

    @Schema(description = "Quantidade total de registros atualizados com sucesso.", example = "25")
    private final int totalAtualizados;

    @Schema(description = "Quantidade total de registros que apresentaram erro.", example = "2")
    private final int totalErros;

    @Schema(description = "Lista de registros que foram inseridos com sucesso.")
    private final List<ItemResultDTO> inseridos;

    @Schema(description = "Lista de registros atualizados com sucesso.")
    private final List<ItemResultDTO> atualizados;

    @Schema(description = "Lista de registros que apresentaram erro durante o processamento.")
    private final List<ItemResultDTO> erros;

    public ImportResultDTO(List<ItemResultDTO> atualizados,
                           List<ItemResultDTO> inseridos,
                           List<ItemResultDTO> erros,
                           int totalRecebidos) {

        this.atualizados = Collections.unmodifiableList(new ArrayList<>(atualizados));
        this.inseridos = Collections.unmodifiableList(new ArrayList<>(inseridos));
        this.erros = Collections.unmodifiableList(new ArrayList<>(erros));

        this.totalAtualizados = atualizados.size();
        this.totalInseridos = inseridos.size();
        this.totalRecebidos = totalRecebidos;
        this.totalErros = erros.size();

    }

}
