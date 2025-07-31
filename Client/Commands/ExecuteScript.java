package lab5.Client.Commands;

import lab5.Common.Commands.Command;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;

/**
 * Команда 'execute_script'. считывает и исполняет скрипт из указанного файла.
 * В скрипте содержатся команды в таком же виде, в котром их вводит пользователь в интерактивном режиме.
 */
public class ExecuteScript extends Command {
    private final Consolka consolka;

    public ExecuteScript(Consolka consolka) {
        super("execute_script <file_name>", "исполнить скрипт из указанного файла");
        this.consolka = consolka;
    }
    @Override
    public ExecutionResponse apply(String[] arguments, String username) {
        if (arguments[1].isEmpty()) {
            consolka.println("Неправильное количество аргументов!");
            consolka.println("Использование: '" + getName() + "'");
            return new ExecutionResponse(false, "всё пошло не по плану");
        }

        consolka.println("Выполнение скрипта '" + arguments[1] + "'...");
        return new ExecutionResponse(true, " всё супер ");
    }
}