# Sem Blefe API

Backend inicial da plataforma musical Sem Blefe, preparado para atender o PWA em React e, posteriormente, o aplicativo React Native.

## O que já existe

- Spring Boot 3.5.16 e Java 21.
- PostgreSQL 17.10 em Docker.
- Flyway como única fonte de criação e evolução do banco.
- Estrutura inicial de contas, perfis pessoais/organizacionais, especialidades, consentimentos, sessões e auditoria.
- Segurança com negação por padrão: somente endpoints explicitamente públicos são liberados.
- Consulta pública de especialidades usando projeção DTO, sem carregar ou expor entidades completas.
- OpenAPI/Swagger.
- Testes de integração usando PostgreSQL real em Testcontainers.
- Portas e adaptadores com regras de dependência verificadas por ArchUnit.
- Health checks de liveness/readiness e execução stateless para múltiplas instâncias.

## Pré-requisitos no Windows

- JDK 21.
- IntelliJ IDEA.
- Docker Desktop com Docker Compose.
- Git.

## Primeiro teste no IntelliJ

### 1. Preparar o banco

Abra o terminal do PowerShell na pasta do projeto:

```powershell
Copy-Item .env.example .env
```

Abra o `.env` e troque `POSTGRES_PASSWORD` por uma senha local forte. Depois execute:

```powershell
docker compose up -d
docker compose ps
```

O contêiner `sem-blefe-postgres` deverá aparecer como `healthy`.

### 2. Abrir o backend

1. No IntelliJ, escolha **Open** e selecione a pasta `sem-blefe-backend`.
2. Confirme a importação do projeto Maven.
3. Em **File > Project Structure**, selecione o JDK 21.
4. Abra **Run > Edit Configurations** e crie uma configuração para `SemBlefeApplication`.
5. Em **Environment variables**, informe:

```text
POSTGRES_DB=semblefe;POSTGRES_USER=semblefe_app;POSTGRES_PASSWORD=SUA_SENHA;POSTGRES_PORT=5433
```

6. Execute `SemBlefeApplication`.

O Flyway aplicará automaticamente `V1__estrutura_inicial_identidade_perfis.sql`. O Hibernate está configurado com `ddl-auto=validate`: ele não poderá modificar o banco silenciosamente.

### 3. Validar as respostas

No navegador ou Postman:

- Health check: <http://localhost:8080/actuator/health>
- Liveness: <http://localhost:8080/actuator/health/liveness>
- Readiness: <http://localhost:8080/actuator/health/readiness>
- Especialidades: <http://localhost:8080/api/v1/publico/especialidades>
- Swagger: <http://localhost:8080/swagger-ui.html>

Resultado esperado do health check:

```json
{"status":"UP"}
```

A lista pública começa com `FAN` e inclui artista, músico, DJ, produtor musical, beatmaker, mixagem, masterização, agência, produtora, fotografia, vídeo e audiovisual.

### 4. Executar testes automatizados

No painel Maven do IntelliJ, execute `Lifecycle > test`, ou use um Maven 3.6.3 ou superior:

```powershell
mvn test
```

Os testes iniciam um PostgreSQL isolado, aplicam todas as migrations e verificam a API e a proteção das rotas. O Docker precisa estar ativo.

### 5. Encerrar somente o ambiente local

```powershell
docker compose down
```

Esse comando preserva os dados. Para apagar o volume também, use `docker compose down -v` somente quando tiver certeza de que os dados locais podem ser descartados.

## Estrutura de pacotes

```text
br.com.semblefe
├── compartilhado
│   ├── configuracao
│   └── web
└── perfis
    ├── api
    ├── aplicacao
    │   ├── modelo
    │   └── porta
    ├── dominio
    └── infraestrutura
        └── persistencia
```

Os novos módulos seguirão a mesma separação. Um controller não acessa repository diretamente, a aplicação depende de interfaces próprias e um módulo não consulta o repository de outro módulo.

## Próxima etapa

A próxima implementação será o fluxo seguro:

> cadastro → confirmação de e-mail → login → refresh rotativo → criação do perfil → edição autorizada do próprio perfil

Ela incluirá Argon2id, tokens armazenados como hash, MFA administrativo, rate limit compartilhado e testes de tentativa de acesso a perfis de terceiros.

## Commits sugeridos

Para registrar esta fundação:

```powershell
git init
git add .
git commit -m "feat: cria fundacao segura do backend Sem Blefe"
```
