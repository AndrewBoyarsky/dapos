package com.boyarsky.dapos.web;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NodeProxyClient {
    private String baseUrl;
    private HttpClient client;
    private int nodePort;

    private NodeProxyClient(@Value("${tendermint.node.port}") int nodePort) {
        this.nodePort = nodePort;
    }

    @PostConstruct
    public void init() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public String sendRequest(String path, Map<String, String> params) throws IOException, InterruptedException, URISyntaxException {
        String preparedParameters = params.entrySet().stream().map(e -> e.getKey() + "=" + encodeValue(e.getValue())).collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:" + nodePort + "/" + path + "?" + preparedParameters)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        return response.body();
    }

    @SneakyThrows
    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

}
