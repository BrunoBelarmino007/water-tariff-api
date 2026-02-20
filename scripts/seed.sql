-- Script de Dados de Exemplo (Seed)
-- API de Tabela Tarifaria de Agua - GrupoRAS


-- Inserir Tabela Tarifaria de Exemplo

INSERT INTO tabelas_tarifarias (nome, data_vigencia, ativa, created_at)
VALUES ('Tabela Tarifaria 2025', '2025-01-01', TRUE, CURRENT_TIMESTAMP);


-- Categoria: COMERCIAL

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('COMERCIAL', 1);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 5.00,  1),
(11, 20, 8.00,  1),
(21, 30, 12.00, 1),
(31, 99999, 15.00, 1);


-- Categoria: INDUSTRIAL

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('INDUSTRIAL', 1);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 4.00,  2),
(11, 20, 6.50,  2),
(21, 30, 10.00, 2),
(31, 99999, 13.00, 2);


-- Categoria: PARTICULAR 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PARTICULAR', 1);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 3.00,  3),
(11, 20, 5.00,  3),
(21, 30, 8.00,  3),
(31, 99999, 11.00, 3);


-- Categoria: PUBLICO

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PUBLICO', 1);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 2.50,  4),
(11, 20, 4.00,  4),
(21, 30, 6.50,  4),
(31, 99999, 9.00,  4);


-- TABELA TARIFARIA 2025-02

INSERT INTO tabelas_tarifarias (nome, data_vigencia, ativa, created_at)
VALUES ('Tabela Tarifaria 2025-02', '2025-02-01', TRUE, CURRENT_TIMESTAMP);


-- Categoria: COMERCIAL 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('COMERCIAL', 2);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 5.50,  5),
(11, 20, 8.50,  5),
(21, 30, 12.50, 5),
(31, 99999, 15.50, 5);


-- Categoria: INDUSTRIAL 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('INDUSTRIAL', 2);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 4.50,  6),
(11, 20, 7.00,  6),
(21, 30, 10.50, 6),
(31, 99999, 13.50, 6);


-- Categoria: PARTICULAR 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PARTICULAR', 2);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 3.50,  7),
(11, 20, 5.50,  7),
(21, 30, 8.50,  7),
(31, 99999, 11.50, 7);


-- Categoria: PUBLICO 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PUBLICO', 2);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 3.00,  8),
(11, 20, 4.50,  8),
(21, 30, 7.00,  8),
(31, 99999, 9.50,  8);


-- TABELA TARIFARIA 2025-03

INSERT INTO tabelas_tarifarias (nome, data_vigencia, ativa, created_at)
VALUES ('Tabela Tarifaria 2025-03', '2025-03-01', TRUE, CURRENT_TIMESTAMP);


-- Categoria: COMERCIAL 
INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('COMERCIAL', 3);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 6.00,  9),
(11, 20, 9.00,  9),
(21, 30, 13.00, 9),
(31, 99999, 16.00, 9);


-- Categoria: INDUSTRIAL 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('INDUSTRIAL', 3);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 5.00,  10),
(11, 20, 7.50,  10),
(21, 30, 11.00, 10),
(31, 99999, 14.00, 10);


-- Categoria: PARTICULAR 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PARTICULAR', 3);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 4.00,  11),
(11, 20, 6.00,  11),
(21, 30, 9.00,  11),
(31, 99999, 12.00, 11);


-- Categoria: PUBLICO 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PUBLICO', 3);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 3.50,  12),
(11, 20, 5.00,  12),
(21, 30, 7.50,  12),
(31, 99999, 10.00, 12);


-- TABELA TARIFARIA 2025-04

INSERT INTO tabelas_tarifarias (nome, data_vigencia, ativa, created_at)
VALUES ('Tabela Tarifaria 2025-04', '2025-04-01', TRUE, CURRENT_TIMESTAMP);


-- Categoria: COMERCIAL 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('COMERCIAL', 4);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 6.50,  13),
(11, 20, 9.50,  13),
(21, 30, 13.50, 13),
(31, 99999, 16.50, 13);


-- Categoria: INDUSTRIAL 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('INDUSTRIAL', 4);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 5.50,  14),
(11, 20, 8.00,  14),
(21, 30, 11.50, 14),
(31, 99999, 14.50, 14);


-- Categoria: PARTICULAR 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PARTICULAR', 4);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 4.50,  15),
(11, 20, 6.50,  15),
(21, 30, 9.50,  15),
(31, 99999, 12.50, 15);


-- Categoria: PUBLICO 

INSERT INTO categorias_tarifa (categoria, tabela_tarifaria_id)
VALUES ('PUBLICO', 4);

INSERT INTO faixas_consumo (inicio, fim, valor_unitario, categoria_tarifa_id) VALUES
(0,  10, 4.00,  16),
(11, 20, 5.50,  16),
(21, 30, 8.00,  16),
(31, 99999, 10.50, 16);
