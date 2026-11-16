-- =====================================================================
--  Sistema de Avaliação PRF — Grupo 4
--  Etapa 3: esquema físico (PostgreSQL 16)
--
--  Implementa o mapeamento relacional da Etapa 2:
--    11 tabelas · 17 chaves estrangeiras · 5 UNIQUE · 15 CHECK · 3 gatilhos
--
--  Regras gerais de integridade referencial (Etapa 2):
--    * Todas as FKs: ON UPDATE CASCADE (permite corrigir um CPF digitado errado).
--    * ON DELETE CASCADE só onde há dependência de existência
--      (subclasses -> pessoa e contato_emergencia -> paciente).
--    * Nas demais: ON DELETE RESTRICT (preserva o histórico clínico).
-- =====================================================================

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
SET search_path TO public;

-- ---------------------------------------------------------------------
-- 1. PESSOA (superclasse) — especialização opção 8A
-- ---------------------------------------------------------------------
CREATE TABLE pessoa (
    cpf              CHAR(11)     NOT NULL,
    nome_completo    VARCHAR(150) NOT NULL,
    data_nascimento  DATE         NOT NULL,
    genero           VARCHAR(20)  NOT NULL,
    telefone         VARCHAR(20)  NOT NULL,
    tipo             VARCHAR(12)  NOT NULL,

    CONSTRAINT pk_pessoa            PRIMARY KEY (cpf),
    -- alvo das FKs compostas (cpf, tipo) das subclasses -> garante disjunção
    CONSTRAINT uq_pessoa_cpf_tipo   UNIQUE (cpf, tipo),
    CONSTRAINT ck_pessoa_cpf        CHECK (cpf ~ '^[0-9]{11}$'),
    CONSTRAINT ck_pessoa_tipo       CHECK (tipo IN ('PACIENTE', 'PROFISSIONAL', 'ESTAGIARIO')),
    CONSTRAINT ck_pessoa_genero     CHECK (genero IN ('MASCULINO', 'FEMININO', 'OUTRO'))
);

