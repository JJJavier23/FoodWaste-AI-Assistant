package com.example.ui;
import java.util.*;

public class CommandInvoker {
    private List<ICommand> commandHistory = new ArrayList<>();

    public void executeCommand(ICommand command) {
        command.execute();
        commandHistory.add(command);
    }

    public void undoCommand() {
        if (!commandHistory.isEmpty()) {
            ICommand lastCommand = commandHistory.remove(commandHistory.size() - 1);
            lastCommand.undo();
        }
    }
}
