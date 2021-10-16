package br.com.usp.mongusp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final Path RESOURCES_PATH = Paths.get("src", "main",  "resources");

    private static final Integer PORT = 3333;
    private static final Integer BACKLOG = 50;

    private final ExecutorService executor;

    public Server() {
        System.out.println("Iniciando o Servidor");
        final var numberOfThreadsAvailable = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numberOfThreadsAvailable);
    }

    public void init(){
        try(var socket = new ServerSocket(PORT, BACKLOG, InetAddress.getLocalHost())){
            while(!executor.isShutdown()){
                var request = new RequestExecutor(socket.accept());
                executor.submit(request);
            }
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
