package com.cargaglobal.mobilebackend.entrypoint.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:location_tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LocationApiIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void authenticatesValidatesPersistsAndSeparatesLocationsByUser() throws Exception {
        assertThat(request("GET", "/api/v1/locations", null, null).status()).isEqualTo(401);
        assertThat(request("GET", "/api/v1/locations", "invalid", null).status()).isEqualTo(401);
        String ana = registerAndLogin("ana");
        String luis = registerAndLogin("luis");
        assertThat(request("GET", "/api/v1/locations", ana, null).body().path("body").size()).isZero();
        String payload = """
                {"name":" Universidad ","description":" Clase de desarrollo móvil ",
                 "latitude":14.6349,"longitude":-90.5069,"userId":999}
                """;
        assertThat(request("POST", "/api/v1/locations", null, payload).status()).isEqualTo(401);
        for (String invalid : new String[] {
                payload.replace(" Universidad ", " "),
                payload.replace(" Clase de desarrollo móvil ", " "),
                payload.replace("14.6349", "91"),
                payload.replace("-90.5069", "-181"),
                payload.replace("14.6349", "null")}) {
            assertThat(request("POST", "/api/v1/locations", ana, invalid).status()).isEqualTo(400);
        }
        var saved = request("POST", "/api/v1/locations", ana, payload);
        assertThat(saved.status()).isEqualTo(201);
        var item = saved.body().path("body");
        assertThat(item.path("name").asText()).isEqualTo("Universidad");
        assertThat(item.path("description").asText()).isEqualTo("Clase de desarrollo móvil");
        assertThat(item.path("userId").asLong()).isNotEqualTo(999);
        assertThat(item.path("createdAt").asText()).isNotBlank();
        var own = request("GET", "/api/v1/locations", ana, null).body().path("body");
        assertThat(own.size()).isEqualTo(1);
        assertThat(own.get(0).path("id").asLong()).isEqualTo(item.path("id").asLong());
        assertThat(own.get(0).path("latitude").asDouble()).isEqualTo(14.6349);
        assertThat(request("GET", "/api/v1/locations", luis, null).body().path("body").size()).isZero();
        // Un parámetro ajeno no cambia el usuario obtenido del JWT.
        assertThat(request("GET", "/api/v1/locations?userId=" + item.path("userId").asLong(), luis, null)
                .body().path("body").size()).isZero();
        assertThat(request("POST", "/api/v1/locations", luis, payload.replace("Universidad", "Biblioteca")).status()).isEqualTo(201);
        assertThat(request("GET", "/api/v1/locations", ana, null).body().path("body").size()).isEqualTo(1);
    }

    private String registerAndLogin(String username) throws Exception {
        String registration = """
                {"name":"Estudiante","lastName":"Prueba","username":"%s","email":"%s@example.com",
                 "password":"prueba123","gender":"Masculino","birthDate":"2000-01-10"}
                """.formatted(username, username);
        assertThat(request("POST", "/api/v1/auth/register", null, registration).status()).isEqualTo(201);
        var login = request("POST", "/api/v1/auth/login", null,
                "{\"username\":\"" + username + "\",\"password\":\"prueba123\"}");
        assertThat(login.status()).isEqualTo(200);
        return login.body().path("body").path("token").asText();
    }

    private TestResponse request(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new TestResponse(response.statusCode(), json.readTree(response.body()));
    }
    private record TestResponse(int status, JsonNode body) {}
}
