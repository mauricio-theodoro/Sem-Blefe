CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    email_normalizado VARCHAR(254) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    situacao VARCHAR(30) NOT NULL DEFAULT 'PENDENTE_EMAIL',
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    falhas_login SMALLINT NOT NULL DEFAULT 0,
    bloqueado_ate TIMESTAMPTZ,
    ultimo_login_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    versao BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_usuarios_email_normalizado UNIQUE (email_normalizado),
    CONSTRAINT ck_usuarios_email_normalizado CHECK (
        email_normalizado = LOWER(BTRIM(email_normalizado))
        AND CHAR_LENGTH(email_normalizado) BETWEEN 3 AND 254
    ),
    CONSTRAINT ck_usuarios_situacao CHECK (
        situacao IN ('PENDENTE_EMAIL', 'ATIVO', 'SUSPENSO', 'BLOQUEADO', 'EXCLUSAO_SOLICITADA')
    ),
    CONSTRAINT ck_usuarios_falhas_login CHECK (falhas_login BETWEEN 0 AND 100)
);

CREATE TABLE usuario_papeis (
    usuario_id UUID NOT NULL,
    papel VARCHAR(30) NOT NULL,
    atribuido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atribuido_por UUID,

    CONSTRAINT pk_usuario_papeis PRIMARY KEY (usuario_id, papel),
    CONSTRAINT fk_usuario_papeis_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_usuario_papeis_atribuido_por FOREIGN KEY (atribuido_por) REFERENCES usuarios (id),
    CONSTRAINT ck_usuario_papeis_papel CHECK (papel IN ('USUARIO', 'MODERADOR', 'ADMIN'))
);

CREATE TABLE especialidades (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(240) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    ordem_exibicao SMALLINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_especialidades_codigo UNIQUE (codigo),
    CONSTRAINT ck_especialidades_codigo CHECK (codigo ~ '^[A-Z0-9_]{2,50}$'),
    CONSTRAINT ck_especialidades_ordem CHECK (ordem_exibicao >= 0)
);

