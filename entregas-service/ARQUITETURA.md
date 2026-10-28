# Arquitetura dos microsserviços

```mermaid
flowchart LR
    Cliente[Cliente HTTP / testes.http]
    Produtos[produtos-service<br/>127.0.0.1:8081<br/>Catálogo em memória]
    Pedidos[pedidos-service<br/>127.0.0.1:8082<br/>Pedidos em memória]
    Entregas[entregas-service<br/>127.0.0.1:8083<br/>Entregas em memória]

    Cliente -->|GET /produtos/{id}| Produtos
    Cliente -->|POST /pedidos<br/>GET /pedidos/{id}| Pedidos
    Cliente -->|POST /entregas<br/>GET /entregas/{id}| Entregas
    Pedidos -->|HTTP GET /produtos/{id}| Produtos
    Entregas -->|HTTP GET /pedidos/{id}| Pedidos
```

## Responsabilidades e limites

- **Produtos** é dono do catálogo e não acessa dados internos dos outros serviços.
- **Pedidos** consulta Produtos por HTTP e mantém snapshots de pedidos em sua própria memória.
- **Entregas** consulta Pedidos por HTTP, gera IDs de entrega próprios e armazena seus registros em memória própria.
- Os serviços não compartilham classes ou mapas. A integração entre serviços é feita pelas APIs HTTP.
- Cada serviço pode consultar seus próprios registros mesmo quando o serviço remoto fica indisponível; entretanto, uma nova criação depende do serviço consultado.
- Os dados são voláteis: reiniciar um processo elimina os registros que estavam somente na memória desse processo.
