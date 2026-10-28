# Exemplo de microsserviços Java no IntelliJ IDEA

Este projeto corresponde à apresentação Aula de Microsserviços com Java no IntelliJ IDEA. São dois serviços Java independentes, com o mesmo código e os mesmos contratos do material.

## Requisitos

- IntelliJ IDEA e um JDK completo 17 ou superior instalado.
- Portas 8081 e 8082 livres no próprio computador.
- Não precisa instalar Spring Boot, Maven, Gradle ou banco de dados.

## 1 Abrir Produtos

1. Extraia todo o ZIP em uma pasta local.
2. No IntelliJ, use **File > Open** e selecione a pasta **produtos-service**.
3. Se a IDE pedir confirmação de confiança, revise o código local e abra o projeto.
4. Em **File > Project Structure > Project**, escolha seu **Project SDK**: JDK 17 ou superior. Se necessário, use Add SDK / JDK from Disk para apontar para a instalação existente. O nome e a localização exatos do JDK variam por computador.
5. Em **Modules > Dependencies**, mantenha **Project SDK**. Em Sources, `src` deve aparecer como pasta de fontes.
6. Abra **src/Produtos.java** e execute **Produtos.main()** pelo ícone verde ao lado de main. Também há uma configuração compartilhada chamada Produtos.
7. Aguarde a mensagem `Produtos em http://localhost:8081`. Mantenha a execução ativa.
8. No navegador, abra http://localhost:8081/produtos/1.

As configurações de projeto não fixam uma instalação de JDK de outro computador. A seleção do SDK na primeira abertura é esperada. Os nomes dos menus podem variar com a versão e o idioma da IDE.

## 2 Abrir Pedidos

1. Use **File > Open** e selecione **pedidos-service**.
2. Escolha **New Window / Nova janela**, para manter Produtos aberto.
3. Selecione o JDK em Project Structure, como no primeiro projeto.
4. Abra **src/Pedidos.java** e execute **Pedidos.main()**. Há também uma configuração chamada Pedidos.
5. Aguarde `Pedidos em http://localhost:8082`.
6. Confira se os dois consoles continuam ativos.

O endereço de Produtos já tem o padrão `http://localhost:8081`. Para alterá-lo, use **Run > Edit Configurations > Pedidos > Environment variables** e configure `PRODUTOS_URL=http://localhost:8081`, sem barra final. Esta etapa é opcional no laboratório.

As configurações compartilhadas em `.run` incluem a opção de VM `--add-modules jdk.httpserver`. Não crie `module-info.java` para esta atividade.

## 3 Criar e consultar um pedido

Abra **testes.http** na raiz de pedidos-service. Com o HTTP Client disponível na instalação, execute primeiro a requisição 5 e depois a 6. Os números identificam a ordem sugerida, não são IDs da API. Ajuste o ID da consulta para aquele retornado pelo POST.

O corpo do POST é somente o número `1`, em **text/plain**. Não envie `{"produtoId":1}` nesta versão.

Se sua instalação não executar arquivos HTTP, use um cliente HTTP externo ou o **Terminal** integrado ao IntelliJ.

**Windows PowerShell, comando em uma linha:**

```powershell
curl.exe -i -X POST http://localhost:8082/pedidos -H "Content-Type: text/plain" --data "1"
curl.exe -i http://localhost:8082/pedidos/1
```

**Linux/macOS, comando em uma linha:**

```bash
curl -i -X POST http://localhost:8082/pedidos -H "Content-Type: text/plain" --data "1"
curl -i http://localhost:8082/pedidos/1
```

Na primeira criação após iniciar Pedidos, o resultado esperado é **201** com `Location: /pedidos/1` e este corpo:

```json
{"id":1,"produto":{"id":1,"nome":"Caderno","precoCentavos":1500},"status":"CRIADO"}
```

`precoCentavos:1500` significa R$ 15,00. O GET do pedido criado retorna **200**. A ordem e a capitalização dos cabeçalhos podem variar.

## 4 Reproduzir a falha parcial

