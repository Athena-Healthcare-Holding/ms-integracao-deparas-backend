package io.platformbuilder.depara.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class FilialJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(FilialJdbcRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public FilialJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<String> findExistingCodigosProtheus(Set<String> codigosFilial) {

        if (Objects.isNull(codigosFilial) || codigosFilial.isEmpty()) {
            return new HashSet<>();
        }

        String sql =
                "SELECT CODIGO_PROTHEUS " +
                        "FROM NEXTDIGITAL.FILIAL " +
                        "WHERE CODIGO_PROTHEUS IN (" + buildPlaceholders(codigosFilial.size()) + ")";

        List<Object> params = new ArrayList<>(codigosFilial);

        if (log.isDebugEnabled()) {
            log.debug("SQL consulta filial ->\n{}", sql);
            log.debug("Total parametros consulta filial: {}", params.size());
        }

        List<String> result = jdbcTemplate.query(
                sql,
                params.toArray(),
                (rs, rowNum) -> rs.getString("CODIGO_PROTHEUS")
        );

        return new HashSet<>(result);
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
