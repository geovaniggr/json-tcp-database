package br.com.usp.mongusp;

import java.nio.file.*;

public class Main {

    /*
        Iremos apenas criar uma instância do nosso servidor TCP
        e utilizar o método init para que ele possa começar
        a receber requisições
     */
    public static void main(String[] args){
        var server = new Server();
        var path = Paths.get("");
        server.init();
    }
}
