package com.example.mychat.Server;

import com.example.mychat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {
    private final Map<String, ClientHandler> clients;//список клиентов подключенны к серверу

    public ChatServer() {
        this.clients = new HashMap<>();
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

    public void sendPrivateMessage(ClientHandler from, String nickto,String message) {
        ClientHandler clientTo = clients.get(nickto);
        if (clientTo==null){
            from.sendMessage(Command.ERROR, "Пользователь не авторизован");
            return;
        }
        clientTo.sendMessage(Command.MESSAGE, " От " + from.getNick() + ": " + message);
        from.sendMessage(Command.MESSAGE,"Участнику " + nickto + ": " + message);
    }


    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public boolean isNickBusy(String nick) {
       return clients.get(nick) !=null;
    }

    private void broadcastClientList() {//все пользователи будут получать список подключенных в данный момент клиентов
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        //clients.values(). - вернет список клиентов, которые лежат в мапе
        //берем stream() и начинаем делать операции
//.map - операция применяет к каждому элементу этого стрима применяет функцию, в данном случае у каждого ClientHandler берем getNick
//.collect(Collectors.joining - потом мы все собираем. Который объединяет эти стринги с разделителем пробел
        broadcast(Command.CLIENTS,nicks);
    }

    public void broadcast(Command command, String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command,message);
        }
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();

    }

}
