package com.example.mychat;

public enum Command {
    //каждый экземпляр перечисления является наследником от Command, в котором есть абстр.метод. Поэтому имплементируем в каждый кэземпляр этот метод

    AUTH("/auth"){//auth login1 pass1
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1], split[2]};//возвращаем массив, который состоит из логина и пароля
        }
    },
    AUTHOK("/authok") {// authok nock1
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER);
            return new String[]{split[1]};
        }
    },
    END("/end") {//*
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    PRIVATE_MESSAGE("/w") {// /w nick1 long long message
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER, 3);
            return new String[]{split[1], split[2]};
        }
    },
    CLIENTS("/clients") {//*
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    ERROR("/error") {// error hello my friend
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    };

    private final String command;
    static final String TOKEN_DELIMITER = "\\p{Blank}+";

    Command(String command) {
        this.command = command;
    }
    //парсить - брать текст и превращать его в команду
    public abstract String [] parse(String commandText);

}
