package io.platformbuilder.depara.controller;

import io.platformbuilder.depara.dto.ImportResultDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/depara")
@Tag(
        name = "De-Para Conta Contábil SOLUS",
        description = "Endpoints responsáveis pelo download do modelo Excel e pela importação do de-para de contas contábeis do sistema legado SOLUS."
)
public class DeParaController {

    private static final String TEMPLATE_FILE_PATH = "modelo/modelo_planilha_de_para.xlsx";
    private static final String TEMPLATE_DOWNLOAD_NAME = "modelo_planilha_de_para.xlsx";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ImportService service;

    @Operation(
            summary = "Importar planilha de de-para de conta contábil SOLUS",
            description = "Recebe um arquivo Excel no formato do modelo oficial e realiza o processamento dos registros de de-para de conta contábil do legado SOLUS. " +
                    "O retorno informa os itens inseridos com sucesso, os itens ignorados por já existirem e os itens que apresentaram erro durante o processamento."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arquivo processado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ImportResultDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arquivo inválido, vazio ou fora do layout esperado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno durante o processamento da importação",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    @PostMapping(
            value = "/{destinoImportacao}/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ImportResultDTO> uploadExcel(
            @PathVariable("destinoImportacao") String destinoImportacao,
            @Parameter(
                    description = "Arquivo Excel no padrão do modelo de de-para de conta contábil SOLUS",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = @Encoding(name = "arquivo", contentType = EXCEL_CONTENT_TYPE)
                    )
            )
            @RequestParam("arquivo") MultipartFile arquivo) {
        DestinoImportacaoEnum destino = DestinoImportacaoEnum.fromPathValue(destinoImportacao);
        return ResponseEntity.ok(service.importar(arquivo, destino));
    }

    @Operation(
            summary = "Baixar modelo Excel de importação",
            description = "Realiza o download do arquivo modelo Excel que deve ser utilizado como base para preenchimento e posterior importação do de-para de conta contábil SOLUS."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arquivo modelo localizado e disponibilizado para download",
                    content = @Content(mediaType = EXCEL_CONTENT_TYPE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Arquivo modelo não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao tentar disponibilizar o arquivo modelo",
                    content = @Content
            )
    })
    @GetMapping("/modelo")
    public ResponseEntity<Resource> downloadModelo() throws IOException {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_FILE_PATH);

        if (!resource.exists()) {
            throw new IllegalArgumentException("Arquivo modelo não encontrado: " + TEMPLATE_FILE_PATH);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(TEMPLATE_DOWNLOAD_NAME)
                        .build()
        );
        headers.setContentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE));
        headers.setContentLength(resource.contentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

}
