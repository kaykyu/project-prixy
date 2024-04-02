package vttp.project.app.backend.exception;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class SqlOrdersException extends Exception {
    
    public SqlOrdersException() {
        super();
    }

    public SqlOrdersException(String message) {
        super(message);
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder().add("error", this.getMessage()).build();
    }
}
