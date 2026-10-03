package vmware.services.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe o gateway de verdade (Netty) sem Kubernetes e exercita o roteamento: uma rota estatica
 * /proxy/** (com StripPrefix=1) aponta para o proprio gateway, entao /proxy/actuator/health
 * so responde se o gateway encaminhou a requisicao e removeu o prefixo.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "spring.cloud.kubernetes.enabled=false",
                "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false"
        })
class GatewayApplicationTests {

    private static final int PORT = freePort();

    /**
     * A rota inteira fica aqui (e nao em "properties"): listas indexadas nao sao mescladas entre
     * fontes de propriedades, entao o "uri" dinamico esconderia o restante da rota.
     */
    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) {
        String route = "spring.cloud.gateway.server.webflux.routes[0]";
        registry.add("server.port", () -> PORT);
        registry.add(route + ".id", () -> "self");
        registry.add(route + ".uri", () -> "http://localhost:" + PORT);
        registry.add(route + ".predicates[0].name", () -> "Path");
        registry.add(route + ".predicates[0].args.pattern", () -> "/proxy/**");
        registry.add(route + ".filters[0].name", () -> "StripPrefix");
        registry.add(route + ".filters[0].args.parts", () -> "1");
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + PORT + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void exposesItsOwnHealthEndpoint() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void routesRequestsMatchingTheConfiguredPathAndStripsThePrefix() throws Exception {
        HttpResponse<String> response = get("/proxy/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void answers404WhenNoRouteMatches() throws Exception {
        assertThat(get("/no-such-route/anything").statusCode()).isEqualTo(404);
    }
}
