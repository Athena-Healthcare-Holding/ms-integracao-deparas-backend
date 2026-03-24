package io.platformbuilder.depara.dto.audit;

import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public final class DeParaExecutionAuditDTO {

    private final String executionId;
    private final DestinoImportacaoEnum destinoImportacao;
    private final String dataHoraExecucao;
    private final String arquivoOrigem;
    private final int totalRecebidos;
    private final int totalInseridos;
    private final int totalAtualizados;
    private final int totalErros;
    private final List<DeParaExecutionItemDTO> registrosInseridos;
    private final List<DeParaExecutionItemDTO> registrosAtualizados;

}
