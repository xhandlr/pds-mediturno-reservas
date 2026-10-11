package cl.losandes.mediturno.reservas.gherkin;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;

public class Hooks {

    @Before
    public void configurarEscenario() {
        RestAssured.baseURI = System.getProperty(
                "mediturno.base-url",
                "http://localhost:8082"
        );

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @After
    public void limpiarEscenario() {
        RestAssured.reset();
    }
}
