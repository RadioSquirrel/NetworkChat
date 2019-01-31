package ru.geekbrains;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isntConnected() {
        return socket == null || socket.isClosed();
    }

    public void connect(CallbackArgs callMessageToTextArea, CallbackArgs callAuthOk, CallbackArgs callClientsList, CallbackArgs callDisconnect) {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok ")) {
                                callAuthOk.callback(str.split(" ")[1]);
                                break;
                            }
                            callMessageToTextArea.callback(str + "\n");
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.startsWith("/clientslist ")) {
                                    String[] tokens = str.split(" ");
                                    callClientsList.callback(tokens);
                                }
                            } else {
                                callMessageToTextArea.callback(str + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        callDisconnect.callback();
                        closeConnection();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
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
