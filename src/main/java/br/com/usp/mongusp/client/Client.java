package br.com.usp.mongusp.client;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static Gson parser = new Gson();
    private static String collection = "database";

    private static String sendMessage(String message){
        try(
            var socket = new Socket(InetAddress.getLocalHost(), 3333);
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream())
        ){
            output.writeUTF(message);

            var response = input.readUTF();

            return response;
        } catch (Exception e){
            e.printStackTrace();

            return "Houve um erro ao fazer a requisição";
        }
    }

    private static String getUser(String key){
        var json = new JsonObject();
        json.addProperty("type", "get");
        json.addProperty("collection", collection);

        if(key.trim().startsWith("[")){
            var nonPrimitiveKey = parser.fromJson(key, JsonElement.class);
            json.add("key", nonPrimitiveKey);
        } else {
            json.addProperty("key", key);
        }

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }
    private static String deleteUser(String key){
        var json = new JsonObject();
        json.addProperty("type", "delete");
        json.addProperty("collection", collection);
        json.addProperty("key", key);
        json.addProperty("value", "");

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }
    private static String updateUser(String key, String value){
        var json = new JsonObject();

        json.addProperty("type", "update");
        json.addProperty("collection", collection);

        if(key.trim().startsWith("[")){
            var nonPrimitiveKey = parser.fromJson(key, JsonElement.class);
            json.add("key", nonPrimitiveKey);
        } else {
            json.addProperty("key", key);
        }

        if(value.trim().startsWith("{")){
            var parsedValue = new Gson().fromJson(value, JsonElement.class);
            json.add("value", parsedValue);
        } else {
            json.addProperty("value", value);
        }

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }
    private static String setUser(String value){

        var parsedValue = new Gson().fromJson(value, JsonElement.class);

        var json = new JsonObject();
        json.addProperty("type", "set");
        json.addProperty("collection", collection);
        json.add("value", parsedValue);

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }

    private static String getAll(){
        var json = new JsonObject();
        json.addProperty("type", "list_all");
        json.addProperty("collection", collection);

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }

    private static String createCollection(String name){
        var json = new JsonObject();
        json.addProperty("type", "create_collection");
        json.addProperty("collection", name);

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }

    private static String removeCollection(String name){
        var json = new JsonObject();
        json.addProperty("type", "remove_collection");
        json.addProperty("collection", collection);
        json.addProperty("key", name);

        var data = new GsonBuilder().create().toJson(json);

        return data;
    }

    private static void setCollection(String name){
        collection = name;
    }

    public static void main(String[] args){
        System.out.println("Iniciando o Cliente");

        int choice = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("+----------------------Trabalho de Redes----------------------+");
        System.out.println("+---Bruno Henrique de Souza Jeannine Rocha - nUSP: 11207971---+");
        System.out.println("+--------------Geovani Granieri - nUSP: 11270681--------------+");
        System.out.println("+-------Guilherme Henrique dos Santos - nUSP: 10407663--------+");
        System.out.println("+-------Guilherme Vaz de Sousa Ribeiro - nUSP: 10300320-------+");
        System.out.println("+-----Leonardo Siqueira Toscano de Britto - nUSP: 9794062-----+");
        System.out.println("+-------------------------------------------------------------+");
        while(choice < 9) {
            System.out.println("Utilizando a Coleção: [ " + collection + " ]");
            System.out.println("Escolha uma opção:");
            System.out.println("1 --> Buscar Usuário");
            System.out.println("2 --> Deletar Usuário");
            System.out.println("3 --> Editar Usuário");
            System.out.println("4 --> Criar Usuário");
            System.out.println("5 --> Listar Todos Elementos");
            System.out.println("6 --> Trocar Coleção");
            System.out.println("7 --> Criar Coleção");
            System.out.println("8 --> Remover Coleção");
            System.out.println("9 --> Sair");
            System.out.print("----> ");
            choice = scanner.nextInt();
            scanner.nextLine();
            String key;
            String value;
            String result = "Saindo";
            switch (choice){
                case 1:
                    System.out.print("---->Id: ");
                    key = scanner.nextLine();
                    result = getUser(key);
                    break;
                case 2:
                    System.out.print("---->Id: ");
                    key = scanner.nextLine();
                    result = deleteUser(key);
                    break;
                case 3:
                    System.out.print("---->Id: ");
                    key = scanner.nextLine();
                    System.out.print("---->Valor: ");
                    value = scanner.nextLine();
                    result = updateUser(key, value);
                    break;
                case 4:
                    System.out.print("---->Valor: ");
                    value = scanner.nextLine();
                    result = setUser(value);
                    break;
                case 5:
                    result = getAll();
                    break;
                case 6:
                    System.out.print("---->Nome da Coleção: ");
                    value = scanner.nextLine();
                    setCollection(value);
                    break;
                case 7:
                    System.out.print("---->Nome da Coleção: ");
                    value = scanner.nextLine();
                    result = createCollection(value);
                    System.out.println(result);
                    break;
                case 8:
                    System.out.print("---->Nome da Coleção: ");
                    value = scanner.nextLine();
                    result = removeCollection(value);
                    break;
            }

            if(choice > 8 || choice != 6){
                String finalResult = result;

                System.out.println(result);
                new Thread(() -> {
                    try(
                            var socket = new Socket(InetAddress.getLocalHost(), 3333);
                            var input = new DataInputStream(socket.getInputStream());
                            var output = new DataOutputStream(socket.getOutputStream())
                    ){
                        output.writeUTF(finalResult);

                        var response = input.readUTF();
                        System.out.println(response);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

}
