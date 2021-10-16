package br.com.usp.mongusp;

import com.google.gson.*;

import java.io.IOException;


public class Main {

    private static final Gson parser = new Gson();
    public static void main(String[] args) throws IOException {
        var server = new Server();
        server.init();

//        var firstRequest = parser.fromJson("""
//            {
//                "method": "CREATE_COLLECTION",
//                "key": "nome",
//                "value": "Geovani"
//            }
//        """, RequestInput.class);
//
//
//        var secondRequest = parser.fromJson("""
//            {
//                "type": "set",
//                "key": [ "disciplinas", "matematica", "nota" ],
//                "value": "9.75"
//            }
//        """, RequestInput.class);
//
//
//        var thirdRequest = parser.fromJson("""
//            {
//                "type": "set",
//                "key": "nome",
//                "value": "Geovani Granieri"
//            }
//        """, RequestInput.class);
//
//        var fourthRequest = parser.fromJson("""
//            {
//                "type": "set",
//                "key": [ "disciplinas", "geografia", "nota" ],
//                "value": "7.75"
//            }
//        """, RequestInput.class);
//
//        var fifthRequest = parser.fromJson("""
//            {
//                "type": "set",
//                "key": [ "disciplinas", "geografia", "nota" ],
//                "value": "8.75"
//            }
//        """, RequestInput.class);
//
//        var sixthRequest = parser.fromJson("""
//            {
//                "type": "get",
//                "key": [ "disciplinas", "geografia", "nota" ],
//                "value": "8.75"
//            }
//        """, RequestInput.class);
//
//        var seventhRequest = parser.fromJson("""
//            {
//                "type": "get",
//                "key": [ "disciplinas", "geografia", "nota" ],
//                "value": "8.75"
//            }
//        """, RequestInput.class);
//
//        var eightyRequest = parser.fromJson("""
//            {
//                "type": "get",
//                "key": "nome",
//                "value": "8.75"
//            }
//        """, RequestInput.class);
//
//        BiConsumer<Database, RequestInput> addOrUpdate = (database, request) -> database.addOrUpdate(request.getKey(), request.getValue());
//        BiConsumer<Database, RequestInput> remove = (database, request) -> database.removeElement(request.getKey());
//        BiFunction<Database, RequestInput, Object> get = (database, request) -> database.get(request.getKey());
//
//        Consumer<RequestInput> partialAdd = (requestInput) -> addOrUpdate.accept(db, requestInput);

//        partialAdd.accept(firstRequest);
//        partialAdd.accept(secondRequest);
//        partialAdd.accept(thirdRequest);
//        partialAdd.accept(fourthRequest);
//        partialAdd.accept(fifthRequest);

        //7989

//        remove.accept(db, seventhRequest);
    }
}