1. Crie pelo menos um pedido e anote o ID.
2. Na janela de Produtos, use Stop para encerrar somente esse serviço.
3. Consulte o pedido já criado. Deve retornar 200.
4. Tente criar um novo pedido. Deve retornar 503.
5. Execute Produtos novamente. A criação deve voltar a funcionar.
6. Reinicie Pedidos e consulte um ID antigo. Será 404, pois a memória do processo foi reiniciada.

Não execute todas as requisições de testes.http indiscriminadamente: os testes 13 e 14 dependem de parar Produtos manualmente.

## 5 Contratos do exemplo

| Operação | Entrada | Resultado |
|---|---|---|
| GET /produtos/{id} na porta 8081 | ID no caminho | 200 ou 404; 400 se ID inválido |
| POST /pedidos na porta 8082 | ID positivo como texto | 201; 400 inválido; 422 produto inexistente; 503 catálogo indisponível |
| GET /pedidos/{id} na porta 8082 | ID no caminho | 200 ou 404; 400 se ID inválido |

Método incorreto em rota conhecida: 405 com Allow. Rota desconhecida: 404. Corpo do POST acima de 64 bytes: 413.

## 6 Entender o código

**Produtos.java:** cria o servidor HTTP, mantém o catálogo fixo, valida o método e o ID e responde em JSON.

**Pedidos.java:** valida a entrada, consulta Produtos com HttpClient, trata a resposta remota, cria um ID próprio e guarda o pedido no mapa local. GET de pedido usa apenas esse mapa. O snapshot guarda os dados do produto vistos na criação.

Para depurar, coloque um breakpoint antes de `cliente.send`. Execute Pedidos em Debug e Produtos em Run. Envie um POST e observe `produtoId` e a resposta. Use Resume para permitir que o cliente receba o resultado.

## 7 Diagnóstico rápido

| Sintoma | O que conferir |
|---|---|
| SDK não configurado | Project Structure > Project SDK e Module SDK |
| Main não aparece | JDK selecionado, classe em src e assinatura de main |
| Address already in use | Outra execução do mesmo serviço ainda está ativa |
| Erro nos imports HttpServer | JDK completo com jdk.httpserver e pasta src como Sources Root |
| Resposta 400 | Corpo deve conter apenas um ID inteiro positivo |
| Resposta 503 | Produtos deve estar ativo e PRODUTOS_URL deve estar correta |
| Pedido desapareceu | Dados em memória se perdem ao reiniciar Pedidos |

## 8 Limites didáticos

Servidores acessíveis somente no próprio computador, usando 127.0.0.1. Não há banco, autenticação, HTTPS, estoque ou pagamento real. O catálogo usa JSON de valores fixos e o pedido incorpora esse JSON. Não ampliar isso concatenando entrada livre de usuários: uma evolução deve usar serialização e validação adequadas.

O timeout de conexão é 2 segundos e o da requisição é 3 segundos. Não há retry de negócio nem proteção contra duplicação de POST. Os IDs são locais a cada execução.

## 9 Exercício de Entregas

O exercício de Entregas foi implementado neste pacote. Consulte `EXERCICIO.md` para os requisitos e `entregas-service/README.md`, `MATRIZ_TESTES.md` e `ARQUITETURA.md` para execução, testes e arquitetura.

## Execução alternativa por terminal

Dentro de cada pasta `src`, em terminais separados:

```text
java --add-modules jdk.httpserver Produtos.java
java --add-modules jdk.httpserver Pedidos.java
```

Cada comando deve executar na pasta do arquivo correspondente. No IntelliJ, prefira Run nas classes.


## 10 Executar Entregas (exercício implementado)

1. Abra a pasta `entregas-service` em uma nova janela do IntelliJ.
2. Selecione o mesmo JDK 17 ou superior.
3. Execute `src/Entregas.java` e mantenha o processo ativo. O serviço usa a porta 8083.
4. Mantenha `produtos-service` (8081) e `pedidos-service` (8082) ativos para o fluxo inicial.
5. Consulte `entregas-service/README.md`, `testes.http`, `MATRIZ_TESTES.md` e `ARQUITETURA.md` para o contrato e a demonstração.

A criação de entrega consulta `GET /pedidos/{id}` por HTTP. Não há dependência direta das classes de Pedidos. Para testar a falha parcial, siga a ordem e as instruções em `MATRIZ_TESTES.md`.
