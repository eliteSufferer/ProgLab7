package server.utils;

import common.functional.Request;
import common.functional.Response;
import common.functional.ServerResponseCode;
import common.functional.User;
import server.RunServer;

import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

public class HandleRequestTask implements Callable<Response> {
    private Request request;
    private CommandControl commandControl;


    public HandleRequestTask(Request request, CommandControl commandControl) {
        this.request = request;
        this.commandControl = commandControl;
    }

    @Override
    public Response call() {
        User hashedUser = new User(
                request.getUser().getUsername(),
                request.getUser().getPassword()
        );
        ServerResponseCode responseCode = executeCommand(request.getCommandName(), request.getCommandStringArgument(),
                request.getCommandObjectArgument(), hashedUser);
        RunServer.logger.info("Response сформирован");
        return new Response(responseCode, ResponseOutputer.getAndClear());
    }




    private synchronized ServerResponseCode executeCommand(String command, String commandStringArgument,
                                                           Object commandObjectArgument, User user) {
        switch (command) {
            case "":
                break;
            case "addElement":
                if (!commandControl.add(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "add_if_min":
                if (!commandControl.addElementIfMin(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "clear":
                if (!commandControl.clear(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.AHTUNG;
                break;
            case "execute_script":
                if (!commandControl.executeScript(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "filter_greater_than_status":
                if (!commandControl.filterGreaterStatus(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "group_counting_by_status":
                if (!commandControl.groupByStatus(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "help":
                if (!commandControl.help(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "info":
                if (!commandControl.info(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "print_field_ascending_person":
                if (!commandControl.printField(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                return ServerResponseCode.CLIENT_EXIT;
            case "remove_element_by_id":
                if (!commandControl.removeById(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "remove_greater":
                if (!commandControl.removeGreater(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "show":
                if (!commandControl.show(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "sort":
                if (!commandControl.sort(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "update_by_id":
                if (!commandControl.updateById(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "login":
                if (!commandControl.login(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            case "register":
                if (!commandControl.register(commandStringArgument, commandObjectArgument, user))
                    return ServerResponseCode.ERROR;
                break;
            default:
                ResponseOutputer.appendln("Команда '" + command + "' не найдена. Наберите 'help' для справки.");
                return ServerResponseCode.ERROR;
        }
        return ServerResponseCode.OK;
    }

}
