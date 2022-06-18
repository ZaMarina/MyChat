package com.example.mychat.Server;

import java.io.Closeable;

//класс который хранит логины и пароли
public interface AuthService extends Closeable{
    String getNickByLoginAndPassword(String login, String password);
}


