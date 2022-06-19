package com.example.mychat;

import java.util.Arrays;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Command {
    //каждый экземпляр перечисления является наследником от Command, в котором есть абстр.метод. Поэтому имплементируем в каждый кэземпляр этот метод

    AUTH("/auth") {//auth login1 pass1

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
    CLIENTS("/clients") {//список клиентов: clients nick1 nick2 nick3

        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER);
            String[] nicks = new String[split.length - 1];
            for (int i = 0; i < nicks.length; i++) {
                nicks[i] = split[i + 1];
            }
            return nicks;
        }
    },
    ERROR("/error") {// error hello my friend

        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER,2);
            return new String[]{split[1]};
        }
    },
    MESSAGE("/message"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(TOKEN_DELIMITER,2);
            return new String[]{split[1]};
        }
    };

    private final String command;
    static final String TOKEN_DELIMITER = "\\p{Blank}+";
    //создадим Map для более быстрого поиска.(поиск будет за константное время) Цикл нам не нужен. Это статическое поле, которое инициализируется при загрузке класса при запуске программы.
    //вместо этого:
//    static final Map<String, Command> commandMap = Map.of(
//            "/auth", AUTH,
//            "/authok", AUTHOK,
//            "/end", END,
//            "/w", PRIVATE_MESSAGE
//            "/clients", CLIENTS
//            "/error", ERROR
//    );
    // Сделаем через стрим:
    static final Map<String, Command> commandMap = Arrays.stream(values()).collect(Collectors.toMap(Command::getCommand, Function.identity()));
    //Function.identity() = a->a
    //этот стрим превращает массив в мапу

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isCommand(String message) {
        //если сообщение начинается со / то это команда
        return message.startsWith("/");
    }

    public static Command getCommand(String message) {
        if (!isCommand(message)) {
            //если это не команда, тогда бросим исключение
            throw new RuntimeException("'" + message + "' is not a command ");
        }
        String cmd = message.split(TOKEN_DELIMITER, 2)[0];

        //в результате, в 0 индексе - команда будет лежать
        //метод values() возвращает массив из перечислений
//        for (Command command : values()) {
//            if (command.getCommand().equals(cmd)) {
//                return command;
//            }
//        }
//вместо цикла:
        Command command = commandMap.get(cmd);
        if (command==null){
            throw new RuntimeException("Unknown command '" + cmd + "'");
        }
        return command;
    }

    //парсить - брать текст и превращать его в команду
    public abstract String[] parse(String commandText);

//String...params эта запись синтаксический сахар(String[]params).Проще вызывать
    //можно просто перечислить любое колличество параметров - collectMessage("1","3","dfhj") и получится массив стрингов
    public String collectMessage(String...params){

return this.command + " " + String.join(" ", params);
    }
}
