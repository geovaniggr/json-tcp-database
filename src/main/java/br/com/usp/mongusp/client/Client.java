package br.com.usp.mongusp.client;

import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.Scanner;

public class Client {

    public static void main(String[] args){

        System.out.println("Starting Client");

        try(
            var socket = new Socket(InetAddress.getLocalHost(), 3333);
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream())
        ){
            var requestTemplate = """
                {
                    "type": "get",
                    "key": "idade",
                    "value": ""
                }
            """;

            output.writeUTF(requestTemplate);

            var response = input.readUTF();

            System.out.println(response);
        } catch (Exception e){

        }
    }
}