CREATE TABLE perfis (
    id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    proprietario_usuario_id UUID,
    slug VARCHAR(30) NOT NULL,
    nome_exibicao VARCHAR(100) NOT NULL,
    bio VARCHAR(1000),
    avatar_chave VARCHAR(500),
    verificado BOOLEAN NOT NULL DEFAULT FALSE,
    situacao VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    versao BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uk_perfis_slug UNIQUE (slug),
    CONSTRAINT uk_perfis_proprietario_pessoal UNIQUE (proprietario_usuario_id),
    CONSTRAINT fk_perfis_proprietario FOREIGN KEY (proprietario_usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_perfis_tipo CHECK (tipo IN ('PESSOA', 'ORGANIZACAO')),
    CONSTRAINT ck_perfis_proprietario CHECK (
        (tipo = 'PESSOA' AND proprietario_usuario_id IS NOT NULL)
        OR (tipo = 'ORGANIZACAO' AND proprietario_usuario_id IS NULL)
    ),
    CONSTRAINT ck_perfis_slug CHECK (
        slug = LOWER(slug)
        AND slug ~ '^[a-z0-9][a-z0-9._-]{1,28}[a-z0-9]$'
    ),
    CONSTRAINT ck_perfis_nome CHECK (CHAR_LENGTH(BTRIM(nome_exibicao)) BETWEEN 2 AND 100),
    CONSTRAINT ck_perfis_situacao CHECK (situacao IN ('RASCUNHO', 'ATIVO', 'SUSPENSO', 'REMOVIDO'))
);

CREATE TABLE perfil_membros (
    perfil_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    papel VARCHAR(20) NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_perfil_membros PRIMARY KEY (perfil_id, usuario_id),
    CONSTRAINT fk_perfil_membros_perfil FOREIGN KEY (perfil_id) REFERENCES perfis (id),
    CONSTRAINT fk_perfil_membros_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_perfil_membros_papel CHECK (papel IN ('PROPRIETARIO', 'GESTOR', 'EDITOR')),
    CONSTRAINT ck_perfil_membros_situacao CHECK (situacao IN ('CONVIDADO', 'ATIVO', 'REVOGADO'))
);

CREATE TABLE perfil_especialidades (
    perfil_id UUID NOT NULL,
    especialidade_id BIGINT NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_perfil_especialidades PRIMARY KEY (perfil_id, especialidade_id),
    CONSTRAINT fk_perfil_especialidades_perfil FOREIGN KEY (perfil_id) REFERENCES perfis (id),
    CONSTRAINT fk_perfil_especialidades_especialidade FOREIGN KEY (especialidade_id) REFERENCES especialidades (id)
);

CREATE UNIQUE INDEX uk_perfil_especialidade_principal
    ON perfil_especialidades (perfil_id)
    WHERE principal = TRUE;

CREATE INDEX ix_perfil_especialidades_busca
    ON perfil_especialidades (especialidade_id, perfil_id);

CREATE TABLE perfil_links (
    id UUID PRIMARY KEY,
    perfil_id UUID NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    rotulo VARCHAR(60),
    url VARCHAR(1000) NOT NULL,
    ordem_exibicao SMALLINT NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_perfil_links_perfil FOREIGN KEY (perfil_id) REFERENCES perfis (id),
    CONSTRAINT ck_perfil_links_tipo CHECK (
        tipo IN ('SITE', 'BEATSTARS', 'SOUNDCLOUD', 'SPOTIFY', 'YOUTUBE', 'INSTAGRAM', 'TIKTOK', 'OUTRO')
    ),
    CONSTRAINT ck_perfil_links_ordem CHECK (ordem_exibicao BETWEEN 0 AND 100)
);

CREATE INDEX ix_perfil_links_listagem
    ON perfil_links (perfil_id, ativo, ordem_exibicao, id);

CREATE TABLE aceites_legais (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    documento VARCHAR(30) NOT NULL,
    versao_documento VARCHAR(30) NOT NULL,
    origem VARCHAR(20) NOT NULL,
    aceito_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_aceites_legais_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_aceites_legais UNIQUE (usuario_id, documento, versao_documento),
    CONSTRAINT ck_aceites_legais_documento CHECK (documento IN ('TERMOS_USO', 'POLITICA_PRIVACIDADE')),
    CONSTRAINT ck_aceites_legais_origem CHECK (origem IN ('PWA', 'ANDROID', 'IOS', 'ADMIN'))
);

CREATE TABLE tokens_verificacao_email (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMPTZ NOT NULL,
    utilizado_em TIMESTAMPTZ,

    CONSTRAINT fk_tokens_verificacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_tokens_verificacao_hash UNIQUE (token_hash),
    CONSTRAINT ck_tokens_verificacao_validade CHECK (expira_em > criado_em)
);

CREATE INDEX ix_tokens_verificacao_pendentes
    ON tokens_verificacao_email (usuario_id, expira_em)
    WHERE utilizado_em IS NULL;

CREATE TABLE sessoes_refresh (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    familia_id UUID NOT NULL,
    token_hash CHAR(64) NOT NULL,
    cliente VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMPTZ NOT NULL,
    revogado_em TIMESTAMPTZ,
    substituido_por_id UUID,

    CONSTRAINT fk_sessoes_refresh_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_sessoes_refresh_substituicao FOREIGN KEY (substituido_por_id) REFERENCES sessoes_refresh (id),
    CONSTRAINT uk_sessoes_refresh_hash UNIQUE (token_hash),
    CONSTRAINT ck_sessoes_refresh_cliente CHECK (cliente IN ('PWA', 'ANDROID', 'IOS', 'OUTRO')),
    CONSTRAINT ck_sessoes_refresh_validade CHECK (expira_em > criado_em)
);

CREATE INDEX ix_sessoes_refresh_ativas_usuario
    ON sessoes_refresh (usuario_id, expira_em)
    WHERE revogado_em IS NULL;

CREATE INDEX ix_sessoes_refresh_familia
    ON sessoes_refresh (familia_id, criado_em);

CREATE TABLE eventos_auditoria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id UUID,
    evento VARCHAR(80) NOT NULL,
    recurso_tipo VARCHAR(60),
    recurso_id VARCHAR(100),
    resultado VARCHAR(20) NOT NULL,
    request_id VARCHAR(64),
    metadados JSONB NOT NULL DEFAULT '{}'::JSONB,
    ocorrido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_eventos_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_eventos_auditoria_resultado CHECK (resultado IN ('SUCESSO', 'NEGADO', 'FALHA'))
);

