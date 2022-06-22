package com.example.mychat.Client;

import com.example.mychat.Command;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.example.mychat.Command.*;

public class ChatClient {
    private static final int time = 2_000;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticate;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;

    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (waitAuth());
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

        new Thread(() -> {

            try {
                Thread.sleep(time);//спим определенное время
                if (authenticate==false) {//если клиент не подключен

                    sendMessage(END);

                    Platform.runLater(() -> controller.showError("CloseConnection \n\n Вы не вошли в чат. Время ожидания истекло."));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean waitAuth() throws IOException {
          authenticate = false;//Добавила
        while (true) {
            final String message = in.readUTF();
            Command command = getCommand(message);
            String[] params = command.parse(message);


            if (command == Command.AUTHOK) {///authok nick1
                final String nick = params[0];
                controller.setAuth(true);
                controller.addMessage("успешная авторизация под ником " + nick);
                authenticate = true;//Добавила
                return true;//изменила
            }
            if (command == Command.ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            if (command == Command.END) {
                return false;
            }
        }
    }


    private void closeConnection() {

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String message = in.readUTF();
            Command command = getCommand(message);
            if (END == command) {
                controller.setAuth(false);
                break;
            }
            String[] params = command.parse(message);
            if (ERROR == command) {
                String errorMessage = params[0];
                Platform.runLater(() -> controller.showError(errorMessage));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(command.parse(message)[0]));
            }
            if (CLIENTS == command) {
                Platform.runLater(() -> controller.updateClientsList(params));
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}

