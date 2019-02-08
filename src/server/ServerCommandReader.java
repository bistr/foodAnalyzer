package server;

import enums.Command;

import java.util.Scanner;

public class ServerCommandReader implements Runnable {
    @Override


    public void run() {
        Scanner scanner = new Scanner(System.in);
        String consoleInput;

        while ((consoleInput = scanner.nextLine()) != null) {
            if (consoleInput.equalsIgnoreCase(Command.STOP.toString())) {
                CommandExecutionServer.stop();
                break;
            }
        }
        scanner.close();
    }
}
