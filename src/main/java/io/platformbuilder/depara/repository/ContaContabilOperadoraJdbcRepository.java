package io.platformbuilder.depara.repository;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.sql.SqlStatements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class ContaContabilOperadoraJdbcRepository implements ImportJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(ContaContabilOperadoraJdbcRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public ContaContabilOperadoraJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DestinoImportacaoEnum getDestino() {
        return DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA;
    }

    @Override
    public Set<String> findExistingKeys(List<CommandDTO> commands) {

        Set<String> filiais = new HashSet<>();
        Set<String> codigosLegado = new HashSet<>();
        Set<String> sistemas = new HashSet<>();

        for (CommandDTO command : commands) {
            filiais.add(command.getNomeFilial());
            codigosLegado.add(command.getCodigoLegado());
            sistemas.add(command.getSistemaLegado().toUpperCase());
        }

        if (filiais.isEmpty() || codigosLegado.isEmpty()) {
            return new HashSet<>();
        }

        String filialPlaceholders = buildPlaceholders(filiais.size());
        String codigoPlaceholders = buildPlaceholders(codigosLegado.size());
        String sistemaPlaceholders = buildPlaceholders(sistemas.size());

        String sql = String.format(
                SqlStatements.SELECT_EXISTENTES_CONTA_CONTABIL_OPERADORA,
                sistemaPlaceholders,
                filialPlaceholders,
                codigoPlaceholders
        );


        List<Object> params = new ArrayList<>();
        params.addAll(sistemas);
        params.addAll(filiais);
        params.addAll(codigosLegado);

        if (log.isDebugEnabled()) {
            log.debug("SQL findExistingKeys conta_contabil_operadora ->\n{}", sql);
            log.debug("Quantidade de parâmetros: {}", params.size());
        }

        List<String> keys = jdbcTemplate.query(
                sql,
                params.toArray(),
                (rs, rowNum) -> rs.getString("NOME_FILIAL") + "|" + rs.getString("CODIGO_LEGADO")
        );

        return new HashSet<>(keys);
    }

    @Override
    public int[] batchInsert(final List<CommandDTO> commands) {

        if (Objects.isNull(commands) || commands.isEmpty()) {
            return new int[0];
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL batchInsert conta_contabil_operadora ->\n{}", SqlStatements.INSERT_CONTA_CONTABIL_OPERADORA);
            log.debug("Quantidade para batchInsert: {}", commands.size());
        }

        return jdbcTemplate.batchUpdate(
                SqlStatements.INSERT_CONTA_CONTABIL_OPERADORA,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        CommandDTO command = commands.get(i);
                        ps.setString(1, command.getNomeFilial());
                        ps.setString(2, command.getDescricaoLegado());
                        ps.setString(3, command.getCodigoLegado());
                        ps.setString(4, command.getSistemaLegado());
                        ps.setString(5, command.getDescricaoProtheus());
                        ps.setString(6, command.getCodigoProtheus());
                    }

                    @Override
                    public int getBatchSize() {
                        return commands.size();
                    }
                }
        );
    }

    @Override
    public int[] batchUpdate(final List<CommandDTO> commands) {

        if (Objects.isNull(commands) || commands.isEmpty()) {
            return new int[0];
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL batchUpdate conta_contabil_operadora ->\n{}", SqlStatements.UPDATE_CONTA_CONTABIL_OPERADORA);
            log.debug("Quantidade para batchUpdate: {}", commands.size());
        }

        return jdbcTemplate.batchUpdate(
                SqlStatements.UPDATE_CONTA_CONTABIL_OPERADORA,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        CommandDTO command = commands.get(i);
                        ps.setString(1, command.getDescricaoLegado());
                        ps.setString(2, command.getDescricaoProtheus());
                        ps.setString(3, command.getCodigoProtheus());
                        ps.setString(4, command.getNomeFilial());
                        ps.setString(5, command.getCodigoLegado());
                        ps.setString(6, command.getSistemaLegado());
                    }

                    @Override
                    public int getBatchSize() {
                        return commands.size();
                    }
                }
        );
    }

    @Override
    public int insert(CommandDTO command) {

        if (Objects.isNull(command)) {
            throw new IllegalArgumentException("Command para insert não pode ser nulo.");
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL insert individual conta_contabil_operadora ->\n{}", SqlStatements.INSERT_CONTA_CONTABIL_OPERADORA);
            log.debug("Parâmetros insert: filial={}, codigoLegado={}, codigoProtheus={}",
                    command.getNomeFilial(), command.getCodigoLegado(), command.getCodigoProtheus());
        }

        return jdbcTemplate.update(
                SqlStatements.INSERT_CONTA_CONTABIL_OPERADORA,
                command.getNomeFilial(),
                command.getDescricaoLegado(),
                command.getCodigoLegado(),
                command.getSistemaLegado(),
                command.getDescricaoProtheus(),
                command.getCodigoProtheus()
        );
    }

    @Override
    public int update(CommandDTO command) {

        if (Objects.isNull(command)) {
            throw new IllegalArgumentException("Command para update não pode ser nulo.");
        }

        if (log.isDebugEnabled()) {
            log.debug("SQL update individual conta_contabil_operadora ->\n{}", SqlStatements.UPDATE_CONTA_CONTABIL_OPERADORA);
            log.debug("Parâmetros update: filial={}, codigoLegado={}, codigoProtheus={}",
                    command.getNomeFilial(), command.getCodigoLegado(), command.getCodigoProtheus());
        }

        return jdbcTemplate.update(
                SqlStatements.UPDATE_CONTA_CONTABIL_OPERADORA,
                command.getDescricaoLegado(),
                command.getDescricaoProtheus(),
                command.getCodigoProtheus(),
                command.getNomeFilial(),
                command.getCodigoLegado(),
                command.getSistemaLegado()
        );
    }

    private String buildPlaceholders(int size) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }

        return sb.toString();
    }

}
