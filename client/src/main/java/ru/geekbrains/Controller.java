package ru.geekbrains;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private boolean authorized;
    private Network network;
    private String nick;
    private ObservableList<String> clientsList;

    private ChatLog chatLog;
    final static int CHAT_LOG_SIZE = 100;
    final static String CHAT_FILE_NAME = "chatlog.txt";


    @FXML
    TextField msgField, loginField;

    @FXML
    TextArea mainTextArea;

    @FXML
    PasswordField passField;

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    ListView<String> clientsView;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (this.authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            clientsView.setVisible(true);
            clientsView.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            clientsView.setVisible(false);
            clientsView.setManaged(false);
            nick = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        clientsList = FXCollections.observableArrayList();
        clientsView.setItems(clientsList);
        network = new Network();
    }

    public void sendMsg() {
        network.sendMessage(msgField.getText());
        msgField.clear();
        msgField.requestFocus();
    }

    public void sendAuth(ActionEvent actionEvent) {
        if (network.isntConnected()) {
            network.connect(
                    argsGetMessage -> {
                        mainTextArea.appendText((String) argsGetMessage[0]);
                        if(!chatLog.writeString((String) argsGetMessage[0]))
                            showAlert("Log msg hasnt saved");
                    },
                    argsAuthOk -> {
                        nick = (String) argsAuthOk[0];
                        Controller.this.setAuthorized(true);
                        chatLog = new ChatLog(nick + ".txt"/*CHAT_FILE_NAME*/, CHAT_LOG_SIZE);
                        String[] strs = chatLog.getStrings();
                        for (String str : strs) {
                            mainTextArea.appendText(str + "\n");
                        }
                    },
                    argsGetClientsList -> Platform.runLater(() -> {
                        clientsList.clear();
                        String[] tokens = (String[]) argsGetClientsList;
                        for (int i = 1; i < tokens.length; i++) {
                            clientsList.add(tokens[i]);
                        }
                    }),
                    argsDisconnect -> {
                        showAlert("Lost connection");
                        setAuthorized(false);
                    }
            );
        }

        if (network.sendMessage("/auth " + loginField.getText() + " " + passField.getText())) {
            loginField.clear();
            passField.clear();
        } else {
            showAlert("Msg dont send. Check ur network");
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void clickClientsList(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String str = clientsView.getSelectionModel().getSelectedItem();
            msgField.setText("/w " + str + " ");
            msgField.requestFocus();
            msgField.selectEnd();
        }
    }
}
