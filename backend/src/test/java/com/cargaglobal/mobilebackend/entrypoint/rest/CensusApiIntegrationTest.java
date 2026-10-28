package com.cargaglobal.mobilebackend.entrypoint.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:census_tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CensusApiIntegrationTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();
    private String token;
    private static final String CAPTURE = """
            {"name":"Ana","lastName":"López","totalMembers":4,"televisionCount":2,
             "latitude":14.6349,"longitude":-90.5069,"origin":"MASIVO","censistId":999}
            """;

    @BeforeEach
    void registerCensist() throws Exception {
        jdbc.update("DELETE FROM liveness_sessions");
        jdbc.update("DELETE FROM censuses");
        jdbc.update("DELETE FROM users");
        var registered = request("POST", "/api/v1/auth/register", null, """
                {"name":"Ana","lastName":"López","username":"ana","email":"ana@example.com",
                 "password":"prueba123","gender":"Femenino","birthDate":"2000-01-10","role":"ADMINISTRADOR"}
                """);
        assertThat(registered.status()).isEqualTo(201);
        assertThat(registered.body().path("body").path("role").asText()).isEqualTo("CENSISTA");
        token = request("POST", "/api/v1/auth/login", null,
                "{\"username\":\"ana\",\"password\":\"prueba123\"}").body().path("body").path("token").asText();
        assertThat(token).isNotBlank();
    }

    @Test
    void protectsCaptureAndMapsWithRealJwtAndPreservesRolesOnProfileUpdates() throws Exception {
        assertThat(request("GET", "/api/v1/censuses", null, null).status()).isEqualTo(401);
        assertThat(request("GET", "/api/v1/censuses", token, null).status()).isEqualTo(403);
        var saved = request("POST", "/api/v1/censuses", token, CAPTURE);
        assertThat(saved.status()).isEqualTo(201);
        assertThat(saved.body().path("body").path("origin").asText()).isEqualTo("NORMAL");
        long userId = jdbc.queryForObject("SELECT id FROM users WHERE username='ana'", Long.class);
        assertThat(saved.body().path("body").path("censistId").asLong()).isEqualTo(userId);

        jdbc.update("UPDATE users SET role='ADMINISTRADOR' WHERE username='ana'");
        assertThat(request("POST", "/api/v1/censuses", token, CAPTURE).status()).isEqualTo(403);
        assertThat(request("GET", "/api/v1/censuses", token, null).status()).isEqualTo(200);
        var profile = request("PUT", "/api/v1/profile", token, """
                {"name":"Ana","lastName":"López","email":"ana@example.com","gender":"Femenino",
                 "birthDate":"2000-01-10","phone":"5555-5555","avatarBase64":"aGVsbG8=","role":"CENSISTA"}
                """);
        assertThat(profile.status()).isEqualTo(200);
        assertThat(profile.body().path("body").path("role").asText()).isEqualTo("ADMINISTRADOR");
        assertThat(request("GET", "/api/v1/profile", token, null).body().path("body").path("avatarBase64").asText()).isEqualTo("aGVsbG8=");
    }

    @Test
    void registrationAcceptsOptionalPhoneAndRejectsInvalidPhone() throws Exception {
        assertThat(request("GET", "/api/v1/profile", token, null).body().path("body").path("phone").isNull()).isTrue();
        String payload = """
                {"name":"Luis","lastName":"Pérez","username":"luis","email":"luis@example.com",
                 "password":"prueba123","gender":"Masculino","birthDate":"2000-01-10","phone":"%s"}
                """;
        assertThat(request("POST", "/api/v1/auth/register", null, payload.formatted("abc1234567")).status()).isEqualTo(400);
        var registered = request("POST", "/api/v1/auth/register", null, payload.formatted("+502 5555-1234"));
        assertThat(registered.status()).isEqualTo(201);
        assertThat(registered.body().path("body").path("phone").asText()).isEqualTo("+502 5555-1234");
        assertThat(jdbc.queryForObject("SELECT phone FROM users WHERE username='luis'", String.class)).isEqualTo("+502 5555-1234");
    }

    @Test
    void onlyAdminCanSelectRolesAndPublicRegistrationCannotEscalate() throws Exception {
        String user = """
            {"user":{"name":"Carlos","lastName":"Pérez","username":"carlos","email":"carlos@example.com",
             "password":"prueba123","gender":"Masculino","birthDate":"2000-01-10"}, "role":"ADMINISTRADOR"}
            """;
        assertThat(request("POST","/api/v1/admin/users",null,user).status()).isEqualTo(401);
        assertThat(request("POST","/api/v1/admin/users",token,user).status()).isEqualTo(403);
        jdbc.update("UPDATE users SET role='ADMINISTRADOR' WHERE username='ana'");
        var created=request("POST","/api/v1/admin/users",token,user);
        assertThat(created.status()).isEqualTo(201);
        assertThat(created.body().path("body").path("role").asText()).isEqualTo("ADMINISTRADOR");
        var adminLogin=request("POST","/api/v1/auth/login",null,"{\"username\":\"carlos\",\"password\":\"prueba123\"}");
        assertThat(adminLogin.status()).isEqualTo(200);
        String newAdminToken=adminLogin.body().path("body").path("token").asText();
        assertThat(request("GET","/api/v1/censuses",newAdminToken,null).status()).isEqualTo(200);
        assertThat(request("POST","/api/v1/censuses",newAdminToken,CAPTURE).status()).isEqualTo(403);
        assertThat(request("POST","/api/v1/admin/users",token,user).status()).isEqualTo(409);
        var censist=request("POST","/api/v1/admin/users",token,user.replace("carlos","maria").replace("ADMINISTRADOR","CENSISTA"));
        assertThat(censist.status()).isEqualTo(201);
        assertThat(censist.body().path("body").path("role").asText()).isEqualTo("CENSISTA");
        var censistLogin=request("POST","/api/v1/auth/login",null,"{\"username\":\"maria\",\"password\":\"prueba123\"}");
        assertThat(censistLogin.status()).isEqualTo(200);
        String newCensistToken=censistLogin.body().path("body").path("token").asText();
        assertThat(request("GET","/api/v1/censuses",newCensistToken,null).status()).isEqualTo(403);
        assertThat(request("POST","/api/v1/censuses",newCensistToken,CAPTURE).status()).isEqualTo(201);
        assertThat(request("POST","/api/v1/admin/users",token,user.replace("ADMINISTRADOR","SUPERUSER")).status()).isEqualTo(400);
        assertThat(request("GET","/api/v1/profile",token,null).body().path("body").path("username").asText()).isEqualTo("ana");
    }

    @Test
    void livenessRequiresLoginAndFailsClosedWithoutAWS() throws Exception {
        assertThat(request("GET","/api/v1/liveness/config",null,null).status()).isEqualTo(401);
        assertThat(request("GET","/api/v1/liveness/config",token,null).body().path("body").path("configured").asBoolean()).isFalse();
        assertThat(request("POST","/api/v1/liveness/sessions",token,"{\"consent\":false}").status()).isEqualTo(400);
        assertThat(request("POST","/api/v1/liveness/sessions",token,"{\"consent\":true}").status()).isEqualTo(503);
        assertThat(request("GET","/api/v1/liveness/sessions/unknown",token,null).status()).isEqualTo(404);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM liveness_sessions",Integer.class)).isZero();
    }

    @Test
    void validatesRequiredCountsAndCoordinates() throws Exception {
        for (String invalid : new String[] {
                CAPTURE.replace("\"totalMembers\":4", "\"totalMembers\":0"),
                CAPTURE.replace("\"televisionCount\":2", "\"televisionCount\":-1"),
                CAPTURE.replace("14.6349", "91"),
                CAPTURE.replace("-90.5069", "-181"),
                CAPTURE.replace("\"latitude\":14.6349", "\"latitude\":null")}) {
            assertThat(request("POST", "/api/v1/censuses", token, invalid).status()).isEqualTo(400);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM censuses", Integer.class)).isZero();
    }

    @Test
    void usesDescriptiveConsistentEnvelopesWithoutChangingAuthorizationOrData() throws Exception {
        var unauthenticated=request("GET","/api/v1/censuses",null,null);
        assertThat(unauthenticated.status()).isEqualTo(401);
        assertThat(unauthenticated.body().path("httpCode").asInt()).isEqualTo(401);
        assertThat(unauthenticated.body().path("message").asText()).isEqualTo("Token requerido o inválido");
        assertThat(unauthenticated.body().path("timestamp").asText()).isNotBlank();
        var forbidden=request("GET","/api/v1/censuses",token,null);
        assertThat(forbidden.status()).isEqualTo(403);
        assertThat(forbidden.body().path("httpCode").asInt()).isEqualTo(403);
        assertThat(forbidden.body().path("timestamp").asText()).isNotBlank();
        var profile=request("GET","/api/v1/profile",token,null);
        assertThat(profile.status()).isEqualTo(200);
        assertThat(profile.body().path("message").asText()).isEqualTo("Perfil consultado correctamente");
        assertThat(profile.body().path("body").path("username").asText()).isEqualTo("ana");
        var config=request("GET","/api/v1/liveness/config",token,null);
        assertThat(config.body().path("message").asText()).isEqualTo("Configuración de prueba facial consultada");
        jdbc.update("UPDATE users SET role='ADMINISTRADOR' WHERE username='ana'");
        var listed=request("GET","/api/v1/censuses",token,null);
        assertThat(listed.status()).isEqualTo(200);
        assertThat(listed.body().path("httpCode").asInt()).isEqualTo(200);
        assertThat(listed.body().path("message").asText()).isEqualTo("Censos consultados correctamente");
        assertThat(listed.body().path("body").path("items").size()).isZero();
        assertThat(listed.body().path("body").path("hasMore").asBoolean()).isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM censuses",Integer.class)).isZero();
    }

    @Test
    void paginatesAllCensusesAndFiltersMassiveOrigin() throws Exception {
        request("POST", "/api/v1/censuses", token, CAPTURE);
        for (int i = 0; i < 3; i++) jdbc.update("""
                INSERT INTO censuses(name,last_name,total_members,television_count,latitude,longitude,origin,created_at)
                VALUES ('Prueba','Masiva',3,1,14.63,-90.5,'MASIVO',CURRENT_TIMESTAMP)
                """);
        jdbc.update("UPDATE users SET role='ADMINISTRADOR' WHERE username='ana'");
        var first = request("GET", "/api/v1/censuses?limit=2", token, null).body().path("body");
        assertThat(first.path("items").size()).isEqualTo(2);
        assertThat(first.path("hasMore").asBoolean()).isTrue();
        long cursor = first.path("nextCursor").asLong();
        var second = request("GET", "/api/v1/censuses?limit=2&afterId=" + cursor, token, null).body().path("body");
        assertThat(second.path("items").size()).isEqualTo(2);
        assertThat(second.path("items").get(0).path("id").asLong()).isGreaterThan(cursor);
        assertThat(second.path("hasMore").asBoolean()).isFalse();
        assertThat(request("GET", "/api/v1/censuses?origin=MASIVO", token, null).body().path("body").path("items").size()).isEqualTo(3);
        assertThat(request("GET", "/api/v1/censuses?origin=NORMAL", token, null).body().path("body").path("items").size()).isEqualTo(1);
        assertThat(request("GET", "/api/v1/censuses?limit=501", token, null).status()).isEqualTo(400);
        assertThat(request("GET", "/api/v1/censuses?origin=OTRO", token, null).status()).isEqualTo(400);
    }

    private TestResponse request(String method, String path, String jwt, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (jwt != null) builder.header("Authorization", "Bearer " + jwt);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new TestResponse(response.statusCode(), json.readTree(response.body()));
    }
    private record TestResponse(int status, JsonNode body) {}
}
