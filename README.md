# Sistema de Avaliação PRF — Backend (Etapa 3)

Projeto de Banco de Dados — CIn/UFPE — **Grupo 4**

Davi Rosendo Carvalho · Beatriz Freitas Souza Pedrosa · Beatriz Pandolfi Maroja · Luísa Bispo Lócio ·
João Luís de Siqueira Ribeiro · João Antônio Lins Carvalho de Aguiar · Marina Cabral Nogueira Lima ·
Sofia Avallone Sakovitz

Este backend implementa o modelo relacional da Etapa 2. Ele tem endpoints para **criar, ler, atualizar e
excluir** registros das 11 tabelas do minimundo de avaliação de Problemas Relacionados à Farmacoterapia (PRF).

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 (Spring Web) |
| ORM | Spring Data JPA + Hibernate 6 |
| Banco | PostgreSQL 16 |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Execução | Docker Compose |

---

## Como executar

Pré-requisito: Docker com o plugin Compose (Docker Desktop já vem com ele).

```bash
docker compose up --build
```

O primeiro build baixa as dependências do Maven e pode levar alguns minutos. Quando aparecer
`Started PrfApplication` no log, a aplicação está no ar:

| O quê | Endereço |
|---|---|
| API | http://localhost:8080/api |
| Swagger UI (documentação interativa, permite testar no navegador) | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| PostgreSQL (DBeaver, pgAdmin, psql) | `localhost:5433` · banco `prf` · usuário `prf` · senha `prf` |

> O banco fica exposto na porta **5433** para não conflitar com um PostgreSQL já instalado na máquina.

**Recriar o banco do zero** (útil antes de rodar os exemplos de requisição):

```bash
docker compose down -v      # o -v apaga o volume com os dados
docker compose up --build
```

Os scripts de `db/` só rodam quando o volume é criado. Por isso, alterações no schema exigem o `down -v`.

### Rodando o backend fora do Docker (desenvolvimento)

```bash
docker compose up -d db                 # sobe só o banco
cd backend && mvn spring-boot:run       # usa localhost:5433 por padrão (ver application.yml)
```

---

## Estrutura do projeto

```
projeto-bd-grupo4/
├── docker-compose.yml        # serviços db (PostgreSQL) e api (Spring Boot)
├── README.md
├── db/                       # executados pelo PostgreSQL na criação do volume, em ordem
│   ├── 01-schema.sql         # 11 tabelas, 17 FKs, 5 UNIQUE, 15 CHECK, gatilhos e índices
│   └── 02-seed.sql           # dados de exemplo
├── requisicoes/
│   ├── requests.http                         # 75 exemplos (VS Code REST Client / IntelliJ)
│   └── PRF-Grupo4.postman_collection.json    # os mesmos exemplos para o Postman
└── backend/
    ├── Dockerfile            # build em duas etapas: Maven compila, JRE executa
    ├── pom.xml
    └── src/main/
        ├── resources/application.yml
        └── java/br/ufpe/cin/prf/
            ├── PrfApplication.java       # ponto de entrada
            ├── config/                   # CORS (para o front da Etapa 4) e OpenAPI
            ├── entity/                   # mapeamento ORM: uma classe por tabela + enums dos CHECKs
            ├── repository/               # Spring Data JPA: acesso ao banco e consultas derivadas
            ├── dto/                      # corpos de requisição (com validação) e de resposta
            ├── service/                  # regras do minimundo e transações
            ├── controller/               # endpoints REST
            └── exception/                # erros padronizados (404, 409, 422...)
```

### Fluxo de uma requisição

1. O **controller** recebe o JSON, que o Bean Validation confere (`@Valid` no DTO). Campo faltando ou formato errado gera **400**.
2. O **service** abre a transação, confere as regras do minimundo e resolve as chaves estrangeiras. Regra violada gera **422**; duplicidade gera **409**.
3. O **repository** (Spring Data JPA) e o **Hibernate** geram o SQL.
4. O **PostgreSQL** é a última barreira: CHECK, FK, UNIQUE e gatilhos. Se algo escapar das camadas anteriores, o `GlobalExceptionHandler` converte o erro do banco em 409/422 com o nome da restrição violada, em vez de um erro 500.

As regras ficam nas duas camadas de propósito. O service dá mensagens claras para quem usa a API, e o
banco garante a integridade mesmo para quem acessa as tabelas por fora da aplicação.

