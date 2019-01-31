package ru.geekbrains;

import java.sql.*;

public class DBAuthHandler implements AuthHandler{
    private static Connection connection;
    private static PreparedStatement psSelect;
    private static PreparedStatement psSelectNick;
    private static PreparedStatement psUpdate;


    public DBAuthHandler() {
    }

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chatusers.db");
            psSelect = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?;");
            psSelectNick = connection.prepareStatement("SELECT * FROM users WHERE nickname = ?;");
            psUpdate = connection.prepareStatement("UPDATE users SET nickname = ? WHERE login = ?;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        try {
            psSelect.setString(1, login);
            psSelect.setString(2, pass);
            ResultSet rs = psSelect.executeQuery();
            if(rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean changeNick(String login, String newNick) {
        try {
            psSelectNick.setString(1, newNick);
            ResultSet rs = psSelectNick.executeQuery();
            if(rs.next()) {
                return false;
            }

            psUpdate.setString(1, newNick);
            psUpdate.setString(2, login);
            psUpdate.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void stop() {
        try {
            psSelect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            psSelectNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            psUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
