# Guia de Testes - Water Tariff API

Guia completo para validacao da API de Tabela Tarifaria de Agua, cobrindo testes automatizados (unitarios e de integracao) e testes manuais via cURL.

---

## Indice

1. [Visao Geral da Estrategia de Testes](#1-visao-geral-da-estrategia-de-testes)
2. [Ferramentas Utilizadas](#2-ferramentas-utilizadas)
3. [Executando os Testes](#3-executando-os-testes)
4. [Testes Unitarios - CalculoServiceTest](#4-testes-unitarios---calculoservicetest)
5. [Testes Unitarios - TabelaTarifariaServiceTest](#5-testes-unitarios---tabelatarifariaservicetest)
6. [Testes de Integracao - CalculoControllerTest](#6-testes-de-integracao---calculocontrollertest)
7. [Testes de Integracao - TabelaTarifariaControllerTest](#7-testes-de-integracao---tabelatarifariacontrollertest)
8. [Testes E2E via cURL](#8-testes-e2e-via-curl)
9. [Matriz de Cobertura](#9-matriz-de-cobertura)

---

## 1. Visao Geral da Estrategia de Testes

A aplicacao adota a piramide de testes classica, priorizando testes unitarios na base e testes de integracao na camada intermediaria:

```
         /\
        /  \           Testes E2E (cURL / Postman)
       / E2E\          Validacao manual do fluxo completo
      /------\
     /        \        Testes de Integracao (MockMvc)
    / Integr.  \       Controller + Service mockado + HTTP
   /------------\
  /              \     Testes Unitarios (Mockito)
 / Unit Tests     \    Service isolado com mocks de Repository
/------------------\
```

**Resumo quantitativo:**

| Camada | Classes | Testes | Tipo |
|---|---|---|---|
| Unitarios | `CalculoServiceTest` | 4 | Mockito + JUnit 5 |
| Unitarios | `TabelaTarifariaServiceTest` | 17 | Mockito + JUnit 5 |
| Integracao | `CalculoControllerTest` | 2 | MockMvc + WebMvcTest |
| Integracao | `TabelaTarifariaControllerTest` | 12 | MockMvc + WebMvcTest |
| **Total** | **4 classes** | **35 testes** | |

---

## 2. Ferramentas Utilizadas

| Ferramenta | Proposito |
|---|---|
| JUnit 5 | Framework de testes |
| Mockito | Mocking de dependencias (Repository, Service) |
| AssertJ | Assercoes fluentes e legiveislegivel |
| MockMvc | Testes de controllers HTTP sem servidor |
| @WebMvcTest | Contexto Spring parcial (apenas Web layer) |
| @ExtendWith(MockitoExtension) | Integracao Mockito com JUnit 5 |
| @Nested / @DisplayName | Organizacao e legibilidade dos testes |
| ArgumentCaptor | Captura de argumentos passados a mocks |

---

## 3. Executando os Testes

### Executar todos os testes

```bash
mvn test
```

**Output esperado:**

```
Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Executar uma classe especifica

```bash
# Testes do servico de calculo
mvn test -Dtest=CalculoServiceTest

# Testes do servico de tabela tarifaria
mvn test -Dtest=TabelaTarifariaServiceTest

# Testes do controller de calculo
mvn test -Dtest=CalculoControllerTest

# Testes do controller de tabela tarifaria
mvn test -Dtest=TabelaTarifariaControllerTest
```

### Executar com output detalhado

```bash
mvn test -Dtest=TabelaTarifariaServiceTest -Dsurefire.useFile=false
```

---

## 4. Testes Unitarios - CalculoServiceTest

**Arquivo:** `src/test/java/com/gruporas/watertariffapi/service/CalculoServiceTest.java`

Testa a logica de calculo progressivo por faixas de consumo de forma isolada, com mocks para `TabelaTarifariaRepository` e `CategoriaTarifaRepository`.

| # | Cenario | Metodo Testado | Resultado Esperado |
|---|---------|----------------|--------------------|
| 1 | Calculo com consumo em 2 faixas (18 m3 INDUSTRIAL) | `calcular()` | `valorTotal = 92.00`, 2 itens no detalhamento |
| 2 | Calculo com consumo em 1 faixa (5 m3) | `calcular()` | `valorTotal = 20.00`, 1 item no detalhamento |
| 3 | Categoria invalida | `calcular()` | `BusinessException` |
| 4 | Nenhuma tabela ativa no sistema | `calcular()` | `ResourceNotFoundException` |

### Exemplo - Calculo progressivo

```
Entrada: { "categoria": "INDUSTRIAL", "consumo": 18 }

Faixas:
  [0-10]    -> R$ 4,00/m3  =>  10 m3 x R$ 4,00 = R$  40,00
  [11-20]   -> R$ 6,50/m3  =>   8 m3 x R$ 6,50 = R$  52,00

Resultado: valorTotal = R$ 92,00
```

---

## 5. Testes Unitarios - TabelaTarifariaServiceTest

**Arquivo:** `src/test/java/com/gruporas/watertariffapi/service/TabelaTarifariaServiceTest.java`

Testa o CRUD completo do servico de tabelas tarifarias e todas as regras de validacao de faixas de consumo. Utiliza `@Nested` para organizar os testes por funcionalidade.

### 5.1 Testes de Criacao (`criar()`)

| # | Cenario | Resultado Esperado |
|---|---------|-------------------|
| 1 | Dados validos (1 categoria, 4 faixas) | Retorna Response com ID, nome, ativa=true |
| 2 | Verificacao via ArgumentCaptor | Entidade salva com campos corretos |
| 3 | Multiplas categorias (COMERCIAL + INDUSTRIAL) | Response com 2 categorias |

### 5.2 Testes de Validacao de Faixas

| # | Cenario | Entrada Invalida | Excecao |
|---|---------|------------------|---------|
| 4 | Sobreposicao de faixas | `[0-10], [8-20]` | `BusinessException: Sobreposicao detectada` |
| 5 | Lacuna entre faixas | `[0-10], [15-20]` | `BusinessException: Lacuna detectada` |
| 6 | Nao inicia em 0 | `[5-10], [11-20]` | `BusinessException: primeira faixa deve iniciar em 0` |
| 7 | inicio >= fim | `[0-0]` | `BusinessException: inicio deve ser menor que o fim` |
| 8 | Categoria inexistente | `RESIDENCIAL` | `BusinessException: Categoria invalida` |
| 9 | Faixas fora de ordem (validas) | `[21-30], [0-10], [31-99999], [11-20]` | Sucesso (ordena internamente) |

### 5.3 Testes de Listagem (`listarTodas()`)

| # | Cenario | Resultado Esperado |
|---|---------|-------------------|
| 10 | Nenhuma tabela cadastrada | Lista vazia |
| 11 | Duas tabelas cadastradas | Lista com 2 elementos, IDs corretos |

### 5.4 Testes de Busca por ID (`buscarPorId()`)

| # | Cenario | Resultado Esperado |
|---|---------|-------------------|
| 12 | ID existente | Retorna Response com dados corretos |
| 13 | ID inexistente (999) | `ResourceNotFoundException` |

### 5.5 Testes de Exclusao (`excluir()`)

| # | Cenario | Resultado Esperado |
|---|---------|-------------------|
| 14 | ID existente | `repository.delete()` chamado |
| 15 | ID inexistente (999) | `ResourceNotFoundException`, delete nunca chamado |

### 5.6 Testes de Conversao (`toResponse()`)

| # | Cenario | Resultado Esperado |
|---|---------|-------------------|
| 16 | Ordenacao de faixas | Faixas ordenadas por `inicio ASC` |
| 17 | Mapeamento completo | Todos os campos preenchidos (id, nome, data, ativa, categorias, faixas) |

---

## 6. Testes de Integracao - CalculoControllerTest

**Arquivo:** `src/test/java/com/gruporas/watertariffapi/controller/CalculoControllerTest.java`

Testa o endpoint `POST /api/calculos` via MockMvc com `CalculoService` mockado.

| # | Cenario | Request | HTTP Status | Validacao |
|---|---------|---------|-------------|-----------|
| 1 | Calculo valido | `{"categoria": "INDUSTRIAL", "consumo": 18}` | 200 OK | Body com detalhamento |
| 2 | Campos ausentes | `{}` | 400 Bad Request | Erro de validacao |

---

## 7. Testes de Integracao - TabelaTarifariaControllerTest

**Arquivo:** `src/test/java/com/gruporas/watertariffapi/controller/TabelaTarifariaControllerTest.java`

Testa todos os endpoints REST de tabelas tarifarias via MockMvc com `TabelaTarifariaService` mockado. Organizado com `@Nested` por endpoint.

### 7.1 POST /api/tabelas-tarifarias

| # | Cenario | HTTP Status | Validacao |
|---|---------|-------------|-----------|
| 1 | Tabela valida completa | 201 Created | Body com id, nome, categorias, faixas |
| 2 | Nome ausente | 400 Bad Request | Validacao `@NotBlank` |
| 3 | Data de vigencia ausente | 400 Bad Request | Validacao `@NotNull` |
| 4 | Lista de categorias vazia | 400 Bad Request | Validacao `@NotEmpty` |
| 5 | Faixas com sobreposicao | 400 Bad Request | `BusinessException` no body |
| 6 | Body vazio `{}` | 400 Bad Request | Multiplas validacoes |

### 7.2 GET /api/tabelas-tarifarias

| # | Cenario | HTTP Status | Validacao |
|---|---------|-------------|-----------|
| 7 | Lista com dados | 200 OK | Array com elementos, campos corretos |
| 8 | Lista vazia | 200 OK | Array vazio `[]` |

### 7.3 GET /api/tabelas-tarifarias/{id}

| # | Cenario | HTTP Status | Validacao |
|---|---------|-------------|-----------|
| 9 | ID existente | 200 OK | Tabela completa com categorias |
| 10 | ID inexistente (999) | 404 Not Found | Mensagem contendo "999" |

### 7.4 DELETE /api/tabelas-tarifarias/{id}

| # | Cenario | HTTP Status | Validacao |
|---|---------|-------------|-----------|
| 11 | ID existente | 204 No Content | Sem body, `excluir()` chamado |
| 12 | ID inexistente (999) | 404 Not Found | Mensagem contendo "999" |

---

## 8. Testes E2E via cURL

Apos iniciar a aplicacao (`mvn spring-boot:run`), execute os seguintes comandos para validar o fluxo completo:

### 8.1 Criar tabela tarifaria

```bash
curl -s -X POST http://localhost:8080/api/tabelas-tarifarias \
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
  }' | python3 -m json.tool
```

**Esperado:** HTTP 201 com tabela completa.

### 8.2 Listar tabelas

```bash
curl -s http://localhost:8080/api/tabelas-tarifarias | python3 -m json.tool
```

**Esperado:** Array com a tabela criada.

### 8.3 Calcular consumo (INDUSTRIAL, 18 m3)

```bash
curl -s -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{"categoria": "INDUSTRIAL", "consumo": 18}' | python3 -m json.tool
```

**Esperado:**

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

### 8.4 Calcular consumo (COMERCIAL, 35 m3)

```bash
curl -s -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{"categoria": "COMERCIAL", "consumo": 35}' | python3 -m json.tool
```

**Esperado:** `valorTotal = 325.00` com 4 faixas no detalhamento.

### 8.5 Testar validacao - categoria invalida

```bash
curl -s -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{"categoria": "RESIDENCIAL", "consumo": 10}' | python3 -m json.tool
```

**Esperado:** HTTP 400 com mensagem `"Categoria invalida: 'RESIDENCIAL'"`.

### 8.6 Testar validacao - campos ausentes

```bash
curl -s -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{}' | python3 -m json.tool
```

**Esperado:** HTTP 400 com mensagens de campos obrigatorios.

### 8.7 Testar validacao - faixas com sobreposicao

```bash
curl -s -X POST http://localhost:8080/api/tabelas-tarifarias \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Tabela Invalida",
    "dataVigencia": "2025-01-01",
    "categorias": [{
      "categoria": "COMERCIAL",
      "faixas": [
        {"inicio": 0, "fim": 10, "valorUnitario": 1.00},
        {"inicio": 8, "fim": 20, "valorUnitario": 2.00}
      ]
    }]
  }' | python3 -m json.tool
```

**Esperado:** HTTP 400 com mensagem `"Sobreposicao detectada"`.

### 8.8 Testar recurso nao encontrado

```bash
curl -s http://localhost:8080/api/tabelas-tarifarias/99999 | python3 -m json.tool
```

**Esperado:** HTTP 404 com mensagem `"nao encontrada"`.

### 8.9 Excluir tabela

```bash
curl -s -X DELETE http://localhost:8080/api/tabelas-tarifarias/1 -w "\nHTTP Status: %{http_code}\n"
```

**Esperado:** HTTP 204 No Content.

### 8.10 Verificar exclusao

```bash
curl -s http://localhost:8080/api/tabelas-tarifarias | python3 -m json.tool
```

**Esperado:** A tabela excluida nao aparece na lista.

---

## 9. Matriz de Cobertura

### Cobertura por Funcionalidade

| Funcionalidade | Service Test | Controller Test | cURL E2E |
|---|:---:|:---:|:---:|
| Criar tabela valida | OK | OK | OK |
| Criar com multiplas categorias | OK | - | OK |
| Validacao: sobreposicao | OK | OK | OK |
| Validacao: lacuna | OK | - | - |
| Validacao: nao inicia em 0 | OK | - | - |
| Validacao: inicio >= fim | OK | - | - |
| Validacao: categoria invalida | OK | - | OK |
| Validacao: faixas desordenadas | OK | - | - |
| Validacao: nome ausente | - | OK | - |
| Validacao: data ausente | - | OK | - |
| Validacao: categorias vazia | - | OK | - |
| Validacao: body vazio | - | OK | OK |
| Listar tabelas (com dados) | OK | OK | OK |
| Listar tabelas (vazio) | OK | OK | - |
| Buscar por ID existente | OK | OK | - |
| Buscar por ID inexistente | OK | OK | OK |
| Excluir ID existente | OK | OK | OK |
| Excluir ID inexistente | OK | OK | - |
| Conversao faixas ordenadas | OK | - | - |
| Mapeamento completo | OK | - | - |
| Calculo progressivo valido | OK | OK | OK |
| Calculo campos ausentes | - | OK | OK |
| Calculo categoria invalida | OK | - | OK |
| Calculo sem tabela ativa | OK | - | - |

### Cobertura por Camada

| Camada | Componente | Cenarios Cobertos |
|---|---|---|
| **Service** | `TabelaTarifariaService` | CRUD completo + 5 regras de validacao + conversao DTO |
| **Service** | `CalculoService` | Calculo progressivo + erros de categoria/tabela |
| **Controller** | `TabelaTarifariaController` | 4 endpoints (POST, GET, GET/{id}, DELETE) + erros HTTP |
| **Controller** | `CalculoController` | POST /api/calculos + validacao de entrada |
| **E2E** | Fluxo completo | Criar -> Listar -> Calcular -> Excluir -> Verificar |

---

**Observacão:** Todos os 35 testes automatizados podem ser executados sem dependencia de banco de dados externo, pois utilizam mocks na camada de Repository (testes unitarios) e na camada de Service (testes de integracao com MockMvc).
