package apiTests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import static org.hamcrest.Matchers.*;

public class BookingTests {
    String accessToken;
    int bookingID;

    //1. Login (Create token one time in a class as precondition)

    @BeforeMethod

    public void setupPreCondition_accessTokenLogin()
    {
        String authorizationEndpoint = "https://restful-booker.herokuapp.com/auth";
        String body =
                """
                        {
                             "username" : "admin",
                             "password" : "password123"
                         } """;

        var responseToValidate = given().body(body).header("Content-Type", "application/json")
                .log().all().when().post(authorizationEndpoint)
                .then().extract().response();
        JsonPath jsonPath = responseToValidate.jsonPath();
        accessToken = jsonPath.getString("token");
        System.out.println(accessToken);
    }

//    2. Create booking in test method with the suitable assertions

    @Test(priority = 0)

    public void testCreateBooking()
    {
        String bookingEndpoint = "https://restful-booker.herokuapp.com/booking";
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
                } """;
        var responseToValidate = given().header("Content-Type", "application/json")
                .header("Accept", "application/json").body(body)
                .log().all().when().post(bookingEndpoint).then();

        responseToValidate.body("booking.firstname", equalTo("Jim"))
                .body("booking.lastname", equalTo("Brown"))
                .body("booking.totalprice", equalTo(111))
                .statusCode(200);
        Response response = responseToValidate.extract().response();
        JsonPath jsonPath = response.jsonPath();
        bookingID = jsonPath.getInt("bookingid");
        responseToValidate.log().all();
    }

//    3. Edit the booking created in step 2 in test method with the suitable assertions

    @Test(priority = 1)

    public void testEditBooking()
    {
        String bookingEndpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        String body = """
                {
                    "firstname": "James",
                    "lastname": "Brown",
                    "totalprice": 111,
                    "depositpaid": true,
                    "bookingdates": {
                     "checkin": "2018-01-01",
                     "checkout": "2019-01-01"
                    },
                "additionalneeds": "Breakfast"
                } """;

        var responseToValidate = given().header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .header("Cookie", "token=" + accessToken)
                .body(body)
                .log().all().when()
                .put(bookingEndpoint).then();

        responseToValidate.body("firstname", equalTo("James"))
                .body("bookingdates.size()", greaterThanOrEqualTo(0))
                .statusCode(200);
    }

    //4. Get the booking created in step 2 in test method with the suitable assertions

    @Test(priority = 2)
    public void testGetBooking()
    {
        String bookingEndpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var responseToValidate = given().header("Content-Type", "application/json")
                .when().log().all()
                .get(bookingEndpoint)
                .then();
        responseToValidate.body("lastname", equalTo("Brown"))
                .body("totalprice", equalTo(111))
                .statusCode(200);
    }

    //5. Delete the booking created in step 2 in test method with the suitable assertions

    @Test(priority = 3)
    public void testDeleteBooking()
    {
        String bookingEndpoint = "https://restful-booker.herokuapp.com/booking/" + bookingID;
        var responseToValidate = given().header("Content-Type", "application/json")
                .header("Cookie", "token=" + accessToken)
                .when().log().all()
                .delete(bookingEndpoint)
                .then();
        responseToValidate.statusCode(201);
        Response response = responseToValidate.extract().response();
        Assert.assertEquals(response.asString(), "Created");
    }
}