---

## Do modelo relacional ao código (decisões de mapeamento ORM)

| Decisão da Etapa 2 | Banco (`01-schema.sql`) | ORM (pacote `entity`) |
|---|---|---|
| Especialização de pessoa, **opção 8A** (uma tabela por classe) | `pessoa` + `paciente`, `profissional_saude`, `estagiario`, com PK = FK `cpf` | Herança `@Inheritance(JOINED)` com `@DiscriminatorColumn("tipo")`: salvar um `Paciente` insere em `pessoa` **e** em `paciente` na mesma transação |
| Especialização **disjunta** | FK composta `(cpf, tipo)` + `CHECK (tipo = '...')` em cada subclasse | O Hibernate grava o `@DiscriminatorValue`; a coluna `tipo` da subclasse usa o `DEFAULT` do banco |
| Especialização **total** | Gatilhos `DEFERRABLE INITIALLY DEFERRED`, checados no `COMMIT` | Não há endpoint que crie uma pessoa sem subclasse; o cadastro é sempre por `/pacientes`, `/profissionais` ou `/estagiarios` |
| Entidade fraca `contato_emergencia` | PK composta `(cpf_paciente, nome)`, `ON DELETE CASCADE` | `@EmbeddedId ContatoEmergenciaId` + `@MapsId`; endpoints aninhados em `/pacientes/{cpf}/contatos` |
| Especialização de consulta, **opção 8C** (tabela única) | Coluna discriminadora `tipo` + `CHECK` (RETORNO ⇔ origem) | Atributo `enum TipoConsulta`, sem herança JPA, já que as subclasses têm os mesmos atributos |
| Relacionamentos 1:N | FK no lado N | `@ManyToOne(fetch = LAZY)` |
| Retorno do mesmo paciente (`origina`, 1:1) | FK composta `(id_avaliacao_origem, cpf_paciente)` + `UNIQUE` | `@ManyToOne` pela coluna do id; a regra do mesmo paciente também é conferida no `ConsultaService` |
| Chaves substitutas | `INTEGER GENERATED ALWAYS AS IDENTITY` | `@GeneratedValue(strategy = IDENTITY)` |
| Domínios fixos (categoria NESC, tipo de intervenção etc.) | `CHECK (coluna IN (...))` | `enum` Java + `@Enumerated(STRING)`: valor fora da lista dá **400** antes de chegar ao banco |
| Corrigir CPF | Todas as FKs com `ON UPDATE CASCADE` | `PATCH /api/pessoas/{cpf}/cpf` executa um `UPDATE` nativo e o banco propaga para todas as tabelas |

**O Hibernate não cria tabelas** (`ddl-auto: none`). O `01-schema.sql` é a fonte da verdade, porque contém
CHECKs, gatilhos e FKs compostas que o Hibernate não gera.

### Gatilhos

Os 3 gatilhos da Etapa 2 viram 5 objetos `TRIGGER` no PostgreSQL, porque a regra de especialização total
precisa vigiar tanto `pessoa` quanto as três subclasses:

| Regra | Função | Ligada a |
|---|---|---|
| 1. Toda pessoa nova/alterada precisa ter linha na subclasse do seu tipo | `fn_verifica_especializacao_total` | `pessoa` (AFTER INSERT/UPDATE, deferido) |
| 2. Remover a linha da subclasse não pode deixar a pessoa sem especialização | `fn_verifica_especializacao_total` | `paciente`, `profissional_saude`, `estagiario` (AFTER UPDATE/DELETE, deferido) |
| 3. O medicamento da suspeita deve ser do mesmo paciente da avaliação | `fn_suspeita_medicamento_mesmo_paciente` | `suspeita_prf` (BEFORE INSERT/UPDATE) |

---

## Endpoints

Todos sob `/api`. Corpos e respostas em JSON. Datas em `AAAA-MM-DD`; data e hora em `AAAA-MM-DDTHH:MM`.

