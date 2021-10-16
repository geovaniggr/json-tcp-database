package br.com.usp.mongusp;

import java.util.regex.Pattern;

public class Request{
    private final String collection;
    private final String method;
    private final String value;

    private final Pattern pattern = Pattern.compile("(\\w+)\\.(\\w+)\\((.+)\\)");

    Request(String rawRequest){
        var splitRequest  = pattern.matcher(rawRequest);

        if(splitRequest.find()){
            collection = splitRequest.group(1);
            method = splitRequest.group(2);
            value = splitRequest.group(3);
        } else {
            collection = "DEFAULT";
            method = "";
            value = null;
        }
    }

    @Override
    public String toString() {
        return """
            Request = {
                "collection": "%s",
                "method": "%s",
                "value": "%s"
            }
        """.formatted(collection, method, value);
    }
}