CREATE INDEX ix_eventos_auditoria_usuario_tempo
    ON eventos_auditoria (usuario_id, ocorrido_em DESC, id DESC);

CREATE INDEX ix_eventos_auditoria_evento_tempo
    ON eventos_auditoria (evento, ocorrido_em DESC, id DESC);

CREATE INDEX ix_perfis_listagem_publica
    ON perfis (tipo, nome_exibicao, id)
    WHERE situacao = 'ATIVO';

CREATE INDEX ix_perfil_membros_por_usuario
    ON perfil_membros (usuario_id, situacao, perfil_id);

INSERT INTO especialidades (codigo, nome, descricao, ordem_exibicao) VALUES
    ('FAN', 'Fã', 'Pessoa que acompanha, apoia e interage com artistas e projetos musicais.', 10),
    ('ARTISTA', 'Artista', 'Artista musical ou performático com atuação autoral ou colaborativa.', 20),
    ('MUSICO', 'Músico', 'Instrumentista ou profissional de execução musical.', 30),
    ('CANTOR', 'Cantor', 'Profissional de interpretação vocal.', 40),
    ('RAPPER', 'Rapper', 'Artista de rap, rima, composição e performance.', 50),
    ('DJ', 'DJ', 'Profissional de discotecagem, performance e curadoria musical.', 60),
    ('COMPOSITOR', 'Compositor', 'Autor de letras, melodias ou obras musicais.', 70),
    ('PRODUTOR_MUSICAL', 'Produtor musical', 'Responsável pela direção e produção musical de obras e fonogramas.', 80),
    ('BEATMAKER', 'Beatmaker', 'Criador e produtor de beats e instrumentais.', 90),
    ('MIXER', 'Profissional de mixagem', 'Responsável pela edição e mixagem de áudio.', 100),
    ('MASTERIZADOR', 'Profissional de masterização', 'Responsável pela finalização e masterização de áudio.', 110),
    ('AGENCIA', 'Agência', 'Equipe ou organização de representação, comunicação ou produção artística.', 120),
    ('PRODUTORA', 'Produtora', 'Organização de produção musical, cultural ou de conteúdo.', 130),
    ('FOTOGRAFO', 'Fotógrafo', 'Profissional de fotografia artística, editorial ou de eventos.', 140),
    ('VIDEOMAKER', 'Videomaker', 'Profissional de captação, edição e criação de vídeos.', 150),
    ('PRODUTOR_AUDIOVISUAL', 'Produtor audiovisual', 'Profissional de planejamento e execução de produções audiovisuais.', 160),
    ('OUTRO', 'Outra atuação', 'Outra atividade ligada à música, cultura ou produção criativa.', 999);

COMMENT ON TABLE usuarios IS 'Contas humanas que autenticam no sistema; organizações não compartilham credenciais.';
COMMENT ON TABLE perfis IS 'Identidades públicas pessoais ou organizacionais usadas na rede social.';
COMMENT ON TABLE eventos_auditoria IS 'Eventos técnicos e de segurança; nunca deve receber senhas, tokens ou conteúdo sensível.';
