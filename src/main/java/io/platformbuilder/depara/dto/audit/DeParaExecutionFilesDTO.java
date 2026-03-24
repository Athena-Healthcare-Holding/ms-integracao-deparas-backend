package io.platformbuilder.depara.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class DeParaExecutionFilesDTO {

    private final String executionFolder;
    private final String jsonFilePath;
    private final String rollbackSqlFilePath;
    private final String insertSqlFilePath;
    private final String updateSqlFilePath;

}
