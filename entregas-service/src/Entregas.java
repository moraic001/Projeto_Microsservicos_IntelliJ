import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Microsservico de entregas.
 *
 * Mantem seus proprios registros em memoria e consulta Pedidos exclusivamente
 * pela API HTTP. Nao importa nem acessa classes ou mapas de Pedidos.
 */
public class Entregas {
    private static final ConcurrentHashMap<Integer, String> entregas = new ConcurrentHashMap<>();
    private static final AtomicInteger sequencia = new AtomicInteger();

    private static final HttpClient cliente = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build();

    private static final String PEDIDOS_URL = System.getenv().getOrDefault(
        "PEDIDOS_URL", "http://localhost:8082");

    public static void main(String[] args) throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 8083), 0);
        servidor.createContext("/", e -> {
            String caminho = e.getRequestURI().getPath();

            if (caminho.equals("/entregas")) {
                if (!e.getRequestMethod().equals("POST")) {
                    metodo(e, "POST");
                    return;
                }
                criar(e);
                return;
            }

            if (caminho.startsWith("/entregas/")) {
                if (!e.getRequestMethod().equals("GET")) {
                    metodo(e, "GET");
                    return;
                }
                consultar(e, caminho.substring("/entregas/".length()));
                return;
            }

            responder(e, 404, "{\"erro\":\"Rota inexistente\"}");
        });
        servidor.start();
        System.out.println("Entregas em http://localhost:8083");
    }

    private static void criar(HttpExchange e) throws IOException {
        // O contrato recebe somente o ID do pedido em text/plain.
        byte[] bytes = e.getRequestBody().readNBytes(65);
        if (bytes.length > 64) {
            responder(e, 413, "{\"erro\":\"Corpo muito grande\"}");
            return;
        }

        int pedidoId;
        try {
            pedidoId = Integer.parseInt(new String(bytes, StandardCharsets.UTF_8).trim());
            if (pedidoId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            responder(e, 400, "{\"erro\":\"Envie um ID de pedido inteiro positivo no corpo\"}");
            return;
        }

        HttpRequest requisicao;
        try {
            requisicao = HttpRequest.newBuilder()
                .uri(URI.create(PEDIDOS_URL + "/pedidos/" + pedidoId))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        } catch (IllegalArgumentException ex) {
            responder(e, 503, "{\"erro\":\"Endereco do servico de pedidos invalido\"}");
            return;
        }

        try {
            HttpResponse<String> resposta = cliente.send(
                requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() == 404) {
                responder(e, 422, "{\"erro\":\"Pedido informado nao existe\"}");
                return;
            }
            if (resposta.statusCode() != 200) {
                responder(e, 503, "{\"erro\":\"Servico de pedidos indisponivel\"}");
                return;
            }

            int id = sequencia.incrementAndGet();
            String entrega = "{\"id\":" + id
                + ",\"pedidoId\":" + pedidoId
                + ",\"status\":\"AGUARDANDO_ENVIO\"}";

            // O registro pertence a Entregas; a consulta local nao depende de Pedidos.
            entregas.put(id, entrega);
            e.getResponseHeaders().set("Location", "/entregas/" + id);
            responder(e, 201, entrega);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            responder(e, 503, "{\"erro\":\"Consulta ao servico de pedidos interrompida\"}");
        } catch (IOException ex) {
            responder(e, 503, "{\"erro\":\"Nao foi possivel conectar ao servico de pedidos\"}");
        }
    }

    private static void consultar(HttpExchange e, String trechoId) throws IOException {
        try {
            int id = Integer.parseInt(trechoId);
            if (id <= 0) {
                throw new NumberFormatException();
            }

            String entrega = entregas.get(id);
            if (entrega == null) {
                responder(e, 404, "{\"erro\":\"Entrega inexistente\"}");
            } else {
                responder(e, 200, entrega);
            }
        } catch (NumberFormatException ex) {
            responder(e, 400, "{\"erro\":\"ID da entrega deve ser inteiro positivo\"}");
        }
    }

    private static void metodo(HttpExchange e, String permitido) throws IOException {
        e.getResponseHeaders().set("Allow", permitido);
        responder(e, 405, "{\"erro\":\"Metodo nao permitido\"}");
    }

    private static void responder(HttpExchange e, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        e.sendResponseHeaders(status, bytes.length);
        try (var saida = e.getResponseBody()) {
            saida.write(bytes);
        }
        System.out.println(e.getRequestMethod() + " " + e.getRequestURI() + " -> " + status);
    }
}
