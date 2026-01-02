package at.shiftcontrol.shiftservice.integration.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public abstract class RestITBase {
    @LocalServerPort
    protected int port;
    protected RestAssuredConfig config;

    public RestITBase() {
    }

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    public void setBasePath() {
        if (RestAssured.requestSpecification == null) {
            RestAssured.requestSpecification = (new RequestSpecBuilder())
                .setPort(this.port)
                .setAccept(this.getRestAssuredAcceptContentType())
                .setContentType(this.getRestAssuredContentType())
                .build();
            this.config = RestAssured.config.encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset(this.defaultCharset()));
        }
        RestAssured.requestSpecification.basePath(this.getBasePath());
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    public void setThreadNameToTest() {
        Thread var10000 = Thread.currentThread();
        long var10001 = Thread.currentThread().getId();
        var10000.setName(var10001 + "-" + this.getClass().getSimpleName());
    }

    public String getBasePath() {
        return "/api/v1/";
    }

    protected int getPort() {
        return this.port;
    }

    protected ContentType getRestAssuredContentType() {
        return ContentType.JSON;
    }

    protected ContentType getRestAssuredAcceptContentType() {
        return ContentType.ANY;
    }

    protected String defaultCharset() {
        return "UTF-8";
    }

    public <T> T getRequest(final String uri, final Class<T> expectedObject) {
        return this.doRequest(Method.GET, uri, "", 200, expectedObject);
    }

    public <T> T getRequest(final String uri, final Map<String, String> params, final Class<T> expectedObject) {
        return this.doRequest(Method.GET, uri, "", params, 200, expectedObject);
    }

    public <T> List<T> getRequestList(final String uri, final Class<T> expectedObject) {
        return this.doRequestList(Method.GET, uri, "", 200, expectedObject);
    }

    public <T> List<T> getRequestList(final String uri, final Map<String, String> params, final Class<T> expectedObject) {
        return this.doRequestList(Method.GET, uri, "", params, 200, expectedObject);
    }

    public <T> T putRequest(final String uri, final Object body, final Class<T> expectedObject) {
        return doRequest(Method.PUT, uri, body, 200, expectedObject);
    }

    public <T> T postRequest(final String uri, final Object body, final Class<T> expectedObject) {
        return doRequest(Method.POST, uri, body, 200, expectedObject);
    }

    public <T> T deleteRequest(final String uri) {
        return doRequest(Method.DELETE, uri, "", 200, null);
    }

    public <T> T deleteRequest(final String uri, final Class<T> expectedObject) {
        return doRequest(Method.DELETE, uri, "", 200, expectedObject);
    }

    public <T> T doRequest(final Method method, final String url, final Object body, final int expectedStatusCode, final Class<T> expectedObject) {
        return doRequest(method, url, body, new HashMap<>(), expectedStatusCode, expectedObject);
    }

    public <T> List<T> doRequestList(final Method method, final String url, final Object body, final int expectedStatusCode, final Class<T> elementType) {
        return doRequestList(method, url, body, new HashMap<>(), expectedStatusCode, elementType);
    }

    public <T> T doRequest(final Method method, final String url, final Object body, final Map<String, String> params, final int expectedStatusCode,
                           final Class<T> expectedObject) {
        if (expectedObject == null) {
            RestAssured.given()
                .body(body)
                .params(params)
                .request(method, url)
                .then()
                .statusCode(expectedStatusCode);
            return null; // Return null for void methods
        } else if (Iterable.class.isAssignableFrom(expectedObject)) {
            throw new IllegalArgumentException("Use doRequestList for Iterable types");
        }

        return RestAssured.given()
            .body(body)
            .params(params)
            .request(method, url)
            .then()
            .statusCode(expectedStatusCode)
            .extract()
            .body()
            .as(expectedObject);
    }

    public <T> List<T> doRequestList(final Method method, final String url, final Object body,
                                     final Map<String, String> params, final int expectedStatusCode, final Class<T> elementType) {
        return RestAssured.given()
            .body(body)
            .params(params)
            .request(method, url)
            .then()
            .statusCode(expectedStatusCode)
            .extract()
            .body()
            .jsonPath()
            .getList(".", elementType); // Explicitly convert LinkedHashMap to T
    }

    public void doRequestWithoutResponseContent(final Method method, final String url, final Object body) {
        RestAssured.given()
            .body(body)
            .request(method, url, new Object[0])
            .then()
            .statusCode(204);
    }

    public byte[] doFileRequestWithoutResponseContent(final Method method, final String url, final Map<String, String> params, final Object body) {
        return RestAssured.given()
            .body(body)
            .params(params)
            .request(method, url, new Object[0])
            .then()
            .statusCode(200)
            .contentType(ContentType.BINARY)
            .extract()
            .asByteArray();
    }

    // note: if more advanced assertions are needed, a custom matcher should be created (instead of just checking the detail like here)
    public void doRequestAndAssertMessage(final Method method, final String url, final Object body, final int expectedStatusCode, final String message) {
        doRequestAndAssertMessage(method, url, body, expectedStatusCode, message, true);
    }

    public void doRequestAndAssertMessage(final Method method, final String url, final Object body, final int expectedStatusCode, final String message,
                                          final boolean matchDetailExactly) {
        doRequestAndAssertMessage(method, url, body, new HashMap<>(), expectedStatusCode, message, matchDetailExactly);
    }

    public void doRequestAndAssertMessage(final Method method, final String url, final Object body, final Map<String, String> params,
                                          final int expectedStatusCode, final String message) {
        doRequestAndAssertMessage(method, url, body, params, expectedStatusCode, message, true);
    }

    public void doRequestAndAssertMessage(final Method method, final String url, final Object body, final Map<String, String> params,
                                          final int expectedStatusCode, final String message, final boolean matchDetailExactly) {
        String actual = RestAssured.given()
            .body(body)
            .params(params)
            .request(method, url, new Object[0])
            .then()
            .statusCode(expectedStatusCode)
            .extract()
            .asString();

        if (matchDetailExactly) {
            assertThat(actual).isEqualTo(message);
        } else {
            assertThat(actual).contains(message);
        }
    }
}
