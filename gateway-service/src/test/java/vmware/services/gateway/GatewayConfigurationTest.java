package vmware.services.gateway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protege o application.yml do gateway. No cluster as rotas vem do discovery locator
 * (/employee/**, /department/**, /organization/**): sem ele o gateway sobe, mas responde 404 em tudo.
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
    void discoveryLocatorIsEnabledWithLowerCaseServiceIds() {
        assertThat(properties.getProperty("spring.cloud.gateway.server.webflux.discovery.locator.enabled"))
                .isEqualTo("true");
        assertThat(properties.getProperty("spring.cloud.gateway.server.webflux.discovery.locator.lower-case-service-id"))
                .isEqualTo("true");
    }

    @Test
    void kubernetesConfigIsImportedAsOptional() {
        assertThat(properties.getProperty("spring.config.import")).isEqualTo("optional:kubernetes:");
    }
}
