# Baseline de segurança

## Princípio central

O React e o React Native nunca decidem autorização, preço, saldo, selo verificado, status ou propriedade de um recurso. O backend valida todas as regras novamente e nega por padrão.

Isso não significa ignorar a segurança do cliente. O PWA precisará de cookie de renovação `HttpOnly`, `Secure` e proteção CSRF; o aplicativo usará Keychain/Keystore.

## Controles obrigatórios

- DTOs explícitos e allowlist de campos; nunca receber entidades JPA.
- Argon2id para senhas e proteção contra senhas comuns ou vazadas.
- Access token curto; refresh token opaco, rotativo e armazenado somente como hash.
- Detecção de reutilização com revogação da família de sessões.
- Verificação de e-mail e recuperação com tokens descartáveis armazenados como hash.
- MFA para ADMIN e MODERADOR.
- Rate limit por IP, conta e ação com armazenamento compartilhado.
- Autorização por papel, vínculo organizacional e propriedade do recurso.
- Testes contra BOLA/IDOR em toda operação por identificador.
- Logs sem senha, token, cookie, código de recuperação ou corpo sensível.
- Segredos apenas em variáveis protegidas ou cofre; nunca no Git.
- Auditoria de login, privilégios, moderação, pontos, preços e pagamentos.
- Consentimento versionado, minimização de dados, exportação e exclusão conforme LGPD.

## Upload seguro

Nenhum arquivo será publicado diretamente após o envio. O fluxo planejado é:

1. autorização do backend;
2. entrada em quarentena;
3. validação por assinatura real, tamanho e duração;
4. varredura e remoção de metadados;
5. transcodificação e geração de prévias;
6. estado `PRONTO` ou `REJEITADO`;
7. publicação somente quando todas as mídias estiverem prontas.

SVG e arquivos executáveis não serão aceitos como avatar. Vídeos e áudios terão quotas para impedir esgotamento de CPU, disco e custos.

## Critério mínimo de liberação

- Todas as regras de autorização têm teste permitido e negado.
- Migrations sobem do zero no PostgreSQL usado em produção.
- Erros não expõem SQL, stack trace, segredo ou caminho interno.
- Dependências não possuem vulnerabilidade crítica ou alta conhecida sem mitigação.
- Endpoints administrativos retornam 401 sem autenticação e 403 sem permissão.
- Um usuário não acessa e-mail, sessão ou perfil privado de outro.
- Upload malicioso, grande, corrompido ou disfarçado é rejeitado.
