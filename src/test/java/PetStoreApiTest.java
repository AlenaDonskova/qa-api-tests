import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


public class PetStoreApiTest {

    private int currentPetId;
    private String currentPetName;
    private String currentPetStatus;
    private static final String BASE_URL = "https://petstore.swagger.io/v2";
    private static final String basePathPet = "/pet";

    @BeforeEach
    void setUp() {
        currentPetId = (int) (Math.random() * 10000);
        currentPetName = "PreConditionDog " + currentPetId;
        currentPetStatus = "PreConditionStatus " + currentPetId;

        Map<String, Object> petData = new HashMap<>();
        petData.put("id", currentPetId);
        petData.put("name", currentPetName);
        petData.put("status", currentPetStatus);

        given().contentType(ContentType.JSON).body(petData)
                .when().post(BASE_URL + basePathPet)
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void shouldCreatePetSuccessfully() {
        Map<String, Object> petData = new HashMap<>();
        petData.put("id", 0);
        petData.put("name", "Betty");
        petData.put("status", "available");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(petData)
                .when()
                .post(BASE_URL + basePathPet);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.jsonPath().getString("name")).isEqualTo("Betty");
        assertThat(response.jsonPath().getString("status")).isEqualTo("available");
    }

    @Test
    public void shouldGetExistingPetById() {
        Response response = given()
                .when()
                .get(BASE_URL + basePathPet + "/" + currentPetId);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(currentPetId);
        assertThat(response.jsonPath().getString("name")).isEqualTo(currentPetName);
        assertThat(response.jsonPath().getString("status")).isEqualTo(currentPetStatus);
    }

    @Test
    public void shouldGetExistingPetByStatus() {
        Response response = given()
                .when()
                .get(BASE_URL + "/pet/findByStatus?status=" + currentPetStatus);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
        List<Map<String, Object>> pets = response.jsonPath().getList("$");
        Map<String, Object> petFound = pets.stream()
                .filter(pet -> ((Number) pet.get("id")).intValue() == currentPetId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Питомец не найден!"));

        assertThat(petFound.get("name")).isEqualTo(currentPetName);
        assertThat(petFound.get("status")).isEqualTo(currentPetStatus);
    }

    @Test
    public void shouldDeleteExistingPet() {
        Response response = given()
                .when()
                .delete(BASE_URL + basePathPet + "/" + currentPetId);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void shouldReturnErrorForInvalidJson() {
        String brokenJson = "{\"name\": \"Barsik\", \"status\": ";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(brokenJson)
                .when()
                .post(BASE_URL + basePathPet);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldDeleteNotExistingPet() {
        int nonExistingId = (int) (Math.random() * 10000000) + 1000000;

        Response response = given()
                .when()
                .delete(BASE_URL + basePathPet + "/" + nonExistingId);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }
}
