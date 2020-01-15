package chat;

import chat.managers.Executor;

import java.io.IOException;

public class Main {

    public static void main(final String[] args) throws IOException {

        try (Executor executor = new Executor()) {
            executor.configure();
            executor.run();
        }

    }

}