package ru.geekbrains;

import java.io.*;
import java.util.ArrayList;

public class ChatLog {
    private BufferedReader chatLogReader;
    //private FileWriter chatLogWriter;
    private ArrayList<String> chatlog;
    private String chatFileName;

    public ChatLog (String chatFileName, int chatLogSize) {
        chatlog = new ArrayList<>();
        this.chatFileName = chatFileName;

        try {
            chatLogReader = new BufferedReader(new FileReader(this.chatFileName));
            String str;
            while((str = chatLogReader.readLine()) != null) {
                chatlog.add(str);
                if(chatlog.size() > chatLogSize) chatlog.remove(0);
            }
            chatLogReader.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getStrings() {
        String[] strings = new String[chatlog.size()];
        chatlog.toArray(strings);
        return strings;
    }

    public boolean writeString(String str) {
        try(FileWriter chatLogWriter = new FileWriter(chatFileName, true)) {
            chatLogWriter.write(str);
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }
}
