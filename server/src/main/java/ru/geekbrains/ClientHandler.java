package ru.geekbrains;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String login;
    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            startWorkerThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWorkerThread() {
        server.getServerExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/auth ")) {
                            // /auth login1 pass1
                            String[] tokens = msg.split(" ");
                            ClientHandler.this.login = tokens[1];
                            String nick = server.getAuthHandler().getNickByLoginPass(tokens[1], tokens[2]);
                            if (nick != null) {
                                if (server.isNickBusy(nick)) {
                                    out.writeUTF("Account already used");
                                    server.getLogger().log(Level.WARNING, "Account already used");
                                    continue;
                                }
                                out.writeUTF("/authok " + nick);
                                ClientHandler.this.nick = nick;
                                server.subscribe(ClientHandler.this);
                                server.getLogger().log(Level.INFO, "User " + nick + " log in");
                                break;
                            } else {
                                out.writeUTF("Wrong login/password");
                                server.getLogger().log(Level.WARNING, "Wrong login/password");
                            }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.startsWith("/w ")) {
                                // /w nick2 hello hello
                                String[] tokens = msg.split(" ", 3);
                                server.sendPrivateMsg(ClientHandler.this, tokens[1], tokens[2]);
                                server.getLogger().log(Level.INFO, "User " + ClientHandler.this.nick + " send a private msg");
                            }
                            if (msg.startsWith("/changenick ")) {
                                String[] tokens = msg.split(" ");
                                if (tokens.length == 2) {
                                    if (server.getAuthHandler().changeNick(ClientHandler.this.login, tokens[1])) {
                                        server.getLogger().log(Level.INFO, "User " + ClientHandler.this.nick + " changed nickname to " + tokens[1]);
                                        server.broadcastMsg(ClientHandler.this, "User " + ClientHandler.this.nick + " changed nickname to " + tokens[1] + ".");
                                        ClientHandler.this.nick = tokens[1];
                                        server.broadcastClientsList();
                                    } else {
                                        server.getLogger().log(Level.WARNING, "Nickname " + tokens[1] + " already used");
                                        ClientHandler.this.sendMessage("Nickname already used");
                                    }
                                } else {
                                    server.getLogger().log(Level.WARNING, "User " + ClientHandler.this.nick + " failed /changenick");
                                    ClientHandler.this.sendMessage("Failed /changenick.");
                                }
                            }
                        } else {
                            server.getLogger().log(Level.INFO, "User " + ClientHandler.this.nick + " sent a public msg");
                            server.broadcastMsg(ClientHandler.this, msg);
                        }
                        //System.out.println(msg);
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                } finally {
                    ClientHandler.this.closeConnection();
                    server.getLogger().log(Level.INFO, (ClientHandler.this.nick != null ? "User " + ClientHandler.this.nick : "Client") + " offline");
                }
            }
        });
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}