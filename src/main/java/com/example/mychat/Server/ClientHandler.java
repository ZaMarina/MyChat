package com.example.mychat.Server;

import com.example.mychat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import static java.lang.Thread.sleep;

//каждый клиент имеет свой сокет
public class ClientHandler {

 //   private static final int time = 2_000;

    private Socket socket;
    private ChatServer server;//этот класс знает все о клиентах
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;


    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //меняю тоже
                   if (authenticate()) {

                       readMessage();
                   }
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // пусть команда аутинтефикации будет /auth login1 pass1(разделены пробелами)
    private boolean authenticate() {//поменяла на boolean

        while (true) {
            try {
                final String message = in.readUTF();
                if (Command.isCommand(message)) {
                    Command command = Command.getCommand(message);

                    if (command == Command.AUTH) {
                        String[] params = command.parse(message);
                        String login = params[0];
                        String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR,"Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast(Command.MESSAGE,"Пользователь " + nick + " зашел в чат");
                            server.subscribe(this);
                            return true;
                        } else {
                            sendMessage(Command.ERROR," Неверные логин или пароль");
                        }
                    }else if (command == Command.END){
                        return false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Command command, String... params) {
        //параметров может быь больше одного, поэтому ...
        sendMessage(command.collectMessage(params));
    }


    private void closeConnection() {
        //тут вставить таймер
        sendMessage(Command.END);
//        try {
//            sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

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
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {

        while (true) {
            try {
                final String message = in.readUTF();
                System.out.println(message);
                final Command command = Command.getCommand(message);
                if (command == Command.END) {
                    break;
                }
                if (command == Command.PRIVATE_MESSAGE) {
                    String[] params = command.parse(message);
                    server.sendPrivateMessage(this,params[0],params[1]);
                    continue;
                }
                server.broadcast(Command.MESSAGE, nick + ": " + command.parse(message)[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String getNick() {
        return nick;
    }

}
