-- =====================================================================
--  Dados de exemplo — Sistema de Avaliação PRF (Grupo 4)
--  Executado uma vez, logo depois do schema, quando o volume do banco
--  é criado. CPFs fictícios (só dígitos, sem validação de DV).
--  Os IDs gerados começam em 1 e seguem a ordem dos INSERTs abaixo.
-- =====================================================================

BEGIN;

-- ---------- Pessoas + subclasses (mesma transação: gatilho deferido) ----------
INSERT INTO pessoa (cpf, nome_completo, data_nascimento, genero, telefone, tipo) VALUES
    ('11111111111', 'Maria das Graças Silva',    '1952-03-14', 'FEMININO',  '81999990001', 'PACIENTE'),
    ('22222222222', 'José Carlos Albuquerque',   '1948-11-02', 'MASCULINO', '81999990002', 'PACIENTE'),
    ('33333333333', 'Ana Beatriz Lima',          '1985-07-21', 'FEMININO',  '81999990003', 'PROFISSIONAL'),
    ('44444444444', 'Ricardo Menezes Costa',     '1979-01-30', 'MASCULINO', '81999990004', 'PROFISSIONAL'),
    ('55555555555', 'Lucas Ferreira Rocha',      '2003-05-09', 'MASCULINO', '81999990005', 'ESTAGIARIO'),
    ('66666666666', 'Camila Souza Andrade',      '2002-09-17', 'FEMININO',  '81999990006', 'ESTAGIARIO');

INSERT INTO paciente (cpf) VALUES ('11111111111'), ('22222222222');

INSERT INTO profissional_saude (cpf, conselho, numero_registro, especialidade) VALUES
    ('33333333333', 'CRF-PE', '12345', 'Farmácia Clínica'),
    ('44444444444', 'CRM-PE', '12345', 'Geriatria');   -- mesmo número, conselho diferente: permitido

INSERT INTO estagiario (cpf, instituicao_ensino, curso) VALUES
    ('55555555555', 'UFPE', 'Farmácia'),
    ('66666666666', 'UFPE', 'Medicina');

-- ---------- Contatos de emergência (entidade fraca) ----------
INSERT INTO contato_emergencia (cpf_paciente, nome, telefone, relacao) VALUES
    ('11111111111', 'Paulo Silva',        '81988880001', 'Filho'),
    ('11111111111', 'Fernanda Silva',     '81988880002', 'Filha'),
    ('22222222222', 'Helena Albuquerque', '81988880003', 'Esposa');

-- ---------- Medicamentos (ids 1..4) ----------
INSERT INTO medicamento (cpf_paciente, nome, dose, forma, via, frequencia) VALUES
    ('11111111111', 'Losartana',    '50 mg',  'Comprimido', 'Oral', '1x ao dia'),        -- 1
    ('11111111111', 'Metformina',   '850 mg', 'Comprimido', 'Oral', '2x ao dia'),        -- 2
    ('22222222222', 'Omeprazol',    '20 mg',  'Cápsula',    'Oral', '1x ao dia, jejum'), -- 3
    ('22222222222', 'Sinvastatina', '20 mg',  'Comprimido', 'Oral', '1x à noite');       -- 4

-- ---------- Catálogo NESC (ids 1..5) ----------
INSERT INTO opcao_nesc (categoria, subtipo, texto) VALUES
    ('NECESSIDADE', 'OUTRO',        'Medicamento sem indicação clínica atual'),          -- 1
    ('EFETIVIDADE', 'QUANTITATIVO', 'Dose abaixo da necessária para o efeito'),          -- 2
    ('SEGURANCA',   'QUANTITATIVO', 'Dose acima da recomendada'),                         -- 3
    ('SEGURANCA',   'QUALITATIVO',  'Reação adversa ao medicamento'),                     -- 4
    ('CUMPRIMENTO', 'OUTRO',        'Paciente não segue o esquema prescrito');            -- 5

-- ---------- Avaliações (ids 1..3) ----------
-- 1: conduzida por profissional (sem supervisor)
-- 2: conduzida por estagiário, supervisionada por profissional
-- 3: conduzida por estagiário, sem data de retorno
INSERT INTO avaliacao_prf (data_realizacao, data_retorno, cpf_paciente, cpf_profissional, cpf_estagiario, cpf_supervisor) VALUES
    ('2026-10-01', '2026-11-01', '11111111111', '33333333333', NULL,          NULL),          -- 1
    ('2026-10-05', '2026-11-05', '22222222222', NULL,          '55555555555', '33333333333'), -- 2
    ('2026-10-20', NULL,         '11111111111', NULL,          '66666666666', '44444444444'); -- 3

-- ---------- Suspeitas (ids 1..3) — medicamento sempre do mesmo paciente ----------
INSERT INTO suspeita_prf (data_registro, id_avaliacao, id_medicamento, id_opcao_nesc) VALUES
    ('2026-10-01', 1, 2, 5),   -- 1: Maria / Metformina / não cumpre esquema
    ('2026-10-05', 2, 4, 4),   -- 2: José  / Sinvastatina / reação adversa
    ('2026-10-20', 3, 1, 2);   -- 3: Maria / Losartana / dose baixa

-- ---------- Intervenções ----------
INSERT INTO intervencao (id_suspeita, tipo, descricao) VALUES
    (1, 'ORIENTACAO_MEDICAMENTO', 'Orientada sobre horários da metformina junto às refeições.'),
    (1, 'MEDIDA_NAO_FARMACOLOGICA', 'Entregue tabela de horários impressa e sugerido alarme no celular.'),
    (2, 'ENCAMINHAMENTO', 'Encaminhado ao médico assistente para avaliar troca da estatina.'),
    (3, 'ENCAMINHAMENTO', 'Encaminhada ao cardiologista para reavaliação da dose de losartana.');

-- ---------- Consultas ----------
INSERT INTO consulta (data_agendada, tipo, cpf_paciente, cpf_profissional, cpf_estagiario, id_avaliacao_origem) VALUES
    ('2026-09-25 09:00', 'PRIMEIRA', '11111111111', '33333333333', NULL,          NULL), -- 1
    ('2026-09-30 14:00', 'PRIMEIRA', '22222222222', NULL,          '55555555555', NULL), -- 2
    ('2026-11-01 09:00', 'RETORNO',  '11111111111', '33333333333', NULL,          1),    -- 3: retorno da avaliação 1
    ('2026-11-05 14:00', 'RETORNO',  '22222222222', NULL,          '55555555555', 2);    -- 4: retorno da avaliação 2

COMMIT;
