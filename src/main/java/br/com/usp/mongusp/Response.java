package br.com.usp.mongusp;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class Response {

    @Expose
    private ResponseStatus response;

    @Expose
    private String reason;

    @Expose
    private JsonElement value;

    public ResponseStatus getResponse() {
        return response;
    }

    public void setResponse(ResponseStatus response) {
        this.response = response;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public JsonElement getValue() {
        return value;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }
}
