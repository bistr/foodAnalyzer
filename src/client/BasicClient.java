package client;

import server.CommandExecutionServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BasicClient {

    private InetAddress remoteHost;
    private int remotePort = 0;

    public BasicClient(InetAddress host, int port) {
        this.remoteHost = host;
        this.remotePort = port;
    }

    public static void main(String[] args) throws UnknownHostException {
        BasicClient ec = new client.BasicClient(InetAddress.getByName("localhost"), CommandExecutionServer.SERVER_PORT);
        ec.start();
    }

    public void start() {
        try (Socket socket = new Socket(remoteHost, remotePort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             Scanner console = new Scanner(System.in)) {
            System.out.println("Client " + socket + " connected to server");

            String consoleInput;
            while ((consoleInput = console.nextLine()) != null) {
                // Stop the client
                if ("quit".equalsIgnoreCase(consoleInput.trim())) {
                    break;
                }
                // Send to the server
                out.println(consoleInput);
                out.flush();

                // Read the response from the server
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    response.append(line);
                    response.append("\n");
                }
                if (line == null) throw new IOException();

                System.out.println(response.toString());
            }
        } catch (IOException ioe) {
            System.out.println("Can't connect to server. :(");
        }
        System.out.println("Client stopped");
    }

}