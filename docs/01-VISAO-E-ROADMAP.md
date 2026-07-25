# Sem Blefe - visão e roadmap técnico

## Visão do produto

O Sem Blefe será uma plataforma musical com três frentes distintas:

1. Rede social e profissional para fãs, artistas e profissionais da música.
2. Agenda e contratação de serviços e dinâmicas do estúdio.
3. Operação futura de beats, direitos, distribuição e benefícios por pontos.

A visão completa será preservada, mas esses produtos não serão implementados ao mesmo tempo. O núcleo que valida a plataforma é:

> cadastro → perfil → seguir/conectar → publicar → feed → interagir → bloquear/denunciar

## Decisões de produto que precisam ser fechadas

- Público inicial: a recomendação para o MVP é maiores de 18 anos. Uma rede acessível a menores exige um projeto específico de proteção, privacidade e moderação.
- Seguir e conectar serão ações diferentes: seguir é unilateral; conexão exige aceite das duas pessoas.
- O preço de R$ 250 de mixagem e masterização precisa ser definido como preço por faixa, hora ou projeto.
- Agência e produtora terão perfil organizacional administrado por contas pessoais; não haverá senha empresarial compartilhada.
- Conteúdo e identidade serão inspirados em boas práticas de redes profissionais, sem copiar interface ou identidade de Facebook e LinkedIn.
- O sistema precisa de política de titularidade, denúncia e contestação antes de receber beats, samples, covers e vídeos.

## Roadmap executável

### Etapa 1 - Fundação técnica (pacote atual)

- Java 21, Spring Boot, PostgreSQL e Flyway.
- Monólito modular e API versionada.
- Banco inicial de identidade, perfis, especialidades, consentimento, sessões e auditoria.
- Consultas por projeção, índices e proteção padrão de endpoints.
- OpenAPI/Swagger e testes de integração com PostgreSQL real em Testcontainers.

Critério de aceite: banco sobe do zero, migrations são validadas, health check responde e especialidades são listadas pela API pública.

### Etapa 2 - Cadastro, autenticação e perfil

- Cadastro com e-mail e senha usando Argon2id.
- Confirmação de e-mail, login, refresh rotativo, logout e recuperação de senha.
- Perfis pessoais e organizacionais com várias especialidades.
- MFA obrigatório para ADMIN e MODERADOR.
- Usuário altera somente o próprio perfil ou organização que administra.

Critério de aceite: cadastro → confirmação → login → criação/edição do próprio perfil, com testes positivos e negativos de autorização.

### Etapa 3 - Rede e moderação

- Seguir, solicitação de conexão, bloqueio e silenciamento.
- Denúncia, análise e sanções administrativas.
- Privacidade de perfil e campos públicos.

### Etapa 4 - Publicações, mídia e feeds

- Texto, imagem, áudio e MP4.
- Arquivos em armazenamento de objetos; somente metadados no PostgreSQL.
- Quarentena, validação, remoção de metadados e transcodificação assíncrona.
- Feed global e feed da rede por cursor, sem `OFFSET` profundo.
- Comentários, reações e republicações.

### Etapa 5 - Agenda, eventos e serviços

- Captação vocal, mixagem/masterização, beats e audiovisual como ofertas configuráveis.
- Tipos de preço: fixo, por hora, a partir de, orçamento e externo.
- Podcast, Fotógrafo Day, Perfil Solo, Cypher e Letra Explicada como eventos configuráveis.
- Disponibilidade, capacidade, inscrição, fila de espera, conflito e ausência.

### Etapa 6 - Comercial

- Orçamentos, pedidos, pagamentos dos serviços próprios, cancelamentos e reembolsos.
- Webhooks assinados e idempotentes.
- Nenhum dado de cartão passa pelo backend do Sem Blefe.

### Etapa 7 - Pontos

- Carteira e livro-razão imutável de créditos, débitos, expiração e estorno.
- Regras antifraude e catálogo de benefícios.

### Etapa 8 - Integrações e aplicativo

- Links oficiais de BeatStars, SoundCloud e outros serviços entram antes de automações.
- Integração somente após confirmação de API e termos comerciais oficiais.
- PWA estabilizado antes do React Native.

## Fora do MVP

- Custódia e repasse de royalties.
- Venda nativa e licenciamento automático de beats.
- Cadastro automático na UBC e emissão automática de ISRC.
- Distribuição automática para plataformas de streaming.
- Algoritmo avançado de recomendação.
- Chat e transmissão ao vivo.
