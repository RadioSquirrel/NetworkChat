package ru.geekbrains;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private ServerSocket serverSocket;
    private AuthHandler authHandler;
    private Vector<ClientHandler> clients;

    private ExecutorService serverExecutor;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    public ExecutorService getServerExecutor() {
        return serverExecutor;
    }

    public Logger getLogger() {
        return logger;
    }

    public Server() {
        serverExecutor = Executors.newCachedThreadPool();
        logger.setLevel(Level.ALL);
        try {
            authHandler = new DBAuthHandler(); //SimpleAuthHandler();
            authHandler.start();
            serverSocket = new ServerSocket(8189);
            clients = new Vector<ClientHandler>();
            logger.log(Level.INFO, "Server connected");//System.out.println("Server connected");
            while (true) {
                Socket socket = serverSocket.accept();
                logger.log(Level.INFO, "Client connected");//System.out.println("Client connected");
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverExecutor.shutdown();
            authHandler.stop();
        }
    }

    public void sendPrivateMsg(ClientHandler from, String to, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(to)) {
                o.sendMessage("from " + from.getNick() + ": " + msg);
                from.sendMessage("to " + to + ": " + msg);
                return;
            }
        }
        from.sendMessage("Client " + to + " not here");
    }

    public void broadcastMsg(ClientHandler client, String msg) {
        String outMsg = client.getNick() + ": " + msg;
        for (ClientHandler o : clients) {
            o.sendMessage(outMsg);
        }
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.substring(0, sb.length() - 1);
        for (ClientHandler o : clients) {
            o.sendMessage(out);
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }
}