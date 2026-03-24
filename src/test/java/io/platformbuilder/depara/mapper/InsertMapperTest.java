package io.platformbuilder.depara.mapper;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.dto.InsertDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertMapperTest {

    private final InsertMapper mapper = new InsertMapper();

    @Test
    void deveParsearExcelValidoCorretamente() throws Exception {

        byte[] excelBytes = createExcel(
                new String[]{"FILIAL", "EMPRESA", "PARA CÓDIGO PROTHEUS", "DE CÓDIGO LEGADO", "DESCRIÇÃO"},
                new Object[]{"02004015;02004016", "mv", "2111110310003", "2111110310007", "Descricao teste"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "importacao.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        List<InsertDTO> result = mapper.parse(file);

        assertEquals(1, result.size());

        InsertDTO dto = result.get(0);

        assertAll("Deve mapear corretamente a linha do Excel",
                () -> assertEquals(2, dto.getLinhaOrigemExcel()),
                () -> assertEquals(Arrays.asList("02004015", "02004016"), dto.getFiliais()),
                () -> assertEquals("MV", dto.getEmpresa()),
                () -> assertEquals("2111110310003", dto.getParaCodigoProtheus()),
                () -> assertEquals("2111110310007", dto.getDeCodigoLegado()),
                () -> assertEquals("Descricao teste", dto.getDescricao())
        );

    }

    @Test
    void deveIgnorarLinhasEmBranco() throws Exception {

        byte[] excelBytes = createExcelWithRows(
                new String[]{"FILIAL", "EMPRESA", "PARA CÓDIGO PROTHEUS", "DE CÓDIGO LEGADO", "DESCRIÇÃO"},
                new Object[]{"02004015", "MV", "100", "200", "Descricao 1"},
                null,
                new Object[]{"   ", "   ", "   ", "   ", "   "},
                new Object[]{"02004016", "TASY", "300", "400", "Descricao 2"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "importacao.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        List<InsertDTO> result = mapper.parse(file);

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getLinhaOrigemExcel());
        assertEquals(5, result.get(1).getLinhaOrigemExcel());

    }

    @Test
    void deveLancarExcecaoQuandoArquivoForNuloOuVazio() {

        IllegalArgumentException exceptionNulo = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.parse(null)
        );

        MockMultipartFile fileVazio = new MockMultipartFile(
                "file",
                "importacao.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        IllegalArgumentException exceptionVazio = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.parse(fileVazio)
        );

        assertAll(
                () -> assertEquals("Arquivo não enviado ou vazio.", exceptionNulo.getMessage()),
                () -> assertEquals("Arquivo não enviado ou vazio.", exceptionVazio.getMessage())
        );

    }

    @Test
    void deveLancarExcecaoQuandoExtensaoDoArquivoForInvalida() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "importacao.txt",
                "text/plain",
                "conteudo".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.parse(file)
        );

        assertEquals("Arquivo inválido. Envie um .xlsx ou .xls", exception.getMessage());

    }

    @Test
    void deveLancarExcecaoQuandoCabecalhoForInvalido() throws Exception {

        byte[] excelBytes = createExcel(
                new String[]{"FILIAL", "EMPRESA", "CODIGO ERRADO", "DE CÓDIGO LEGADO", "DESCRIÇÃO"},
                new Object[]{"02004015", "MV", "100", "200", "Descricao"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "importacao.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.parse(file)
        );

        assertTrue(exception.getMessage().contains("Layout do Excel diferente do esperado"));

    }

    @Test
    void deveLancarExcecaoQuandoLinhaObrigatoriaEstiverInvalida() throws Exception {

        byte[] excelBytes = createExcel(
                new String[]{"FILIAL", "EMPRESA", "PARA CÓDIGO PROTHEUS", "DE CÓDIGO LEGADO", "DESCRIÇÃO"},
                new Object[]{"02004015", "", "100", "200", "Descricao"}
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "importacao.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.parse(file)
        );

        assertTrue(exception.getMessage().contains("Linha 2: EMPRESA é obrigatório."));
    }

    @Test
    void deveConverterInsertDtosEmCommandsExpandindoPorFilial() {

        InsertDTO row1 = new InsertDTO(
                2,
                Arrays.asList("02004015", "02004016"),
                "MV",
                "PRO001",
                "LEG001",
                "Descricao 1"
        );

        InsertDTO row2 = new InsertDTO(
                3,
                Collections.singletonList("02004017"),
                "TASY",
                "PRO002",
                "LEG002",
                "Descricao 2"
        );

        List<CommandDTO> commands = InsertMapper.toCommands(Arrays.asList(row1, row2));

        assertEquals(3, commands.size());

        assertAll("Deve expandir corretamente uma linha por filial",
                () -> assertEquals(2, commands.get(0).getLinhaOrigemExcel()),
                () -> assertEquals("02004015", commands.get(0).getNomeFilial()),
                () -> assertEquals("Descricao 1", commands.get(0).getDescricaoLegado()),
                () -> assertEquals("LEG001", commands.get(0).getCodigoLegado()),
                () -> assertEquals("MV", commands.get(0).getSistemaLegado()),
                () -> assertEquals("Descricao 1", commands.get(0).getDescricaoProtheus()),
                () -> assertEquals("PRO001", commands.get(0).getCodigoProtheus()),
                () -> assertEquals("02004016", commands.get(1).getNomeFilial()),
                () -> assertEquals("02004017", commands.get(2).getNomeFilial()),
                () -> assertEquals("TASY", commands.get(2).getSistemaLegado())
        );

    }

    @Test
    void deveIgnorarRowsSemFiliaisNoToCommands() {

        InsertDTO rowSemFiliais = new InsertDTO(
                2,
                Collections.emptyList(),
                "MV",
                "PRO001",
                "LEG001",
                "Descricao"
        );

        InsertDTO rowValida = new InsertDTO(
                3,
                Collections.singletonList("02004015"),
                "TASY",
                "PRO002",
                "LEG002",
                "Descricao 2"
        );

        List<CommandDTO> commands = InsertMapper.toCommands(Arrays.asList(rowSemFiliais, rowValida));

        assertEquals(1, commands.size());
        assertEquals("02004015", commands.get(0).getNomeFilial());

    }

    private byte[] createExcel(String[] header, Object[] dataRow) throws Exception {
        return createExcelWithRows(header, dataRow);
    }

    private byte[] createExcelWithRows(String[] header, Object[]... rows) throws Exception {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Planilha");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }

            int rowIndex = 1;

            for (Object[] rowData : rows) {

                if (rowData == null) {
                    rowIndex++;
                    continue;
                }

                Row row = sheet.createRow(rowIndex++);

                for (int i = 0; i < rowData.length; i++) {

                    if (rowData[i] == null) {
                        continue;
                    }

                    if (rowData[i] instanceof Number) {
                        row.createCell(i).setCellValue(((Number) rowData[i]).doubleValue());
                    } else {
                        row.createCell(i).setCellValue(String.valueOf(rowData[i]));
                    }

                }

            }

            workbook.write(out);

            return out.toByteArray();
        }

    }

}
