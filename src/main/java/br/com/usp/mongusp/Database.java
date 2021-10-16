package br.com.usp.mongusp;

import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.*;

public class Database {

    private static final String FILENAME = "database.json";
    private static final Path PATH = Server.RESOURCES_PATH.resolve(FILENAME);

    private final Lock readLock;
    private final Lock writeLock;

    private JsonObject db;

    private static volatile Database instance;

    private Database() {
        var lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public static Database getInstance(){
        Database result = instance;

        if( result != null ){
            return result;
        }

        synchronized (Database.class) {
            if( instance == null ) {
                instance = new Database();
            }

            return instance;
        }
    }

    public void init() throws IOException {
        if (Files.exists(PATH)) {
            var collection = new String(Files.readAllBytes(PATH));
            db = new Gson().fromJson(collection, JsonObject.class);
        } else {
            Files.createFile(PATH);
            db = new JsonObject();
            write();
        }
    }

    public void addOrUpdate(JsonElement key, JsonElement value) {
        try {
            writeLock.lock();

            if (key.isJsonPrimitive()) {
                db.add(key.getAsString(), value);
            } else if (key.isJsonArray()) {
                var keys = key.getAsJsonArray();
                var newest = keys.remove(keys.size() - 1).getAsString();

                var toUpdate = getElement(keys, true);
                toUpdate.getAsJsonObject().add(newest, value);
            } else {
                throw new IllegalArgumentException("Invalid");
            }

            write();
        } finally {
            writeLock.unlock();
        }
    }

    public void removeElement(JsonElement key){
        try {
            writeLock.lock();
            if(key.isJsonPrimitive() && db.has(key.getAsString())){
                db.remove(key.getAsString());
            }

            else if (key.isJsonArray()) {
                var keys = key.getAsJsonArray();
                System.out.println(keys);
                var keyToBeRemoved = keys.remove(keys.size() - 1).getAsString();

                var object = getElement(keys, false);

                object.getAsJsonObject().remove(keyToBeRemoved);
            }
            else {
                throw new IllegalArgumentException("A chave precisa ser uma propriedade ou um array de propriedades");
            }
            write();
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        finally {
            writeLock.unlock();
        }
    }

    public JsonElement get(JsonElement key){
        try {

            var isPrimitive = key.isJsonPrimitive();
            var parsedKey = isPrimitive ? key.getAsString() : key.getAsJsonArray();
            readLock.lock();

            if(isPrimitive && db.has((String) parsedKey)){
                return db.get((String) parsedKey );
            }
            else if (key.isJsonArray()){
                return getElement((JsonArray) parsedKey, false);
            }
            else {
                throw new IllegalArgumentException("A chave precisa ser uma propriedade ou lista de propriedades");
            }
        }
        catch (Exception exception){
            exception.printStackTrace();
            return null;
        }
        finally {
            readLock.unlock();
        }
    }

    private JsonElement getElement(JsonArray keys, boolean shouldCreateKey) {
        JsonElement node = db.getAsJsonObject();

        if (shouldCreateKey) {
            for (var key : keys) {
                var keyName = key.getAsString();
                var containsKey = node.getAsJsonObject().has(keyName);

                if (!containsKey) {
                    node.getAsJsonObject().add(keyName, new JsonObject());
                }

                node = node.getAsJsonObject().get(keyName);
            }
        } else {
            for (var key : keys) {
                var keyName = key.getAsString();
                var containsKey = node.getAsJsonObject().has(keyName);

                if ((!key.isJsonPrimitive()) || (!containsKey)) {
                    throw new IllegalArgumentException("Invalid Key Name");
                }
                node = node.getAsJsonObject().get(keyName);
            }
        }
        return node;
    }

    private String prettyPrint(Object collection) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(collection);
    }

    private void write() {
        try (var writer = new FileWriter(PATH.toString())) {
            writer.write(prettyPrint(db));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
