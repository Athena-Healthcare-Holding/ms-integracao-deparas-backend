package io.platformbuilder.depara.sql;

public final class SqlStatements {

    private SqlStatements() {
    }

    public static final String INSERT_CONTA_CONTABIL_OPERADORA =
            "INSERT INTO NEXTDIGITAL.CONTA_CONTABIL_OPERADORA (" +
                    "ID, NOME_FILIAL, DESCRICAO_LEGADO, CODIGO_LEGADO, SISTEMA_LEGADO, DESCRICAO_PROTHEUS, CODIGO_PROTHEUS" +
                    ") VALUES (" +
                    "(SELECT COALESCE(MAX(ID), 0) + 1 FROM NEXTDIGITAL.CONTA_CONTABIL_OPERADORA), ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_CONTA_CONTABIL_OPERADORA =
            "UPDATE NEXTDIGITAL.CONTA_CONTABIL_OPERADORA SET " +
                    "DESCRICAO_LEGADO = ?, " +
                    "DESCRICAO_PROTHEUS = ?, " +
                    "CODIGO_PROTHEUS = ? " +
                    "WHERE NOME_FILIAL = ? " +
                    "AND CODIGO_LEGADO = ? " +
                    "AND SISTEMA_LEGADO = ?";

    public static final String INSERT_CENTRO_CUSTO =
            "INSERT INTO NEXTDIGITAL.CENTRO_CUSTO (" +
                    "ID, NOME_FILIAL, DESCRICAO_LEGADO, CODIGO_LEGADO, SISTEMA_LEGADO, DESCRICAO_PROTHEUS, CODIGO_PROTHEUS" +
                    ") VALUES (" +
                    "(SELECT COALESCE(MAX(ID), 0) + 1 FROM NEXTDIGITAL.CENTRO_CUSTO), ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_CENTRO_CUSTO =
            "UPDATE NEXTDIGITAL.CENTRO_CUSTO SET " +
                    "DESCRICAO_LEGADO = ?, " +
                    "DESCRICAO_PROTHEUS = ?, " +
                    "CODIGO_PROTHEUS = ? " +
                    "WHERE NOME_FILIAL = ? " +
                    "AND CODIGO_LEGADO = ? " +
                    "AND SISTEMA_LEGADO = ?";


    public static final String SELECT_EXISTENTES_CONTA_CONTABIL_OPERADORA =
            "SELECT NOME_FILIAL, CODIGO_LEGADO " +
                    "FROM NEXTDIGITAL.CONTA_CONTABIL_OPERADORA " +
                    "WHERE UPPER(SISTEMA_LEGADO) IN (%s) " +
                    "AND NOME_FILIAL IN (%s) " +
                    "AND CODIGO_LEGADO IN (%s)";

    public static final String SELECT_EXISTENTES_CENTRO_CUSTO =
            "SELECT NOME_FILIAL, CODIGO_LEGADO " +
                    "FROM NEXTDIGITAL.CENTRO_CUSTO " +
                    "WHERE UPPER(SISTEMA_LEGADO) IN (%s) " +
                    "AND NOME_FILIAL IN (%s) " +
                    "AND CODIGO_LEGADO IN (%s)";

}
