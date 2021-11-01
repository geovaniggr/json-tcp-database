package br.com.usp.mongusp;

import com.google.gson.*;

import java.io.*;
import java.net.Socket;

public class RequestExecutor implements Runnable {

    private final Gson parser = new Gson();
    private final Socket socket;

    public RequestExecutor(Socket socket) throws IOException {
        this.socket = socket;
    }

    /*
        Utiliza a biblioteca do JSON para enviar uma resposta formatada para o usuário,
        por exemplo:
        ->  { "nome": "usuario", "email": "email@usp.br"}
        se torna:
        {
            "nome":  "usuario"
     */
    public String getPrettyPrint(Object object){
        return new GsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    /**
     *  O método run é executado quando recebemos uma requisição, e é responsável por algumas coisas:
     *  - Parsear a requisição para uma classe que representa a requisição (JSONRequest)
     *  - Obter o nome da coleção que o usuário quer manipular
     *  - Obter o método que utilizar na coleção
     *  - Passar para a classe Database (responsável por manipular a coleção em arquivo e em memória)
     *    os dados necessários (como por exemplo o id que será atualizado, e os novos dados)
     *  - Obter a resposta do Banco (por exemplo o elemento que foi inserido)
     *  - Enviar para o usuário uma resposta com o resultado da operação, e o resultado da operação
     */
    @Override
    public void run() {
        try(
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream());
        ){
            var request = parser.fromJson(input.readUTF(), JSONRequest.class);
            System.out.println(request);
            var database = CollectionCache.getCollectionInstance(request.getCollection());
            var response = new Response();

            var method = RequestMethodsAccepted.valueOf(request.getType().toUpperCase());

            try {
                switch (method){
                    case CREATE_COLLECTION -> {
                        response.setResponse(ResponseStatus.CREATED);
                        break;
                    }
                    case REMOVE_COLLECTION -> {
                        database.removeCollection(request.getKey().getAsString());
                        response.setResponse(ResponseStatus.OK);
                        break;
                    }
                    case LIST_ALL -> {
                        var result = database.getAll();
                        response.setValue(result);
                        response.setResponse(ResponseStatus.OK);
                        break;
                    }
                    case UPDATE -> {
                        database.update(request.getKey(), request.getValue());
                        response.setResponse(ResponseStatus.OK);
                        break;
                    }
                    case GET -> {
                        var element = database.get(request.getKey());
                        response.setValue(element);
                        response.setResponse(ResponseStatus.OK);
                        break;
                    }
                    case SET -> {
                        database.add(request.getValue());
                        response.setResponse(ResponseStatus.CREATED);
                        break;
                    }
                    case DELETE -> {
                        var element = database.get(request.getKey());
                        database.removeElement(request.getKey());
                        response.setResponse(ResponseStatus.OK);
                        response.setValue(element);
                        break;
                    }
                    default -> {
                        var result = createDefaultMessage("error", "Método não existente");
                        response.setValue(result);
                        response.setResponse(ResponseStatus.ERROR);
                        break;
                    }
                }

            }
            catch (IllegalArgumentException exception){
                var result = createDefaultMessage("reason", exception.getMessage());

                response.setValue(result);
                response.setResponse(ResponseStatus.ERROR);
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

    private JsonElement createDefaultMessage(String key, String message){
        return parser.fromJson("""
            {
                "%s": "%s"
            }
        """.formatted(key, message), JsonElement.class);
    }
}
