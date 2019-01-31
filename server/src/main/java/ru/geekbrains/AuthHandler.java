package ru.geekbrains;

public interface AuthHandler {
    void start();
    String getNickByLoginPass(String login, String pass);
    boolean changeNick(String login, String newNick);
    void stop();
}
