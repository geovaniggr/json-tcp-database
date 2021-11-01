package br.com.usp.mongusp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionCache {

    private static final Map<String, Database> cache = new ConcurrentHashMap<>();

    /**
     * Dado que podemos ter varias coleções diferentes, teremos um hashmap
     * que armazenará o nome da coleção que já foi carregada em memória
     * e uma instância singleton do manipulador daquela coleção
     */
    public static Database getCollectionInstance(String collection) throws IOException {
        if(cache.containsKey(collection)){
           return cache.get(collection);
        }

        var db = new Database(collection);
        db.init();

        cache.put(collection, db);

        return db;
    }
}
