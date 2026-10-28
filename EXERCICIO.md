# Exercício Serviço de Entregas

Crie um terceiro projeto Java no IntelliJ, chamado entregas-service. Classe principal Entregas, servidor na porta 8083 e armazenamento próprio em memória. Não importe classes de Pedidos e não acesse seus dados internos.

## Contrato

- POST /entregas recebe ID inteiro positivo de um pedido em text/plain.
- Consulte GET http://localhost:8082/pedidos/{id} com timeouts.
- Em 200 remoto, gere ID da entrega, armazene e responda 201 com Location.
- Em 404 remoto, responda 422 sem gravar.
- Em falha de rede ou outro status remoto diferente de 200, responda 503 sem gravar.
- Entrada inválida recebe 400. Método incorreto recebe 405 com Allow. Rota desconhecida recebe 404.
- GET /entregas/{id} consulta somente a memória local e responde 200 ou 404. ID inválido recebe 400.

Resposta de exemplo:

```json
{"id":1,"pedidoId":1,"status":"AGUARDANDO_ENVIO"}
```

Na versão básica, várias entregas para o mesmo pedido são permitidas.

## Demonstração obrigatória

1. Criar um pedido e depois sua entrega.
2. Consultar a entrega criada.
3. Testar ID inválido e pedido inexistente.
4. Parar Pedidos e demonstrar 503 na criação.
5. Consultar uma entrega já criada com Pedidos parado: deve funcionar.
6. Parar só Produtos e criar uma entrega para pedido existente: deve funcionar.
7. Reiniciar Entregas e demonstrar a perda dos registros em memória.

## Avaliação

Processo e responsabilidade próprios: 2 pontos. Integração HTTP: 2. POST, GET e IDs próprios: 2. Validação e falhas: 2. Testes e documentação: 1. Explicação individual: 1.

Entregar Entregas.java, README, matriz de resultados e desenho da arquitetura.
