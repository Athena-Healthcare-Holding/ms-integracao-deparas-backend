-- Massa de dados TEMPORÁRIA criada apenas para simular os registros do banco real.
-- Utilizada para validação da regra de importação do de-para, onde é verificada
-- previamente a existência da filial (CODIGO_PROTHEUS) antes da persistência.
-- Pode ser removida ou substituída quando houver integração com a base oficial.

INSERT INTO NEXTDIGITAL.FILIAL (
    ID,
    CODIGO_PROTHEUS,
    NOME,
    CNPJ,
    ENDERECO,
    COMPLEMENTO,
    BAIRRO,
    CIDADE,
    UF
) VALUES
(2004001, '02004001', 'Filial Brasília Centro', '12345678000195', 'SCS Quadra 01 Bloco A', NULL, 'Asa Sul', 'Brasília', 'DF'),
(2004002, '02004002', 'Filial Asa Norte', '12345678000276', 'CLN 102 Bloco B', NULL, 'Asa Norte', 'Brasília', 'DF'),
(2004003, '02004003', 'Filial Taguatinga', '12345678000357', 'QNA 15 Lote 10', NULL, 'Taguatinga Norte', 'Taguatinga', 'DF'),
(2004004, '02004004', 'Filial Ceilândia', '12345678000438', 'QNM 17 Conjunto H', NULL, 'Ceilândia Norte', 'Ceilândia', 'DF'),
(2004005, '02004005', 'Filial Samambaia', '12345678000519', 'QS 401 Conjunto D', NULL, 'Samambaia Sul', 'Samambaia', 'DF'),
(2004006, '02004006', 'Filial Águas Claras', '12345678000690', 'Rua 36 Sul Lote 05', NULL, 'Águas Claras Sul', 'Águas Claras', 'DF'),
(2004007, '02004007', 'Filial Gama', '12345678000771', 'Quadra 12 Lote 08', NULL, 'Setor Central', 'Gama', 'DF'),
(2004008, '02004008', 'Filial Sobradinho', '12345678000852', 'Quadra 08 Conjunto F', NULL, 'Sobradinho', 'Sobradinho', 'DF'),
(2004009, '02004009', 'Filial Planaltina', '12345678000933', 'Setor Tradicional Quadra 02', NULL, 'Centro', 'Planaltina', 'DF'),
(2004010, '02004010', 'Filial Recanto das Emas', '12345678001014', 'Quadra 203 Lote 12', NULL, 'Recanto das Emas', 'Recanto das Emas', 'DF'),
(2004011, '02004011', 'Filial Santa Maria', '12345678001105', 'QR 310 Conjunto 02', NULL, 'Santa Maria Sul', 'Santa Maria', 'DF'),
(2004012, '02004012', 'Filial Riacho Fundo', '12345678001288', 'QN 07 Conjunto 03', NULL, 'Riacho Fundo I', 'Riacho Fundo', 'DF'),
(2004013, '02004013', 'Filial Guará', '12345678001369', 'QE 40 Conjunto B', NULL, 'Guará II', 'Guará', 'DF'),
(2004014, '02004014', 'Filial Núcleo Bandeirante', '12345678001440', 'Avenida Central Bloco 12', NULL, 'Centro', 'Núcleo Bandeirante', 'DF'),
(2004015, '02004015', 'Filial São Sebastião', '12345678001521', 'Quadra 101 Conjunto 05', NULL, 'Centro', 'São Sebastião', 'DF'),
(2004016, '02004016', 'Filial Paranoá', '12345678001602', 'Quadra 02 Conjunto A', NULL, 'Paranoá', 'Paranoá', 'DF'),
(2004017, '02004017', 'Filial Itapoã', '12345678001793', 'Quadra 61 Conjunto D', NULL, 'Itapoã', 'Itapoã', 'DF');