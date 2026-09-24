package com.backend.backend.config;

import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Defines the {@link RestClient} bean used to talk to the Python graph service.
 *
 * <p>Uses the JDK {@link HttpClient} with HTTP/1.1 forced, because uvicorn does not support the h2c
 * upgrade that the JDK client attempts by default.
 */
@Configuration
@EnableConfigurationProperties(GraphServiceProperties.class)
public class GraphServiceConfig {

  @Bean
  public RestClient graphServiceRestClient(GraphServiceProperties props) {
    HttpClient httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(props.connectTimeout())
            .build();

    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(props.readTimeout());

    return RestClient.builder().baseUrl(props.url()).requestFactory(factory).build();
  }
}
