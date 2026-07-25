# Estratégia de escalabilidade horizontal

Escalar horizontalmente significa executar várias instâncias da API ao mesmo tempo. Isso não é obtido apenas com uma estrutura de pacotes: o estado compartilhado precisa estar fora da memória e do disco local de cada instância.

## Regras desde o primeiro incremento

- API stateless; nenhuma sessão HTTP em memória.
- Banco e migrations independentes da instância.
- Nenhum upload permanente no disco local.
- Tokens e permissões validados pelo backend em todas as requisições.
- Operações sensíveis com idempotência.
- Health checks distintos para processo vivo e instância pronta para receber tráfego.
- Encerramento gracioso para terminar requisições durante atualização.
- Logs com `requestId` para rastrear uma chamada entre serviços.

Endpoints preparados:

- `/actuator/health/liveness`: informa se o processo deve ser reiniciado.
- `/actuator/health/readiness`: informa se a instância está pronta e se alcança o banco.

## Evolução por demanda

### Fase inicial

- Uma API Spring Boot.
- PostgreSQL.
- Armazenamento de objetos quando começar o módulo de mídia.
- Backups e observabilidade.

### Duas ou mais instâncias

- Balanceador de carga.
- Redis compartilhado para rate limit, cache pontual e presença.
- Object storage e CDN para imagens, áudio e vídeo.
- Métricas centralizadas e autoscaling.
- Nenhuma dependência de sticky session.

### Processamento intenso

- Fila para transcodificação, notificações e tarefas demoradas.
- Workers separados da API.
- Outbox transacional para não perder eventos confirmados no banco.
- PgBouncer ou serviço equivalente para controlar conexões.
- Réplicas de leitura somente quando métricas demonstrarem necessidade.

## O que não será antecipado

Microserviços, Kafka, múltiplos bancos e cache generalizado não entrarão apenas por expectativa de crescimento. Primeiro serão feitos testes de carga e medidos latência, uso de CPU, pool de conexões, consultas lentas e custo de mídia. A divisão ocorrerá quando houver evidência e um limite de domínio claro.
