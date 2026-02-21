# Water Tariff API

API REST para gerenciamento e **cálculo** de tarifas de água com base em categorias de consumidores e faixas progressivas de consumo.

Desenvolvido como desafio técnico para o processo seletivo do **GrupoRAS**.

---

## Índice

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação e Configuração](#instalação-e-configuração)
- [Execução](#execução)
- [Arquitetura](#arquitetura)
- [Modelo de Dados](#modelo-de-dados)
- [Endpoints da API](#endpoints-da-api)
- [Exemplos de Requisições e Respostas](#exemplos-de-requisições-e-respostas)
- [Regras de Negócio](#regras-de-negócio)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)

---

## Visão Geral

A API permite:

- **Criar** tabelas tarifárias completas com categorias e faixas de consumo via JSON.
- **Listar** todas as tabelas tarifárias cadastradas.
- **Consultar** uma tabela tarifária específica por ID.
- **Excluir** tabelas tarifárias (com cascade para categorias e faixas).
- **Calcular** o valor a pagar com base na categoria do consumidor e volume consumido, utilizando **cálculo progressivo por faixas**.

O sistema é **totalmente parametrizável**: alterações de valores e faixas são feitas exclusivamente no banco de dados, sem necessidade de modificar código ou reiniciar a aplicação.

---

## Tecnologias

| Tecnologia | Versão | Propósito |
|---|---|---|
| Java | 17+ | Linguagem de programação |
| Spring Boot | 3.2.3 | Framework para API REST |
| Spring Data JPA | 3.2.x | ORM e acesso a dados |
| Spring Validation | 3.2.x | Validação de dados de entrada |
| PostgreSQL | 14+ | Banco de dados relacional |
| Hibernate | 6.x | Implementação JPA |
| Lombok | 1.18.x | Redução de boilerplate |
| Maven | 3.8+ | Gerenciamento de dependências |
| JUnit 5 + Mockito | — | Testes unitários e de integração |

---

## Pré-requisitos

Antes de executar a aplicação, certifique-se de ter instalado:

- **Java JDK 17** ou superior  
- **Maven 3.8** ou superior  
- **PostgreSQL 14** ou superior  
- **Git 2.x** ou superior  

Verificação rápida:

```bash
java -version      # deve exibir 17+
mvn -version       # deve exibir 3.8+
psql --version     # deve exibir 14+
git --version      # deve exibir 2.x+
```

---

## Instalação e Configuração

### 1. Clonar o repositório

```bash
git clone https://github.com/BrunoBelarmino007/water-tariff-api.git
cd water-tariff-api
```

### 2. Criar o banco de dados

```bash
# Acessar o PostgreSQL
sudo -u postgres psql

# Criar o banco
CREATE DATABASE water_tariff;

# (Opcional) Criar usuário dedicado
CREATE USER gruporas WITH ENCRYPTED PASSWORD 'senha_segura';
GRANT ALL PRIVILEGES ON DATABASE water_tariff TO gruporas;

\q
```

### 3. Executar scripts SQL

```bash
# Criar as tabelas
psql -U postgres -d water_tariff -f scripts/schema.sql

# Inserir dados de exemplo (opcional)
psql -U postgres -d water_tariff -f scripts/seed.sql
```

### 4. Configurar a conexão

Edite o arquivo `src/main/resources/application.properties` ou defina as variaveis de ambiente:

```properties
# Via application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/water_tariff
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Ou via variaveis de ambiente (recomendado em producao):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/water_tariff
export DB_USER=postgres
export DB_PASS=postgres
```

---

## Execução

### Compilar e executar

```bash
# Compilar o projeto (inclui execucao de testes)
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

A API estara disponivel em: **http://localhost:8080**

### Verificacao rapida

```bash
curl http://localhost:8080/api/tabelas-tarifarias
# Retorna [] (lista vazia) ou as tabelas existentes
```

---

## Arquitetura

A aplicação segue a arquitetura em camadas do Spring Boot:

```
                         Cliente (Postman / cURL / Frontend)
                                      |
                                      v
  +--------------------------------------------------------------+
  |                     Controller Layer                          |
  |      TabelaTarifariaController  |  CalculoController          |
  |      Recebe requests, valida entrada, delega para Service     |
  +--------------------------------------------------------------+
                                      |
                                      v
  +--------------------------------------------------------------+
  |                      Service Layer                            |
  |       TabelaTarifariaService  |  CalculoService               |
  |       Logica de negocio, validacoes, calculo progressivo      |
  +--------------------------------------------------------------+
                                      |
                                      v
  +--------------------------------------------------------------+
  |                     Repository Layer                          |
  |  TabelaTarifariaRepository | CategoriaTarifaRepository        |
  |  FaixaConsumoRepository                                       |
  |  Acesso a dados via Spring Data JPA                           |
  +--------------------------------------------------------------+
                                      |
                                      v
  +--------------------------------------------------------------+
  |                      PostgreSQL                               |
  |  tabelas_tarifarias -> categorias_tarifa -> faixas_consumo    |
  +--------------------------------------------------------------+
```

---

## Modelo de Dados

### Diagrama Entidade-Relacionamento

```
+------------------------+       +-------------------------+       +------------------------+
|  tabelas_tarifarias    |       |   categorias_tarifa     |       |    faixas_consumo      |
+------------------------+       +-------------------------+       +------------------------+
| PK id (BIGSERIAL)      |--1:N->| PK id (BIGSERIAL)      |--1:N->| PK id (BIGSERIAL)      |
|    nome (VARCHAR)      |       |    categoria (VARCHAR)  |       |    inicio (INTEGER)    |
|    data_vigencia (DATE)|       | FK tabela_tarifaria_id  |       |    fim (INTEGER)       |
|    ativa (BOOLEAN)     |       +-------------------------+       |    valor_unitario      |
|    created_at          |                                         |       (NUMERIC 10,2)   |
+------------------------+                                         | FK categoria_tarifa_id |
                                                                   +------------------------+
```

### Categorias suportadas

| Categoria | Descrição |
|---|---|
| COMERCIAL | Estabelecimentos comerciais |
| INDUSTRIAL | Industrias e fabricas |
| PARTICULAR | Residencias |
| PUBLICO | Orgaos publicos |

---

## Endpoints da API

### Tabelas Tarifarias

| Metodo | Endpoint | HTTP Status | Descricao |
|---|---|---|---|
| `POST` | `/api/tabelas-tarifarias` | 201 Created | Cria tabela tarifaria completa |
| `GET` | `/api/tabelas-tarifarias` | 200 OK | Lista todas as tabelas |
| `GET` | `/api/tabelas-tarifarias/{id}` | 200 OK | Busca tabela por ID |
| `DELETE` | `/api/tabelas-tarifarias/{id}` | 204 No Content | Remove tabela (cascade) |
| `GET` | `/api/tabelas-tarifarias/{id}/categorias/{cat}/faixas` | 200 OK | Lista faixas de uma categoria |

### Cálculos

| Metodo | Endpoint | HTTP Status | Descricao |
|---|---|---|---|
| `POST` | `/api/calculos` | 200 OK | Calcula tarifa progressiva por faixas |

---

## Exemplos de Requisições e Respostas

### POST /api/tabelas-tarifarias - Criar tabela completa

**Request:**

```bash
curl -X POST http://localhost:8080/api/tabelas-tarifarias \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Tabela Tarifaria 2025",
    "dataVigencia": "2025-01-01",
    "categorias": [
      {
        "categoria": "COMERCIAL",
        "faixas": [
          {"inicio": 0, "fim": 10, "valorUnitario": 5.00},
          {"inicio": 11, "fim": 20, "valorUnitario": 8.00},
          {"inicio": 21, "fim": 30, "valorUnitario": 12.00},
          {"inicio": 31, "fim": 99999, "valorUnitario": 15.00}
        ]
      },
      {
        "categoria": "INDUSTRIAL",
        "faixas": [
          {"inicio": 0, "fim": 10, "valorUnitario": 4.00},
          {"inicio": 11, "fim": 20, "valorUnitario": 6.50},
          {"inicio": 21, "fim": 30, "valorUnitario": 10.00},
          {"inicio": 31, "fim": 99999, "valorUnitario": 13.00}
        ]
      },
      {
        "categoria": "PARTICULAR",
        "faixas": [
          {"inicio": 0, "fim": 10, "valorUnitario": 3.00},
          {"inicio": 11, "fim": 20, "valorUnitario": 5.00},
          {"inicio": 21, "fim": 30, "valorUnitario": 8.00},
          {"inicio": 31, "fim": 99999, "valorUnitario": 11.00}
        ]
      },
      {
        "categoria": "PUBLICO",
        "faixas": [
          {"inicio": 0, "fim": 10, "valorUnitario": 2.50},
          {"inicio": 11, "fim": 20, "valorUnitario": 4.00},
          {"inicio": 21, "fim": 30, "valorUnitario": 6.50},
          {"inicio": 31, "fim": 99999, "valorUnitario": 9.00}
        ]
      }
    ]
  }'
```

**Response (201 Created):**

```json
{
  "id": 1,
  "nome": "Tabela Tarifaria 2025",
  "dataVigencia": "2025-01-01",
  "ativa": true,
  "createdAt": "2025-01-15T10:30:00",
  "categorias": [
    {
      "id": 1,
      "categoria": "COMERCIAL",
      "faixas": [
        {"id": 1, "inicio": 0, "fim": 10, "valorUnitario": 5.00},
        {"id": 2, "inicio": 11, "fim": 20, "valorUnitario": 8.00},
        {"id": 3, "inicio": 21, "fim": 30, "valorUnitario": 12.00},
        {"id": 4, "inicio": 31, "fim": 99999, "valorUnitario": 15.00}
      ]
    },
    {
      "id": 2,
      "categoria": "INDUSTRIAL",
      "faixas": [
        {"id": 5, "inicio": 0, "fim": 10, "valorUnitario": 4.00},
        {"id": 6, "inicio": 11, "fim": 20, "valorUnitario": 6.50},
        {"id": 7, "inicio": 21, "fim": 30, "valorUnitario": 10.00},
        {"id": 8, "inicio": 31, "fim": 99999, "valorUnitario": 13.00}
      ]
    }
  ]
}
```

### GET /api/tabelas-tarifarias - Listar todas

```bash
curl http://localhost:8080/api/tabelas-tarifarias
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "nome": "Tabela Tarifaria 2025",
    "dataVigencia": "2025-01-01",
    "ativa": true,
    "createdAt": "2025-01-15T10:30:00",
    "categorias": [...]
  }
]
```

### GET /api/tabelas-tarifarias/{id} - Buscar por ID

```bash
curl http://localhost:8080/api/tabelas-tarifarias/1
```

**Response (200 OK):** Retorna a tabela completa com categorias e faixas.

**Response (404 Not Found):**

```json
{
  "timestamp": "2025-01-15T14:30:00",
  "status": 404,
  "erro": "Recurso nao encontrado",
  "mensagem": "Tabela tarifaria com ID 999 nao encontrada"
}
```

### DELETE /api/tabelas-tarifarias/{id} - Excluir tabela

```bash
curl -X DELETE http://localhost:8080/api/tabelas-tarifarias/1
```

**Response:** 204 No Content (sem body).

### POST /api/calculos - Calcular valor a pagar

**Request:**

```bash
curl -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{"categoria": "INDUSTRIAL", "consumo": 18}'
```

**Response (200 OK):**

```json
{
  "categoria": "INDUSTRIAL",
  "consumoTotal": 18,
  "valorTotal": 92.00,
  "detalhamento": [
    {
      "faixa": {"inicio": 0, "fim": 10},
      "m3Cobrados": 10,
      "valorUnitario": 4.00,
      "subtotal": 40.00
    },
    {
      "faixa": {"inicio": 11, "fim": 20},
      "m3Cobrados": 8,
      "valorUnitario": 6.50,
      "subtotal": 52.00
    }
  ]
}
```

### Exemplos de Erros

**Categoria invalida (400 Bad Request):**

```json
{
  "timestamp": "2025-01-15T14:30:00",
  "status": 400,
  "erro": "Erro de validacao",
  "mensagem": "Categoria invalida: 'RESIDENCIAL'. Valores aceitos: COMERCIAL, INDUSTRIAL, PARTICULAR, PUBLICO"
}
```

**Campos obrigatorios ausentes (400 Bad Request):**

```json
{
  "timestamp": "2025-01-15T14:30:00",
  "status": 400,
  "erro": "Erro de validacao dos campos",
  "mensagem": "O campo 'categoria' e obrigatorio; O campo 'consumo' e obrigatorio"
}
```

**Faixas com sobreposicao (400 Bad Request):**

```json
{
  "timestamp": "2025-01-15T14:30:00",
  "status": 400,
  "erro": "Erro de validacao",
  "mensagem": "Categoria 'COMERCIAL': Sobreposicao detectada entre faixas [0-10] e [8-20]"
}
```

---

## Regras de Negocio

### Validacoes de Faixas de Consumo

| Regra | Descricao | Exemplo Invalido |
|---|---|---|
| Ordem valida | `inicio < fim` em cada faixa | `[10, 5]` |
| Cobertura completa | Primeira faixa deve iniciar em `0` | `[5, 10], [11, 20]` |
| Nao sobreposicao | Faixas nao podem ter intervalos cruzados | `[0, 10], [8, 20]` |
| Sem lacunas | Faixas consecutivas sem intervalos vazios | `[0, 10], [15, 20]` |
| Categoria valida | Deve ser COMERCIAL, INDUSTRIAL, PARTICULAR ou PUBLICO | `RESIDENCIAL` |

### Calculo Progressivo

O consumo e distribuido faixa a faixa. Para 35 m3 na categoria COMERCIAL:

```
Faixa 1: [0-10]    -> 10 m3 x R$ 5,00  = R$  50,00
Faixa 2: [11-20]   -> 10 m3 x R$ 8,00  = R$  80,00
Faixa 3: [21-30]   -> 10 m3 x R$ 12,00 = R$ 120,00
Faixa 4: [31-99999] ->  5 m3 x R$ 15,00 = R$  75,00
                                    TOTAL = R$ 325,00
```

### Parametrizacao Total

Para demonstrar que o sistema e totalmente parametrizavel:

```sql
-- Alterar valor da faixa 1 COMERCIAL de R$5.00 para R$6.00
UPDATE faixas_consumo SET valor_unitario = 6.00 WHERE id = 1;

-- O proximo calculo para COMERCIAL ja usara o novo valor
-- SEM PRECISAR ALTERAR CODIGO OU REINICIAR A APLICAçãO
```

---

## Testes

O projeto conta com **35 testes** automatizados, organizados em duas camadas:

### Testes Unitarios (Service Layer)

| Classe | Testes | Cobertura |
|---|---|---|
| `CalculoServiceTest` | 4 | Calculo progressivo, categoria invalida, tabela inexistente |
| `TabelaTarifariaServiceTest` | 17 | CRUD completo, validacoes de faixas, conversao Entity/DTO |

### Testes de Integracao (Controller Layer)

| Classe | Testes | Cobertura |
|---|---|---|
| `CalculoControllerTest` | 2 | POST /api/calculos (sucesso e validacao) |
| `TabelaTarifariaControllerTest` | 12 | Todos os endpoints (POST, GET, GET/{id}, DELETE) + erros |

### Executar os testes

```bash
# Executar todos os testes
mvn test

# Executar uma classe especifica
mvn test -Dtest=TabelaTarifariaServiceTest

# Output esperado:
# Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

Para o guia completo de testes com cenarios detalhados, consulte o arquivo [GUIA_TESTE.md](GUIA_TESTE.md).

---

## Estrutura do Projeto

```
water-tariff-api/
├── pom.xml
├── README.md
├── GUIA_TESTE.md
├── scripts/
│   ├── schema.sql                               # DDL - Criacao das tabelas
│   └── seed.sql                                 # DML - Dados de exemplo
└── src/
    ├── main/java/com/gruporas/watertariffapi/
    │   ├── WatertariffapiApplication.java       # Classe principal
    │   ├── model/                               # Entidades JPA
    │   │   ├── CategoriaEnum.java
    │   │   ├── TabelaTarifaria.java
    │   │   ├── CategoriaTarifa.java
    │   │   └── FaixaConsumo.java
    │   ├── dto/                                 # Data Transfer Objects
    │   │   ├── TabelaTarifariaRequest.java
    │   │   ├── CategoriaRequest.java
    │   │   ├── FaixaRequest.java
    │   │   ├── CalculoRequest.java
    │   │   ├── TabelaTarifariaResponse.java
    │   │   ├── CategoriaResponse.java
    │   │   ├── FaixaResponse.java
    │   │   ├── CalculoResponse.java
    │   │   └── DetalhamentoFaixaResponse.java
    │   ├── repository/                          # Spring Data Repositories
    │   │   ├── TabelaTarifariaRepository.java
    │   │   ├── CategoriaTarifaRepository.java
    │   │   └── FaixaConsumoRepository.java
    │   ├── service/                             # Logica de negocio
    │   │   ├── TabelaTarifariaService.java
    │   │   └── CalculoService.java
    │   ├── controller/                          # Endpoints REST
    │   │   ├── TabelaTarifariaController.java
    │   │   └── CalculoController.java
    │   └── exception/                           # Tratamento de erros
    │       ├── BusinessException.java
    │       ├── ResourceNotFoundException.java
    │       └── GlobalExceptionHandler.java
    ├── main/resources/
    │   └── application.properties
    └── test/java/com/gruporas/watertariffapi/
        ├── service/
        │   ├── TabelaTarifariaServiceTest.java  # 17 testes unitarios
        │   └── CalculoServiceTest.java          # 4 testes unitarios
        └── controller/
            ├── TabelaTarifariaControllerTest.java # 12 testes de integracao
            └── CalculoControllerTest.java         # 2 testes de integracao
```

---
Ó profundidade das riquezas, tanto da sabedoria, como do conhecimento de Deus! Quão insondáveis são os seus juízos, e quão inescrutáveis os seus caminhos! Porque, quem compreendeu a mente do Senhor? Ou quem foi seu conselheiro? Ou quem lhe deu primeiro a ele, para que lhe seja recompensado? Porque dele e por ele, e para ele, são todas as coisas; glória, pois, a ele eternamente. Amém. 

Romanos 11:33-36
---

## Autor

Desenvolvido por **Bruno Belarmino** como parte do processo seletivo para Desenvolvedor Java no GrupoRAS.