| Recurso | Endpoints | Filtros no GET da lista |
|---|---|---|
| Pessoas (superclasse) | `GET /pessoas` · `GET /pessoas/{cpf}` · `PATCH /pessoas/{cpf}/cpf` | — |
| Pacientes | `GET` `POST /pacientes` · `GET` `PUT` `DELETE /pacientes/{cpf}` | — |
| Contatos de emergência | `GET` `POST /pacientes/{cpf}/contatos` · `GET` `PUT` `DELETE /pacientes/{cpf}/contatos/{nome}` | — |
| Profissionais de saúde | `GET` `POST /profissionais` · `GET` `PUT` `DELETE /profissionais/{cpf}` | — |
| Estagiários | `GET` `POST /estagiarios` · `GET` `PUT` `DELETE /estagiarios/{cpf}` | — |
| Catálogo NESC | `GET` `POST /opcoes-nesc` · `GET` `PUT` `DELETE /opcoes-nesc/{id}` | `categoria` |
| Medicamentos | `GET` `POST /medicamentos` · `GET` `PUT` `DELETE /medicamentos/{id}` | `cpfPaciente` |
| Avaliações PRF | `GET` `POST /avaliacoes` · `GET` `PUT` `DELETE /avaliacoes/{id}` | `cpfPaciente` |
| Suspeitas PRF | `GET` `POST /suspeitas` · `GET` `PUT` `DELETE /suspeitas/{id}` | `idAvaliacao` |
| Intervenções | `GET` `POST /intervencoes` · `GET` `PUT` `DELETE /intervencoes/{id}` | `idSuspeita` |
| Consultas (agenda) | `GET` `POST /consultas` · `GET` `PUT` `DELETE /consultas/{id}` | `cpfPaciente`, ou `inicio` e `fim` |

### Códigos de resposta

| Código | Quando |
|---|---|
| 200 / 201 / 204 | Sucesso (201 no POST, com cabeçalho `Location`; 204 no DELETE) |
| 400 | JSON malformado, campo obrigatório ausente, formato inválido (ex.: CPF com pontos) ou valor fora de uma lista fixa |
| 404 | O recurso da URL não existe |
| 409 | Chave duplicada (CPF, registro profissional, contato, opção NESC) ou exclusão bloqueada por `ON DELETE RESTRICT` |
| 422 | Regra do minimundo violada (ex.: estagiário sem supervisor, retorno sem avaliação de origem, medicamento de outro paciente) |

Formato de erro:

```json
{
  "timestamp": "2026-10-28T15:40:12.345-03:00",
  "status": 422,
  "erro": "Unprocessable Entity",
  "mensagem": "Avaliação conduzida por estagiário exige um profissional supervisor (cpfSupervisor).",
  "detalhes": []
}
```

---

## Exemplos de requisição

A pasta `requisicoes/` tem **75 requisições em 13 grupos**, cobrindo o CRUD de todas as tabelas e os casos
de erro que demonstram cada restrição:

- `requests.http`: abra no VS Code (extensão **REST Client**) ou no IntelliJ e clique em *Send Request*.
- `PRF-Grupo4.postman_collection.json`: no Postman, use *Import* e escolha o arquivo.

Rode de cima para baixo num banco recém-criado (`docker compose down -v` e depois `up`). Os ids citados nos
exemplos (medicamento 5, avaliação 4, consulta 6...) contam com os dados iniciais do `02-seed.sql`.

Exemplo com `curl`:

```bash
curl -X POST http://localhost:8080/api/avaliacoes \
  -H "Content-Type: application/json" \
  -d '{"dataRealizacao":"2026-10-29","cpfPaciente":"11111111111","cpfEstagiario":"55555555555","cpfSupervisor":"33333333333"}'
```

### Dados iniciais (`02-seed.sql`)

| CPF | Nome | Tipo |
|---|---|---|
| 11111111111 | Maria das Graças Silva | Paciente (2 contatos, 2 medicamentos, 2 avaliações) |
| 22222222222 | José Carlos Albuquerque | Paciente (1 contato, 2 medicamentos, 1 avaliação) |
| 33333333333 | Ana Beatriz Lima | Profissional (CRF-PE 12345) |
| 44444444444 | Ricardo Menezes Costa | Profissional (CRM-PE 12345: mesmo número, outro conselho) |
| 55555555555 | Lucas Ferreira Rocha | Estagiário (Farmácia/UFPE) |
| 66666666666 | Camila Souza Andrade | Estagiário (Medicina/UFPE) |

Além deles: 5 opções NESC, 3 avaliações (uma por profissional e duas por estagiário com supervisor),
3 suspeitas, 4 intervenções e 4 consultas (2 primeiras e 2 retornos). CPFs fictícios, sem validação de dígito
verificador.
