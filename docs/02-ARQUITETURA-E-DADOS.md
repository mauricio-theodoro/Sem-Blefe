# Arquitetura e dados

## Estratégia

O backend começa como monólito modular. Existe um único deploy, mas cada domínio mantém suas próprias regras e repositórios. Microserviços agora aumentariam custo, superfície de ataque e dificuldade de testes sem uma necessidade medida.

Módulos planejados:

- `identidade`: contas, autenticação, sessões e consentimentos.
- `perfis`: identidade pública, organizações, especialidades e portfólio.
- `rede`: seguir, conexão, bloqueio e silenciamento.
- `conteudo`: publicação, comentário, reação e compartilhamento.
- `midia`: upload, validação, armazenamento e processamento.
- `feed`: consultas globais e da rede.
- `agenda`: serviços, disponibilidade, eventos e inscrições.
- `comercial`: orçamento, pedido, pagamento e reembolso.
- `fidelidade`: carteira e lançamentos de pontos.
- `moderacao`: denúncias e sanções.
- `integracoes`: adaptadores BeatStars, SoundCloud e futuros fornecedores.

Cada módulo seguirá a divisão `api`, `aplicacao`, `dominio` e `infraestrutura`, usando portas de entrada e saída:

```text
api -> porta de entrada -> caso de uso -> porta de saída <- adaptador JPA
```

- `api` conhece HTTP, validação e DTOs de resposta.
- `aplicacao` organiza casos de uso e declara portas.
- `dominio` concentra regras puras, sem Spring ou JPA.
- `infraestrutura` implementa persistência e integrações externas.

Testes ArchUnit impedem que `aplicacao` dependa de `api` ou `infraestrutura`.

## Usuário, perfil e especialidade

- `usuario` é a conta humana que autentica.
- `perfil` é a identidade pública que publica e presta serviços.
- `especialidade` descreve a atuação: DJ, artista, beatmaker, fotógrafo etc.
- `papel` controla segurança: USUARIO, MODERADOR ou ADMIN.

Uma pessoa pode ter um perfil pessoal, várias especialidades e administrar organizações. Especialidade nunca concede permissão administrativa.

## Decisões de desempenho

- IDs públicos em UUID; IDs internos de catálogo podem ser `BIGINT IDENTITY`.
- Listagens sempre paginadas e com limite imposto pelo backend.
- Feed futuro com cursor `(publicado_em, id)`, evitando `OFFSET` profundo.
- Projeções DTO para consultas públicas; entidades JPA não são respostas HTTP.
- `spring.jpa.open-in-view=false` para impedir consultas acidentais na camada web.
- Índices criados pelas consultas previstas, e não indiscriminadamente.
- MP4, áudio e imagens ficam em object storage. PostgreSQL guarda metadados e estado de processamento.
- Preços são registros versionados; nunca constantes no Java.
- Pontos usam lançamentos imutáveis; nunca apenas uma coluna de saldo.
- Reservas, pagamentos e processamento de mídia usam idempotência.

## Consultas futuras do feed

O feed global começa cronológico. A consulta deve filtrar conteúdo público, publicado, não removido, com mídia pronta e autor não bloqueado. O feed da rede acrescenta perfis seguidos e conexões aceitas.

Índices previstos quando o módulo de conteúdo nascer:

```sql
CREATE INDEX ix_publicacoes_feed_global
    ON publicacoes (publicado_em DESC, id DESC)
    WHERE situacao = 'PUBLICADA' AND visibilidade = 'PUBLICA';

CREATE INDEX ix_publicacoes_por_autor
    ON publicacoes (autor_perfil_id, publicado_em DESC, id DESC)
    WHERE situacao = 'PUBLICADA';
```

Antes de concluir cada etapa, as consultas críticas serão verificadas com `EXPLAIN (ANALYZE, BUFFERS)` e massa de dados realista.
