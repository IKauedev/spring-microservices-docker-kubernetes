package vmware.services.gateway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protege o application.yml do gateway. As rotas sao declaradas (/employee/**, /department/**,
 * /organization/**) e o discovery locator fica desligado: sem elas o gateway sobe, mas responde 404 em tudo.
 */
class GatewayConfigurationTest {

    private static Properties properties;

    @BeforeAll
    static void loadApplicationYaml() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        properties = yaml.getObject();
    }

    @Test
    void applicationIsNamedGateway() {
        assertThat(properties.getProperty("spring.application.name")).isEqualTo("gateway");
    }

    @Test
    void discoveryLocatorIsDisabledBecauseRoutesAreDeclared() {
        assertThat(properties.getProperty("spring.cloud.gateway.server.webflux.discovery.locator.enabled"))
                .isEqualTo("false");
    }

    @ParameterizedTest
    @CsvSource({"0,employee", "1,department", "2,organization"})
    void declaresOneStaticRoutePerService(int index, String service) {
        String route = "spring.cloud.gateway.server.webflux.routes[" + index + "].";

        assertThat(properties.getProperty(route + "id")).isEqualTo(service);
        assertThat(properties.getProperty(route + "uri"))
                .contains("http://" + service + "." + service + ".svc.cluster.local:8080");
        assertThat(properties.getProperty(route + "predicates[0]")).isEqualTo("Path=/" + service + "/**");
        assertThat(properties.getProperty(route + "filters[0]")).isEqualTo("StripPrefix=1");
    }

    @Test
    void kubernetesConfigIsImportedAsOptional() {
        assertThat(properties.getProperty("spring.config.import")).isEqualTo("optional:kubernetes:");
    }
}
