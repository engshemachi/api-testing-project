import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RestfulBookerApiTesting {
    String accessToken;
    int targetBookingId;

    @BeforeClass
    public void loginToAppAsPreCondition() {
        String endPoint = "https://restful-booker.herokuapp.com/auth";
        String body = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }""";
        ValidatableResponse response = given().body(body).header("Content-Type", "application/json")
                .when().post(endPoint).then();

        Response responseToBeExtracted = response.extract().response();
        JsonPath jsonPath = responseToBeExtracted.jsonPath();
        accessToken = jsonPath.getString("token");
        System.out.println("access token = " + accessToken);
    }

    @Test(priority = 0)
    public void testCreateBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking";
        String body = """
                {
                    "firstname" : "Jim",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }""";

        ValidatableResponse response = given().body(body).header("Content-Type", "application/json")
                .log().all().when().post(endpoint).then();
        Response responseToBeExtracted = response.extract().response();
        JsonPath jsonPath = responseToBeExtracted.jsonPath();
        targetBookingId = jsonPath.getInt("bookingid");
        System.out.println("Booking ID = " + targetBookingId);

//Validations on API response
        response.statusCode(200);
        response.assertThat().header("Content-Type", equalTo("application/json; charset=utf-8"))
        .body("booking.firstname", equalTo("Jim"))
        .body("booking.lastname", equalTo("Brown"))
        .body("booking.totalprice", equalTo(111))
        .body("booking.depositpaid", equalTo(true))
        .body("booking.bookingdates.checkin", equalTo("2018-01-01"))
        .body("booking.bookingdates.checkout", equalTo("2019-01-01"));
    }

    @Test(priority = 1)
    public void testUpdateBookingInfo() {

        String cookie = "token=" + accessToken;
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + targetBookingId;
        System.out.println("endpoint = " + endpoint);
        System.out.println("cookie : " + cookie);
        String body = """
                {
                    "firstname" : "James",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }""";
        ValidatableResponse response = given().body(body).header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Cookie", cookie)
                .when().put(endpoint).then();

        //Validations on API response
        response.assertThat().body("firstname", equalTo("James"));
        response.statusCode(200);
    }

    @Test(priority = 2)
    public void testGetBookingInfo() {

        String endpoint = "https://restful-booker.herokuapp.com/booking/" + targetBookingId;

        ValidatableResponse response = given().header("Content-Type", "application/json")
                .when().get(endpoint).then();

        //Validations on API response
        response.statusCode(200);
        response.assertThat().header("Content-Type", equalTo("application/json; charset=utf-8"))
        .body("firstname", equalTo("James"))
        .body("lastname", equalTo("Brown"))
        .body("totalprice", equalTo(111))
        .body("depositpaid", equalTo(true))
        .body("bookingdates.checkin", equalTo("2018-01-01"))
        .body("bookingdates.checkout", equalTo("2019-01-01"));
    }

    @Test(priority = 3)
    public void testDeleteBooking() {

        String endpoint = "https://restful-booker.herokuapp.com/booking/" + targetBookingId;
        ValidatableResponse response = given().header("Content-Type", "application/json")
                .header("cookie", "token=" + accessToken).when().delete(endpoint).then();

        //Validations on API response
        Response responseToBeExtracted = response.extract().response();
        Assert.assertEquals(responseToBeExtracted.asString(), "Created");
    }
}
