package example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class APITest {

    private static final String BASE_URL = "https://bfhldevapigw.healthrx.co.in/automation-campus/create/user";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void createValidUser() {
        String requestBody = "{\n" +
                "  \"firstName\": \"John\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"phoneNumber\": 1234567890,\n" +
                "  \"emailId\": \"john.doe@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200);
    }

    @Test
    public void createUserWithDuplicatePhoneNumber() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Jane\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"phoneNumber\": 1234567890,\n" +
                "  \"emailId\": \"jane.doe@example.com\"\n" +
                "}";

        // First create a user
        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(200);

        // Now try creating another user with the same phone number
        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createUserWithoutRollNumber() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Test\",\n" +
                "  \"lastName\": \"User\",\n" +
                "  \"phoneNumber\": 9999999999,\n" +
                "  \"emailId\": \"test.user@test.com\"\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(401);
    }

    @Test
    public void createUserWithMissingFields() {
        String requestBody = "{\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"phoneNumber\": 9876543210,\n" +
                "  \"emailId\": \"missing.field@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createUserWithInvalidEmail() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Invalid\",\n" +
                "  \"lastName\": \"Email\",\n" +
                "  \"phoneNumber\": 1111111111,\n" +
                "  \"emailId\": \"invalid-email\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createUserWithInvalidPhoneNumber() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Invalid\",\n" +
                "  \"lastName\": \"Phone\",\n" +
                "  \"phoneNumber\": \"invalidphone\",\n" +
                "  \"emailId\": \"phone.invalid@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);
    }
    @Test
    public void createUserWithSQLInjection() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Robert'); DROP TABLE Users;--\",\n" +
                "  \"lastName\": \"Hacker\",\n" +
                "  \"phoneNumber\": 8888888888,\n" +
                "  \"emailId\": \"hacker@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);  // Expecting a bad request if the API correctly handles this input
    }

    @Test
    public void createUserWithXSSAttack() {
        String requestBody = "{\n" +
                "  \"firstName\": \"<script>alert('XSS')</script>\",\n" +
                "  \"lastName\": \"Attacker\",\n" +
                "  \"phoneNumber\": 7777777777,\n" +
                "  \"emailId\": \"attacker@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);  // Expecting a bad request if the API correctly handles this input
    }
    @Test
    public void createUserWithInvalidDataTypes() {
        String requestBody = "{\n" +
                "  \"firstName\": \"John\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"phoneNumber\": \"not-a-number\",\n" +
                "  \"emailId\": \"john.doe@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);  // Expecting a bad request due to invalid data type for phoneNumber
    }
    @Test
    public void testRateLimiting() {
        String requestBody = "{\n" +
                "  \"firstName\": \"Rate\",\n" +
                "  \"lastName\": \"Limiter\",\n" +
                "  \"phoneNumber\": 6666666666,\n" +
                "  \"emailId\": \"ratelimit@example.com\"\n" +
                "}";

        for (int i = 0; i < 10; i++) {
            Response response = given()
                    .header("roll-number", "1")
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post();

            // Check for rate limiting response (usually 429) after multiple requests
            if (i > 5) {  // Assuming the rate limit threshold is 5 requests
                response.then().statusCode(429);
            } else {
                response.then().statusCode(200);
            }
        }
    }
    @Test
    public void createUserWithExcessivelyLongInput() {
        String longName = "a".repeat(1000);  // Create a string with 1000 'a' characters

        String requestBody = "{\n" +
                "  \"firstName\": \"" + longName + "\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"phoneNumber\": 5555555555,\n" +
                "  \"emailId\": \"longinput@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);  // Expecting a bad request if the API handles long input correctly
    }
    @Test
    public void createUserWithSpecialCharacters() {
        String requestBody = "{\n" +
                "  \"firstName\": \"!@#$%^&*()_+{}|:\">?\",\n" +
                "  \"lastName\": \"<>&'\",\n" +
                "  \"phoneNumber\": 4444444444,\n" +
                "  \"emailId\": \"specialchar@example.com\"\n" +
                "}";

        given()
                .header("roll-number", "1")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400);  // Expecting a bad request due to special characters
    }

}
