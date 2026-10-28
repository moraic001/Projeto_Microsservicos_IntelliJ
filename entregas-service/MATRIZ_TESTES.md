# Matriz de testes — Entregas Service

Os códigos abaixo são os resultados esperados pelo contrato. Para a entrega acadêmica, execute cada cenário e preencha a última coluna com o resultado real observado.

| Nº | Cenário | Resultado esperado | Resultado observado |
|---:|---|---|---|
| 1 | Criar pedido válido em Pedidos | 201 | A preencher |
| 2 | Criar entrega para pedido existente | 201 + `Location` | A preencher |
| 3 | Consultar entrega existente | 200 | A preencher |
| 4 | Corpo não numérico | 400 | A preencher |
| 5 | ID zero/negativo | 400 | A preencher |
| 6 | Pedido inexistente (Pedidos retorna 404) | 422; sem registro | A preencher |
| 7 | Entrega inexistente | 404 | A preencher |
| 8 | ID da entrega inválido | 400 | A preencher |
| 9 | Método incorreto em `/entregas` | 405 + `Allow: POST` | A preencher |
| 10 | Método incorreto em `/entregas/{id}` | 405 + `Allow: GET` | A preencher |
| 11 | Rota desconhecida | 404 | A preencher |
| 12 | Pedidos parado durante a criação | 503; sem registro | A preencher |
| 13 | Pedidos parado após a entrega ser criada | 200 para consulta local | A preencher |
| 14 | Produtos parado, Pedidos ativo e pedido já salvo | 201 | A preencher |
| 15 | Reiniciar Entregas e consultar registro antigo | 404 | A preencher |

## Procedimento para os cenários de falha

1. Crie e consulte uma entrega com os três serviços ativos.
2. Pare somente `pedidos-service`. Uma nova criação deve responder 503; consultar a entrega já criada deve continuar respondendo 200.
3. Reinicie Pedidos. Crie um pedido válido e confirme que ele foi salvo.
4. Pare somente `produtos-service`. Como Pedidos mantém o pedido já criado em sua própria memória, Entregas deve conseguir criá-la consultando Pedidos.
5. Reinicie somente Entregas e consulte o ID antigo. O resultado deve ser 404 porque os registros são mantidos apenas na memória do processo.

Observação: os cenários 12 a 15 exigem intervenção manual nos consoles e não devem ser executados em sequência automática sem parar/reiniciar os serviços conforme indicado.