-- ---------------------------------------------------------------------
-- 2. PACIENTE (subclasse)
-- ---------------------------------------------------------------------
CREATE TABLE paciente (
    cpf   CHAR(11)    NOT NULL,
    tipo  VARCHAR(12) NOT NULL DEFAULT 'PACIENTE',

    CONSTRAINT pk_paciente       PRIMARY KEY (cpf),
    CONSTRAINT ck_paciente_tipo  CHECK (tipo = 'PACIENTE'),
    CONSTRAINT fk_paciente_pessoa FOREIGN KEY (cpf, tipo)
        REFERENCES pessoa (cpf, tipo) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 3. PROFISSIONAL_SAUDE (subclasse)
-- ---------------------------------------------------------------------
CREATE TABLE profissional_saude (
    cpf              CHAR(11)     NOT NULL,
    tipo             VARCHAR(12)  NOT NULL DEFAULT 'PROFISSIONAL',
    conselho         VARCHAR(20)  NOT NULL,
    numero_registro  VARCHAR(20)  NOT NULL,
    especialidade    VARCHAR(100) NOT NULL,

    CONSTRAINT pk_profissional_saude      PRIMARY KEY (cpf),
    -- o número é atribuído por cada conselho: unicidade vale para o par
    CONSTRAINT uq_profissional_registro   UNIQUE (conselho, numero_registro),
    CONSTRAINT ck_profissional_tipo       CHECK (tipo = 'PROFISSIONAL'),
    CONSTRAINT fk_profissional_pessoa FOREIGN KEY (cpf, tipo)
        REFERENCES pessoa (cpf, tipo) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 4. ESTAGIARIO (subclasse)
-- ---------------------------------------------------------------------
CREATE TABLE estagiario (
    cpf                CHAR(11)     NOT NULL,
    tipo               VARCHAR(12)  NOT NULL DEFAULT 'ESTAGIARIO',
    instituicao_ensino VARCHAR(150) NOT NULL,
    curso              VARCHAR(100) NOT NULL,

    CONSTRAINT pk_estagiario       PRIMARY KEY (cpf),
    CONSTRAINT ck_estagiario_tipo  CHECK (tipo = 'ESTAGIARIO'),
    CONSTRAINT fk_estagiario_pessoa FOREIGN KEY (cpf, tipo)
        REFERENCES pessoa (cpf, tipo) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 5. CONTATO_EMERGENCIA (entidade fraca)
--    PK = chave do dono (cpf_paciente) + chave parcial (nome)
-- ---------------------------------------------------------------------
CREATE TABLE contato_emergencia (
    cpf_paciente  CHAR(11)     NOT NULL,
    nome          VARCHAR(150) NOT NULL,
    telefone      VARCHAR(20)  NOT NULL,
    relacao       VARCHAR(50)  NOT NULL,

    CONSTRAINT pk_contato_emergencia PRIMARY KEY (cpf_paciente, nome),
    CONSTRAINT fk_contato_paciente FOREIGN KEY (cpf_paciente)
        REFERENCES paciente (cpf) ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 6. MEDICAMENTO (relacionamento "usa", 1:N com paciente)
-- ---------------------------------------------------------------------
CREATE TABLE medicamento (
    id_medicamento  INTEGER GENERATED ALWAYS AS IDENTITY,
    cpf_paciente    CHAR(11)     NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    dose            VARCHAR(50)  NOT NULL,
    forma           VARCHAR(50)  NOT NULL,
    via             VARCHAR(50)  NOT NULL,
    frequencia      VARCHAR(100) NOT NULL,

    CONSTRAINT pk_medicamento PRIMARY KEY (id_medicamento),
    CONSTRAINT fk_medicamento_paciente FOREIGN KEY (cpf_paciente)
        REFERENCES paciente (cpf) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 7. OPCAO_NESC (catálogo / tabela de domínio)
-- ---------------------------------------------------------------------
CREATE TABLE opcao_nesc (
    id_opcao_nesc  INTEGER GENERATED ALWAYS AS IDENTITY,
    categoria      VARCHAR(15)  NOT NULL,
    subtipo        VARCHAR(15)  NOT NULL,
    texto          VARCHAR(255) NOT NULL,

    CONSTRAINT pk_opcao_nesc          PRIMARY KEY (id_opcao_nesc),
    CONSTRAINT uq_opcao_nesc          UNIQUE (categoria, subtipo, texto),
    CONSTRAINT ck_opcao_nesc_categoria CHECK (categoria IN ('NECESSIDADE', 'EFETIVIDADE', 'SEGURANCA', 'CUMPRIMENTO')),
    CONSTRAINT ck_opcao_nesc_subtipo   CHECK (subtipo IN ('QUANTITATIVO', 'QUALITATIVO', 'OUTRO'))
);

-- ---------------------------------------------------------------------
-- 8. AVALIACAO_PRF
--    recebe (paciente), conduz_prof, conduz_estag, supervisiona
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao_prf (
    id_avaliacao      INTEGER GENERATED ALWAYS AS IDENTITY,
    data_realizacao   DATE     NOT NULL,
    data_retorno      DATE,
    cpf_paciente      CHAR(11) NOT NULL,
    cpf_profissional  CHAR(11),
    cpf_estagiario    CHAR(11),
    cpf_supervisor    CHAR(11),

    CONSTRAINT pk_avaliacao_prf          PRIMARY KEY (id_avaliacao),
    -- alvo da FK composta de consulta (retorno do MESMO paciente)
    CONSTRAINT uq_avaliacao_id_paciente  UNIQUE (id_avaliacao, cpf_paciente),

    -- exatamente um executor: profissional OU estagiário
    CONSTRAINT ck_avaliacao_executor CHECK (
        (cpf_profissional IS NOT NULL AND cpf_estagiario IS NULL) OR
        (cpf_profissional IS NULL     AND cpf_estagiario IS NOT NULL)
    ),
    -- supervisor presente se, e somente se, a condução for por estagiário
    CONSTRAINT ck_avaliacao_supervisor CHECK (
        (cpf_estagiario IS NOT NULL) = (cpf_supervisor IS NOT NULL)
    ),
    CONSTRAINT ck_avaliacao_datas CHECK (
        data_retorno IS NULL OR data_retorno > data_realizacao
    ),

    CONSTRAINT fk_avaliacao_paciente FOREIGN KEY (cpf_paciente)
        REFERENCES paciente (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_avaliacao_profissional FOREIGN KEY (cpf_profissional)
        REFERENCES profissional_saude (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_avaliacao_estagiario FOREIGN KEY (cpf_estagiario)
        REFERENCES estagiario (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_avaliacao_supervisor FOREIGN KEY (cpf_supervisor)
        REFERENCES profissional_saude (cpf) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 9. SUSPEITA_PRF  (identifica, alvo, classificadaPor)
-- ---------------------------------------------------------------------
CREATE TABLE suspeita_prf (
    id_suspeita     INTEGER GENERATED ALWAYS AS IDENTITY,
    data_registro   DATE    NOT NULL DEFAULT CURRENT_DATE,
    id_avaliacao    INTEGER NOT NULL,
    id_medicamento  INTEGER NOT NULL,
    id_opcao_nesc   INTEGER NOT NULL,

    CONSTRAINT pk_suspeita_prf PRIMARY KEY (id_suspeita),
    CONSTRAINT fk_suspeita_avaliacao FOREIGN KEY (id_avaliacao)
        REFERENCES avaliacao_prf (id_avaliacao) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suspeita_medicamento FOREIGN KEY (id_medicamento)
        REFERENCES medicamento (id_medicamento) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suspeita_opcao_nesc FOREIGN KEY (id_opcao_nesc)
        REFERENCES opcao_nesc (id_opcao_nesc) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 10. INTERVENCAO (gera, 1:N com suspeita)
-- ---------------------------------------------------------------------
CREATE TABLE intervencao (
    id_intervencao  INTEGER GENERATED ALWAYS AS IDENTITY,
    id_suspeita     INTEGER     NOT NULL,
    tipo            VARCHAR(30) NOT NULL,
    descricao       TEXT        NOT NULL,

    CONSTRAINT pk_intervencao PRIMARY KEY (id_intervencao),
    CONSTRAINT ck_intervencao_tipo CHECK (tipo IN (
        'ENCAMINHAMENTO', 'PRESCRICAO', 'ORIENTACAO_MEDICAMENTO',
        'ORIENTACAO_DOENCA', 'MEDIDA_NAO_FARMACOLOGICA')),
    CONSTRAINT fk_intervencao_suspeita FOREIGN KEY (id_suspeita)
        REFERENCES suspeita_prf (id_suspeita) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 11. CONSULTA — especialização opção 8C (tabela única + discriminador)
-- ---------------------------------------------------------------------
CREATE TABLE consulta (
    id_consulta          INTEGER GENERATED ALWAYS AS IDENTITY,
    data_agendada        TIMESTAMP   NOT NULL,
    tipo                 VARCHAR(10) NOT NULL,
    cpf_paciente         CHAR(11)    NOT NULL,
    cpf_profissional     CHAR(11),
    cpf_estagiario       CHAR(11),
    id_avaliacao_origem  INTEGER,

    CONSTRAINT pk_consulta              PRIMARY KEY (id_consulta),
    -- origina adotada como 1:1 na Etapa 2
    CONSTRAINT uq_consulta_origem       UNIQUE (id_avaliacao_origem),
    CONSTRAINT ck_consulta_tipo         CHECK (tipo IN ('PRIMEIRA', 'RETORNO')),
    CONSTRAINT ck_consulta_responsavel  CHECK (
        (cpf_profissional IS NOT NULL AND cpf_estagiario IS NULL) OR
        (cpf_profissional IS NULL     AND cpf_estagiario IS NOT NULL)
    ),
    -- RETORNO se, e somente se, houver avaliação de origem
    CONSTRAINT ck_consulta_retorno_origem CHECK (
        (tipo = 'RETORNO') = (id_avaliacao_origem IS NOT NULL)
    ),

    CONSTRAINT fk_consulta_paciente FOREIGN KEY (cpf_paciente)
        REFERENCES paciente (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_consulta_profissional FOREIGN KEY (cpf_profissional)
        REFERENCES profissional_saude (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_consulta_estagiario FOREIGN KEY (cpf_estagiario)
        REFERENCES estagiario (cpf) ON UPDATE CASCADE ON DELETE RESTRICT,
    -- FK composta: o retorno é do mesmo paciente da avaliação que o originou
    CONSTRAINT fk_consulta_avaliacao_origem FOREIGN KEY (id_avaliacao_origem, cpf_paciente)
        REFERENCES avaliacao_prf (id_avaliacao, cpf_paciente) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================================
-- GATILHOS
-- =====================================================================

-- ---------------------------------------------------------------------
-- Gatilhos 1 e 2 — especialização TOTAL de pessoa.
-- Toda pessoa precisa existir na subclasse indicada pelo seu tipo.
-- São CONSTRAINT TRIGGERs DEFERRABLE INITIALLY DEFERRED: a checagem só
-- acontece no COMMIT, o que permite inserir pessoa e subclasse na mesma
-- transação (é exatamente o que o Hibernate faz na herança JOINED).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_verifica_especializacao_total()
RETURNS TRIGGER AS $$
DECLARE
    v_cpf  CHAR(11);
    v_tipo VARCHAR(12);
    v_ok   BOOLEAN;
BEGIN
    IF TG_TABLE_NAME = 'pessoa' THEN
        v_cpf := NEW.cpf;
    ELSE
        v_cpf := OLD.cpf;   -- remoção/alteração em uma subclasse
    END IF;

    SELECT tipo INTO v_tipo FROM pessoa WHERE cpf = v_cpf;
    IF NOT FOUND THEN
        RETURN NULL;        -- pessoa já removida (ex.: DELETE em cascata)
    END IF;

    v_ok := CASE v_tipo
        WHEN 'PACIENTE'     THEN EXISTS (SELECT 1 FROM paciente           WHERE cpf = v_cpf)
        WHEN 'PROFISSIONAL' THEN EXISTS (SELECT 1 FROM profissional_saude WHERE cpf = v_cpf)
        WHEN 'ESTAGIARIO'   THEN EXISTS (SELECT 1 FROM estagiario         WHERE cpf = v_cpf)
        ELSE FALSE
    END;

    IF NOT v_ok THEN
        RAISE EXCEPTION 'Especialização total violada: a pessoa % (tipo %) não está cadastrada na subclasse correspondente.', v_cpf, v_tipo
            USING ERRCODE = 'check_violation', CONSTRAINT = 'tg_especializacao_total';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Gatilho 1: ao inserir/alterar uma pessoa
CREATE CONSTRAINT TRIGGER tg_pessoa_especializacao_total
    AFTER INSERT OR UPDATE ON pessoa
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verifica_especializacao_total();

-- Gatilho 2: ao remover/alterar uma linha de subclasse (a pessoa não pode
-- ficar "órfã" de especialização). A mesma regra é ligada às três subclasses.
CREATE CONSTRAINT TRIGGER tg_paciente_especializacao_total
    AFTER UPDATE OR DELETE ON paciente
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verifica_especializacao_total();

CREATE CONSTRAINT TRIGGER tg_profissional_especializacao_total
    AFTER UPDATE OR DELETE ON profissional_saude
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verifica_especializacao_total();

CREATE CONSTRAINT TRIGGER tg_estagiario_especializacao_total
    AFTER UPDATE OR DELETE ON estagiario
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION fn_verifica_especializacao_total();

-- ---------------------------------------------------------------------
-- Gatilho 3 — o medicamento da suspeita deve pertencer ao mesmo paciente
-- da avaliação (as duas FKs independentes não garantem isso sozinhas).
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_suspeita_medicamento_mesmo_paciente()
RETURNS TRIGGER AS $$
DECLARE
    v_paciente_avaliacao   CHAR(11);
    v_paciente_medicamento CHAR(11);
BEGIN
    SELECT cpf_paciente INTO v_paciente_avaliacao
      FROM avaliacao_prf WHERE id_avaliacao = NEW.id_avaliacao;
    SELECT cpf_paciente INTO v_paciente_medicamento
      FROM medicamento WHERE id_medicamento = NEW.id_medicamento;

    -- se algum não existir, a própria FK acusará o erro
    IF v_paciente_avaliacao IS NOT NULL AND v_paciente_medicamento IS NOT NULL
       AND v_paciente_avaliacao <> v_paciente_medicamento THEN
        RAISE EXCEPTION 'O medicamento % pertence ao paciente %, mas a avaliação % é do paciente %.',
            NEW.id_medicamento, v_paciente_medicamento, NEW.id_avaliacao, v_paciente_avaliacao
            USING ERRCODE = 'check_violation', CONSTRAINT = 'tg_suspeita_medicamento_paciente';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_suspeita_medicamento_paciente
    BEFORE INSERT OR UPDATE OF id_avaliacao, id_medicamento ON suspeita_prf
    FOR EACH ROW EXECUTE FUNCTION fn_suspeita_medicamento_mesmo_paciente();

-- =====================================================================
-- ÍNDICES (FKs e datas mais consultadas)
-- PostgreSQL não cria índice automático em coluna de FK.
-- =====================================================================
CREATE INDEX ix_medicamento_paciente     ON medicamento   (cpf_paciente);
CREATE INDEX ix_avaliacao_paciente       ON avaliacao_prf (cpf_paciente);
CREATE INDEX ix_avaliacao_profissional   ON avaliacao_prf (cpf_profissional);
CREATE INDEX ix_avaliacao_estagiario     ON avaliacao_prf (cpf_estagiario);
CREATE INDEX ix_avaliacao_supervisor     ON avaliacao_prf (cpf_supervisor);
CREATE INDEX ix_avaliacao_data           ON avaliacao_prf (data_realizacao);
CREATE INDEX ix_suspeita_avaliacao       ON suspeita_prf  (id_avaliacao);
CREATE INDEX ix_suspeita_medicamento     ON suspeita_prf  (id_medicamento);
CREATE INDEX ix_suspeita_opcao_nesc      ON suspeita_prf  (id_opcao_nesc);
CREATE INDEX ix_intervencao_suspeita     ON intervencao   (id_suspeita);
CREATE INDEX ix_consulta_paciente        ON consulta      (cpf_paciente);
CREATE INDEX ix_consulta_profissional    ON consulta      (cpf_profissional);
CREATE INDEX ix_consulta_estagiario      ON consulta      (cpf_estagiario);
CREATE INDEX ix_consulta_data            ON consulta      (data_agendada);
