package io.platformbuilder.depara.util;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import org.apache.commons.lang3.StringUtils;

public final class OracleExecutionScriptBuilder {

    private OracleExecutionScriptBuilder() {
    }

    public static String buildInsert(DestinoImportacaoEnum destino, CommandDTO command) {

        String tableName = resolveTableName(destino);

        return "INSERT INTO NEXTDIGITAL." + tableName + " (" +
                "ID, NOME_FILIAL, DESCRICAO_LEGADO, CODIGO_LEGADO, SISTEMA_LEGADO, DESCRICAO_PROTHEUS, CODIGO_PROTHEUS" +
                ") VALUES (" +
                "(SELECT COALESCE(MAX(ID), 0) + 1 FROM NEXTDIGITAL." + tableName + "), " +
                "'" + escapeSql(command.getNomeFilial()) + "', " +
                "'" + escapeSql(command.getDescricaoLegado()) + "', " +
                "'" + escapeSql(command.getCodigoLegado()) + "', " +
                "'" + escapeSql(command.getSistemaLegado().toUpperCase()) + "', " +
                "'" + escapeSql(command.getDescricaoProtheus()) + "', " +
                "'" + escapeSql(command.getCodigoProtheus()) + "'" +
                ");";
    }

    public static String buildUpdate(DestinoImportacaoEnum destino, CommandDTO command) {
        String tableName = resolveTableName(destino);
        return "UPDATE NEXTDIGITAL." + tableName + " SET " +
                "DESCRICAO_LEGADO = '" + escapeSql(command.getDescricaoLegado()) + "', " +
                "DESCRICAO_PROTHEUS = '" + escapeSql(command.getDescricaoProtheus()) + "', " +
                "CODIGO_PROTHEUS = '" + escapeSql(command.getCodigoProtheus()) + "' " +
                "WHERE NOME_FILIAL = '" + escapeSql(command.getNomeFilial()) + "' " +
                "AND CODIGO_LEGADO = '" + escapeSql(command.getCodigoLegado()) + "' " +
                "AND SISTEMA_LEGADO = '" + escapeSql(command.getSistemaLegado().toUpperCase()) + "';";
    }

    public static String buildDelete(DestinoImportacaoEnum destino, CommandDTO command) {
        String tableName = resolveTableName(destino);
        return "DELETE FROM NEXTDIGITAL." + tableName + " " +
                "WHERE NOME_FILIAL = '" + escapeSql(command.getNomeFilial()) + "' " +
                "AND CODIGO_LEGADO = '" + escapeSql(command.getCodigoLegado()) + "' " +
                "AND SISTEMA_LEGADO = '" + escapeSql(command.getSistemaLegado().toUpperCase()) + "';";
    }

    private static String resolveTableName(DestinoImportacaoEnum destino) {
        switch (destino) {
            case CONTA_CONTABIL_OPERADORA:
                return "CONTA_CONTABIL_OPERADORA";
            case CENTRO_CUSTO:
                return "CENTRO_CUSTO";
            default:
                throw new IllegalArgumentException("Destino não suportado: " + destino);
        }
    }

    private static String escapeSql(String value) {

        if (StringUtils.isBlank(value)) {
            return "";
        }

        return value.replace("'", "''");
    }

}
