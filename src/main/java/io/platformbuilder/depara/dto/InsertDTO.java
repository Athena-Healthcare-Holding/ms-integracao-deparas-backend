package io.platformbuilder.depara.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class InsertDTO {

    private final Integer linhaOrigemExcel;
    private final List<String> filiais;
    private final String empresa;
    private final String paraCodigoProtheus;
    private final String deCodigoLegado;
    private final String descricao;

    public InsertDTO(
            Integer linhaOrigemExcel,
            List<String> filiais,
            String empresa,
            String paraCodigoProtheus,
            String deCodigoLegado,
            String descricao
    ) {
        this.linhaOrigemExcel = linhaOrigemExcel;
        this.filiais = Objects.isNull(filiais) ? Collections.emptyList() : Collections.unmodifiableList(filiais);
        this.empresa = normalize(empresa);
        this.paraCodigoProtheus = normalize(paraCodigoProtheus);
        this.deCodigoLegado = normalize(deCodigoLegado);
        this.descricao = normalize(descricao);
    }

    private static String normalize(String value) {
        if (StringUtils.isBlank(value)) return null;
        return value.trim();
    }

    public void validateOrThrow(int excelRowNumber) {
        if (filiais.isEmpty())
            throw new IllegalArgumentException("Linha " + excelRowNumber + ": FILIAL é obrigatório.");
        if (empresa == null) throw new IllegalArgumentException("Linha " + excelRowNumber + ": EMPRESA é obrigatório.");
        if (paraCodigoProtheus == null)
            throw new IllegalArgumentException("Linha " + excelRowNumber + ": PARA CÓDIGO PROTHEUS é obrigatório.");
        if (deCodigoLegado == null)
            throw new IllegalArgumentException("Linha " + excelRowNumber + ": DE CÓDIGO LEGADO é obrigatório.");
        if (descricao == null)
            throw new IllegalArgumentException("Linha " + excelRowNumber + ": DESCRIÇÃO é obrigatório.");
    }

}
