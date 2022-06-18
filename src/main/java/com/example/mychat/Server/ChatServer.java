package com.example.mychat.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final List<ClientHandler> clients;//список клиентов подключенны к серверу

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);AuthService authService = new InMemoryAuthService()){
            while (true) {
                System.out.println("Ожидается подключение");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendPrivateMessage(ClientHandler clientHandler, String message) {
        try {
            final String[] split = message.split("\\p{Blank}+",4);
            final String nickSender = split[0];
            final String nickRecipient = split[2];
            final String messageText = split[3];
            for (ClientHandler client : clients) {
                if (client.getNick().equals(nickRecipient)) {
                    client.sendMessage(nickSender + ": " + messageText);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            clientHandler.sendMessage("Error. Неверный формать сообщения");
        }
    }


    public void subscribe(ClientHandler client) {
        clients.add(client);//добавили клиента, который только что залогинился
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())){
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

}



