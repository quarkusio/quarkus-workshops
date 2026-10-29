package io.quarkus.workshop.superheroes.load;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "CLI Super Heroes")
public class CLIMain implements Callable<Integer> {

    @CommandLine.Option(names = {"-s", "--stop"},
        description = "Write something and press enter to stop",
        required = true,
        interactive = true)
    String stop;

    private final StopMessage stopAction;

    public CLIMain(StopMessage greetingService) {
        this.stopAction = greetingService;
    }

    @Override
    public Integer call() {
        stopAction.stop();
        return CommandLine.ExitCode.OK;
    }

    @Dependent
    static class StopMessage {
        void stop() {
            Log.info("Stop");
        }
    }
}
