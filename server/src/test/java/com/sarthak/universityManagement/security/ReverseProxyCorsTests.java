package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.config.MySqlTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReverseProxyCorsTests extends MySqlTestContainer {

    private static final String PUBLIC_HOST = "univ.example.com";

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Test
    void sameOriginLoginThroughTlsProxyReachesAuthentication() throws IOException, InterruptedException {
        HttpResponse<String> response = loginThroughProxy("https://" + PUBLIC_HOST);

        assertEquals(401, response.statusCode(), response.body());
    }

    @Test
    void crossOriginLoginThroughTlsProxyIsStillRejected() throws IOException, InterruptedException {
        HttpResponse<String> response = loginThroughProxy("https://evil.example.com");

        assertEquals(403, response.statusCode());
        assertEquals("Invalid CORS request", response.body());
    }

    private HttpResponse<String> loginThroughProxy(String origin) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/login"))
            .header("Content-Type", "application/json")
            .header("Origin", origin)
            .header("X-Forwarded-Proto", "https")
            .header("X-Forwarded-Host", PUBLIC_HOST)
            .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"nobody\",\"password\":\"wrong\"}"))
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
