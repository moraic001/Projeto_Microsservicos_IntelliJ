# Entregas Service

Terceiro microsserviço do exercício. Aplicação Java simples com `HttpServer`, seguindo o mesmo padrão dos projetos `produtos-service` e `pedidos-service`.

## Requisitos

- JDK 17 ou superior com o módulo `jdk.httpserver`.
- Porta `8083` livre.
- `pedidos-service` executando em `http://localhost:8082` para criar novas entregas.
- Não usa Maven, Gradle, Spring Boot ou banco de dados.

## Executar no IntelliJ IDEA

1. Abra a pasta `entregas-service` em uma nova janela do IntelliJ.
2. Em **File > Project Structure > Project**, selecione um JDK 17 ou superior.
3. Confirme que `src` está marcada como Sources Root.
4. Abra `src/Entregas.java` e execute `Entregas.main()`.
5. Confirme no console: `Entregas em http://localhost:8083`.
6. Mantenha os três serviços em execução para testar o fluxo completo.

A configuração compartilhada `.run/Entregas.run.xml` inclui `--add-modules jdk.httpserver`. Se o IntelliJ não importar a configuração automaticamente, execute a classe pelo ícone verde ao lado de `main`.

### Execução pelo terminal

Na pasta `entregas-service/src`:

```bash
java --add-modules jdk.httpserver Entregas.java
```

## Contrato HTTP

| Método e rota | Resultado |
|---|---|
| `POST /entregas` com `text/plain` e ID positivo de pedido | `201 Created`, `Location: /entregas/{id}` |
| `POST /entregas` com entrada inválida | `400 Bad Request` |
| Pedido remoto retorna `404` | `422 Unprocessable Entity`, sem criar registro |
| Pedido remoto retorna outro status ou há falha de rede/timeout | `503 Service Unavailable`, sem criar registro |
| `GET /entregas/{id}` com ID válido existente | `200 OK` |
| `GET /entregas/{id}` com ID válido inexistente | `404 Not Found` |
| `GET /entregas/{id}` com ID inválido | `400 Bad Request` |
| Método incorreto em rota conhecida | `405 Method Not Allowed`, com `Allow` |
| Rota desconhecida | `404 Not Found` |
| Corpo do POST maior que 64 bytes | `413 Payload Too Large` |

O corpo do POST é apenas o número do pedido, por exemplo `1`, com `Content-Type: text/plain`. Não envie JSON.

Exemplo de resposta:

```json
{"id":1,"pedidoId":1,"status":"AGUARDANDO_ENVIO"}
```

## Integração e independência

- `Entregas` consulta `Pedidos` pela API HTTP `GET /pedidos/{id}`.
- A URL padrão é `http://localhost:8082`. Para alterar, defina a variável de ambiente `PEDIDOS_URL`, sem barra final.
- Timeout de conexão: 2 segundos. Timeout de requisição: 3 segundos.
- Entregas mantém seus próprios IDs e registros em `ConcurrentHashMap`.
- A consulta `GET /entregas/{id}` usa somente a memória local e continua funcionando mesmo se Pedidos parar.
- Reiniciar Entregas apaga seus registros, pois não existe persistência em banco.
- A versão básica permite várias entregas para o mesmo pedido.

## Testes

Execute os testes de `testes.http` com o HTTP Client do IntelliJ ou use os comandos do `README` e a matriz em `MATRIZ_TESTES.md`. Os cenários que exigem parar um serviço devem ser executados manualmente, conforme as instruções no arquivo de testes.
