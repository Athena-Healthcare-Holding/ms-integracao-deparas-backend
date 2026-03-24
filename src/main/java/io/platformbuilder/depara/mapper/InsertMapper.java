package io.platformbuilder.depara.mapper;

import io.platformbuilder.depara.dto.CommandDTO;
import io.platformbuilder.depara.dto.InsertDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class InsertMapper {

    private static final int HEADER_ROW_INDEX = 0;

    private static final int COL_FILIAL = 0;
    private static final int COL_EMPRESA = 1;
    private static final int COL_PARA_PROTHEUS = 2;
    private static final int COL_DE_LEGADO = 3;
    private static final int COL_DESCRICAO = 4;

    public List<InsertDTO> parse(MultipartFile file) {

        validateExcelFile(file);

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet);

            DataFormatter formatter = new DataFormatter();
            List<InsertDTO> result = new ArrayList<>();

            for (int rowIndex = HEADER_ROW_INDEX + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (Objects.isNull(row) || isRowBlank(row, formatter)) {
                    continue;
                }

                String filiaisRaw = readTextCell(row, COL_FILIAL, formatter);
                String empresa = readTextCell(row, COL_EMPRESA, formatter);
                String paraProtheus = readCodeCell(row, COL_PARA_PROTHEUS);
                String deLegado = readCodeCell(row, COL_DE_LEGADO);
                String descricao = readTextCell(row, COL_DESCRICAO, formatter);

                InsertDTO dto = new InsertDTO(
                        rowIndex + 1,
                        FilialMapper.parse(filiaisRaw),
                        Objects.isNull(empresa) ? null : empresa.toUpperCase(),
                        normalizeCode(paraProtheus),
                        normalizeCode(deLegado),
                        descricao
                );

                dto.validateOrThrow(rowIndex + 1);
                result.add(dto);

            }

            return result;

        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao ler Excel: " + e.getMessage(), e);
        }

    }

    public static List<CommandDTO> toCommands(List<InsertDTO> rows) {

        List<CommandDTO> commands = new ArrayList<>();

        for (InsertDTO row : rows) {

            if (Objects.isNull(row.getFiliais()) || row.getFiliais().isEmpty()) {
                continue;
            }

            for (String filial : row.getFiliais()) {
                commands.add(new CommandDTO(
                        row.getLinhaOrigemExcel(),
                        filial,
                        row.getDescricao(),
                        row.getDeCodigoLegado(),
                        row.getEmpresa(),
                        row.getDescricao(),
                        row.getParaCodigoProtheus()
                ));
            }

        }

        return commands;
    }

    private void validateExcelFile(MultipartFile file) {

        if (Objects.isNull(file) || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não enviado ou vazio.");
        }

        String name = file.getOriginalFilename();

        if (Objects.isNull(name) || (!name.endsWith(".xlsx") && !name.endsWith(".xls"))) {
            throw new IllegalArgumentException("Arquivo inválido. Envie um .xlsx ou .xls");
        }

    }

    private void validateHeader(Sheet sheet) {

        Row header = sheet.getRow(HEADER_ROW_INDEX);

        if (Objects.isNull(header)) {
            throw new IllegalArgumentException("Cabeçalho não encontrado na primeira linha.");
        }

        DataFormatter formatter = new DataFormatter();

        String h0 = readTextCell(header, COL_FILIAL, formatter);
        String h1 = readTextCell(header, COL_EMPRESA, formatter);
        String h2 = readTextCell(header, COL_PARA_PROTHEUS, formatter);
        String h3 = readTextCell(header, COL_DE_LEGADO, formatter);
        String h4 = readTextCell(header, COL_DESCRICAO, formatter);

        if (!equalsIgnoreCaseTrim(h0, "FILIAL")
                || !equalsIgnoreCaseTrim(h1, "EMPRESA")
                || !equalsIgnoreCaseTrim(h2, "PARA CÓDIGO PROTHEUS")
                || !equalsIgnoreCaseTrim(h3, "DE CÓDIGO LEGADO")
                || !equalsIgnoreCaseTrim(h4, "DESCRIÇÃO")) {

            throw new IllegalArgumentException("Layout do Excel diferente do esperado. Verifique os cabeçalhos (A..E).");
        }

    }

    private boolean isRowBlank(Row row, DataFormatter formatter) {
        for (int columnIndex = 0; columnIndex <= COL_DESCRICAO; columnIndex++) {
            String value = readTextCell(row, columnIndex, formatter);
            if (StringUtils.isNotBlank(value)) {
                return false;
            }
        }
        return true;
    }

    private String readTextCell(Row row, int columnIndex, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return readTextCell(cell, formatter);
    }

    private String readTextCell(Cell cell, DataFormatter formatter) {

        if (Objects.isNull(cell)) {
            return null;
        }

        String value = formatter.formatCellValue(cell);

        if (Objects.isNull(value)) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String readCodeCell(Row row, int columnIndex) {

        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (Objects.isNull(cell)) {
            return null;
        }

        if (CellType.NUMERIC.equals(cell.getCellType())) {
            return normalizeCode(NumberToTextConverter.toText(cell.getNumericCellValue()));
        }

        if (CellType.STRING.equals(cell.getCellType())) {
            return normalizeCode(cell.getStringCellValue());
        }

        if (CellType.FORMULA.equals(cell.getCellType())) {
            return readFormulaCellAsCode(cell);
        }

        return normalizeCode(cell.toString());
    }

    private String readFormulaCellAsCode(Cell cell) {

        CellType cachedFormulaResultType = cell.getCachedFormulaResultType();

        if (CellType.NUMERIC.equals(cachedFormulaResultType)) {
            return normalizeCode(NumberToTextConverter.toText(cell.getNumericCellValue()));
        }

        if (CellType.STRING.equals(cachedFormulaResultType)) {
            return normalizeCode(cell.getStringCellValue());
        }

        return normalizeCode(cell.toString());
    }

    private boolean equalsIgnoreCaseTrim(String a, String b) {

        if (StringUtils.isBlank(a) || StringUtils.isBlank(b)) {
            return false;
        }

        return normalize(a).equals(normalize(b));
    }

    private String normalize(String value) {

        if (StringUtils.isBlank(value)) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {

        if (StringUtils.isBlank(code)) {
            return null;
        }

        String normalizedCode = code.trim();
        return normalizedCode.isEmpty() ? null : normalizedCode;
    }

}
