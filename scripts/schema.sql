-- Script de Criacao do Banco de Dados
-- API de Tabela Tarifaria de Agua - GrupoRAS

-- Criar banco de dados (executar separadamente se necessario)

CREATE DATABASE water_tariff;


-- Tabela: tabelas_tarifarias

CREATE TABLE IF NOT EXISTS tabelas_tarifarias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_vigencia DATE NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- Tabela: categorias_tarifa

CREATE TABLE IF NOT EXISTS categorias_tarifa (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(50) NOT NULL,
    tabela_tarifaria_id BIGINT NOT NULL,
    CONSTRAINT fk_categoria_tabela FOREIGN KEY (tabela_tarifaria_id)
        REFERENCES tabelas_tarifarias(id) ON DELETE CASCADE,
    CONSTRAINT chk_categoria CHECK (
        categoria IN ('COMERCIAL', 'INDUSTRIAL', 'PARTICULAR', 'PUBLICO')
    )
);


-- Tabela: faixas_consumo

CREATE TABLE IF NOT EXISTS faixas_consumo (
    id BIGSERIAL PRIMARY KEY,
    inicio INTEGER NOT NULL,
    fim INTEGER NOT NULL,
    valor_unitario NUMERIC(10, 2) NOT NULL,
    categoria_tarifa_id BIGINT NOT NULL,
    CONSTRAINT fk_faixa_categoria FOREIGN KEY (categoria_tarifa_id)
        REFERENCES categorias_tarifa(id) ON DELETE CASCADE,
    CONSTRAINT chk_inicio_menor_fim CHECK (inicio < fim),
    CONSTRAINT chk_inicio_positivo CHECK (inicio >= 0),
    CONSTRAINT chk_valor_positivo CHECK (valor_unitario > 0)
);


-- Indices para otimizacao de consultas

CREATE INDEX IF NOT EXISTS idx_categorias_tabela ON categorias_tarifa(tabela_tarifaria_id);
CREATE INDEX IF NOT EXISTS idx_categorias_tipo ON categorias_tarifa(categoria);
CREATE INDEX IF NOT EXISTS idx_faixas_categoria ON faixas_consumo(categoria_tarifa_id);
CREATE INDEX IF NOT EXISTS idx_faixas_inicio ON faixas_consumo(inicio);
CREATE INDEX IF NOT EXISTS idx_tabelas_ativa ON tabelas_tarifarias(ativa);
CREATE INDEX IF NOT EXISTS idx_tabelas_vigencia ON tabelas_tarifarias(data_vigencia);
