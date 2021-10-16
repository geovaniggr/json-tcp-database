package br.com.usp.mongusp;

import com.google.gson.*;

import java.io.*;
import java.net.Socket;

public class RequestExecutor implements Runnable{

    private final Gson parser = new Gson();
    private final Socket socket;
    private final Database database;

    public RequestExecutor(Socket socket) throws IOException {
        this.socket = socket;
        this.database = Database.getInstance();
        this.database.init();
    }

    public String getPrettyPrint(Object object){
        return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    @Override
    public void run() {
        try(
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream());
        ){
            System.out.println("Request Recieved");
            var request = parser.fromJson(input.readUTF(), JSONRequest.class);
            var response = new Response();

            var method = RequestMethodsAccepted.valueOf(request.getType().toUpperCase());

            try {
                switch (method){
                    case CREATE_COLLECTION -> {
                        var result = parser.fromJson("""
                            {
                                "result": "Created"
                            }
                        """, JsonElement.class);

                        response.setValue(result);
                    }
                    case GET -> {
                        var element = database.get(request.getKey());
                        response.setValue(element);
                    }
                    case SET -> {
                        database.addOrUpdate(request.getKey(), request.getValue());
                        var element = database.get(request.getKey());
                        response.setValue(element);
                    }
                    case DELETE -> {
                        database.removeElement(request.getKey());
                        var element = database.get(request.getKey());
                        response.setValue(element);
                    }
                    default -> throw new IllegalArgumentException("Método Inexistente");
                }
                response.setResponse(Response.STATUS_OK);

            }
            catch (IllegalArgumentException exception){
                var result = parser.fromJson("""
                    {
                        "reason": "%s"
                    }
                """.formatted(exception.getMessage()), JsonElement.class);

                response.setValue(result);
                response.setResponse(Response.STATUS_ERROR);
            }
            catch ( Exception exception ){
                System.out.println(exception.getMessage());
                exception.printStackTrace();
            }
            output.writeUTF(getPrettyPrint(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
