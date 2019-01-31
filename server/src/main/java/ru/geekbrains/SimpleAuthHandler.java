package ru.geekbrains;

public class SimpleAuthHandler implements AuthHandler {
    private class Entry {
        private String login;
        private String password;
        private String nickname;

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getNickname() {
            return nickname;
        }

        public Entry(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private Entry[] entries;

    public SimpleAuthHandler() {
        this.entries = new Entry[] {
                new Entry("login1", "pass1", "nick1"),
                new Entry("login2", "pass2", "nick2"),
                new Entry("login3", "pass3", "nick3")
        };
    }

    @Override
    public void start() {
        System.out.println("SimpleAuthHandler started...");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getLogin().equals(login) && entries[i].getPassword().equals(pass)) {
                return entries[i].getNickname();
            }
        }
        return null;
    }

    @Override
    public boolean changeNick(String login, String newNick) {
        return false;
    }

    @Override
    public void stop() {
        System.out.println("SimpleAuthHandler stopped...");
    }
}
