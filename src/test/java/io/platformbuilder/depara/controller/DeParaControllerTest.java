package io.platformbuilder.depara.controller;


import io.platformbuilder.depara.dto.ImportResultDTO;
import io.platformbuilder.depara.dto.ItemResultDTO;
import io.platformbuilder.depara.enums.DestinoImportacaoEnum;
import io.platformbuilder.depara.enums.StatusEnum;
import io.platformbuilder.depara.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class DeParaControllerTest {

    private MockMvc mockMvc;

    private ImportService service;

    @BeforeEach
    void setUp() {
        service = mock(ImportService.class);
        DeParaController controller = new DeParaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveProcessarUploadExcelComSucessoParaCentroCusto() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "modelo.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "conteudo".getBytes()
        );

        ItemResultDTO itemInserido = new ItemResultDTO(
                2,
                "02004015",
                "LEG001",
                "PRO001",
                StatusEnum.INSERIDO,
                "Registro inserido com sucesso"
        );

        ImportResultDTO resultado = new ImportResultDTO(
                Collections.emptyList(),
                Collections.singletonList(itemInserido),
                Collections.emptyList(),
                1
        );

        when(service.importar(eq(arquivo), eq(DestinoImportacaoEnum.CENTRO_CUSTO))).thenReturn(resultado);

        mockMvc.perform(fileUpload("/api/depara/centro-custo/import")
                        .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalRecebidos").value(1))
                .andExpect(jsonPath("$.totalInseridos").value(1))
                .andExpect(jsonPath("$.totalAtualizados").value(0))
                .andExpect(jsonPath("$.totalErros").value(0))
                .andExpect(jsonPath("$.inseridos[0].linhaOrigemExcel").value(2))
                .andExpect(jsonPath("$.inseridos[0].nomeFilial").value("02004015"))
                .andExpect(jsonPath("$.inseridos[0].codigoLegado").value("LEG001"))
                .andExpect(jsonPath("$.inseridos[0].codigoProtheus").value("PRO001"))
                .andExpect(jsonPath("$.inseridos[0].status").value("INSERIDO"))
                .andExpect(jsonPath("$.inseridos[0].mensagem").value("Registro inserido com sucesso"));

        verify(service).importar(eq(arquivo), eq(DestinoImportacaoEnum.CENTRO_CUSTO));
    }

    @Test
    void deveProcessarUploadExcelComSucessoParaContaContabilOperadora() throws Exception {

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "modelo.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "conteudo".getBytes()
        );

        ImportResultDTO resultado = new ImportResultDTO(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                0
        );

        when(service.importar(eq(arquivo), eq(DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA))).thenReturn(resultado);

        mockMvc.perform(fileUpload("/api/depara/conta-contabil-operadora/import")
                        .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecebidos").value(0))
                .andExpect(jsonPath("$.totalInseridos").value(0))
                .andExpect(jsonPath("$.totalAtualizados").value(0))
                .andExpect(jsonPath("$.totalErros").value(0));

        verify(service).importar(eq(arquivo), eq(DestinoImportacaoEnum.CONTA_CONTABIL_OPERADORA));
    }

}
