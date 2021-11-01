package br.com.usp.mongusp;

import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

    private final Path PATH;
    private final Lock readLock;
    private final Lock writeLock;

    private JsonObject db;

    public Database(String collection) {
        var lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        PATH = Server.RESOURCES_PATH.resolve(collection + ".json");
    }

    public void init() throws IOException {
        if (Files.exists(PATH)) {
            var collection = new String(Files.readAllBytes(PATH));
            db = new Gson().fromJson(collection, JsonObject.class);
        } else {
            System.out.println("Criando arquivo...");
            Files.createFile(PATH);
            db = new JsonObject();
            write();
        }
    }

    public JsonObject getAll(){
        return db;
    }

    public void createCollection(String name) throws IOException {
        var collectionName = name + ".json";
        var path = Server.RESOURCES_PATH.resolve(collectionName);
        if(!Files.exists(path)){
            Files.createFile(path);
            db = new JsonObject();
            write(path);
        }
    }

    public void removeCollection(String name) throws IOException {
        var path = Server.RESOURCES_PATH.resolve(name + ".json");
        db = new JsonObject();
        Files.deleteIfExists(path);
    }

    /**
     * O método adiciona faz as seguintes operações:
     * - Faz um lock de escrita no arquivo que representa a coleção
     * - Gera um id alfanumérico que representará aquele elemento na coleção
     * - Adiciona em memória o novo elemento
     * - Faz a escrita no arquivo
     * - Libera o lock de escrita
     */
    public void add(JsonElement value) {
        try {
            writeLock.lock();
            var id = UUID.randomUUID().toString();
            db.add(id, value);
            write();
        } finally {
            writeLock.unlock();
        }
    }

    /*
        O método de atualização recebe dois valores,
        o conjunto de chave que deseja inserir, e o novo valor,
        novamente iremos
     */

    public void update(JsonElement key, JsonElement value){
        try {
            writeLock.lock();

            /*
                Um elemento "json primitivo" é qualquer dado que não seja um objeto ({})
                ou um vetor ([]), neste caso o que será o "id", e iremos fazer um update
                completo no elemento da coleção
             */
            if(key.isJsonPrimitive()){
                db.add(key.getAsString(), value);
            }
            /*
                Neste caso o que foi passado foi um vetor que representa o caminho a ser navegado,
                por exemplo:
                    [ "id", "disciplinas", "matematica", "nota" ]
                Com base nisso o que queremos fazer é um update apenas dentro do campo nota,
                então o que iremos fazer é navegar até a posição (n-1) do json, neste caso até
                "matemática", e teremos um nó que representará:

                "matematica": {
                    "nota": 8
                }

                E desta forma o que podemos é atualizar o campo
             */
            else if (key.isJsonArray()){
                var keys = key.getAsJsonArray();
                var newest = keys.remove(keys.size() - 1).getAsString();

                var toUpdate = getElement(keys, true);
                toUpdate.getAsJsonObject().add(newest, value);
            }

            write();
        } finally {
            writeLock.unlock();
        }
    }

    /*
        O método de deletar utilizar a mesma lógica do update, primeiro verificamos se é uma chave
        primitiva (no caso o usuário quer deletar o elemento completamente) e se ele existe, para que a operaçõa
        seja concluida corretamente.

        Caso o usuário passe um JSON Array, indicará que queremos deletar apenas uma propriedade especifica
        daquele elemento, e novamente iremos navegar até a posição (n-1) e fazer a remoção daquela propriedade

        Por fim iremos escrever no arquivo e liberar o lock
     */

    public void removeElement(JsonElement key){
        try {
            writeLock.lock();
            if(key.isJsonPrimitive() && db.has(key.getAsString())){
                db.remove(key.getAsString());
            }

            else if (key.isJsonArray()) {
                var keys = key.getAsJsonArray();
                var keyToBeRemoved = keys.remove(keys.size() - 1).getAsString();

                var object = getElement(keys, false);

                object.getAsJsonObject().remove(keyToBeRemoved);
            }
            else {
                throw new IllegalArgumentException("A chave precisa ser uma propriedade ou um array de propriedades");
            }
            write();
        }
        finally {
            writeLock.unlock();
        }
    }

    /*
        O método de busca irá verificar se temos um dado primitivo (id) e queremos buscar o objeto por completo
        ou se teremos também um vetor e propriedades, indicando que queremos trazer apenas um campo especifico
        do elemento, iremos verificar se existe aquela chave, caso exista iremos retornar para o usuário

        Novamente antes de começar as operações em disco fazemos o "lock" e após finalizar o "unlock" das operações
     */
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
        finally {
            readLock.unlock();
        }
    }

    /*
        O método "getElement" faz a navegação pelo JSON quando nos é passado
         um vetor de propriedades, utilizando a biblioteca do GSON,
        dado que um json tem uma estrutura de árvore o que fazemos é verificar
        se existe aquela propriedade, caso exista podemos ir para esse nó, até que chegue
        no ponto desejado.

        Dado que usamos essa função no "update", "add" e "delete", passamos um
        parâmetro que específica se podemos criar uma chave que é inexistente
     */
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

    /**
     * Método que abre o arquivo específico da coleção e escreve no arquivo
     * o objeto atual da memória
     */
    private void write() {
        try (var writer = new FileWriter(PATH.toString())) {
            writer.write(prettyPrint(db));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void write(Path path) {
        try (var writer = new FileWriter(path.toString())) {
            writer.write(prettyPrint(db));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
