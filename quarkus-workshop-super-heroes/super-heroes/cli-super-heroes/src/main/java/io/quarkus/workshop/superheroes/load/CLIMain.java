package io.quarkus.workshop.superheroes.load;

import io.quarkus.logging.Log;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.workshop.superheroes.load.scenarios.FightScenario;
import io.quarkus.workshop.superheroes.load.scenarios.HeroScenario;
import io.quarkus.workshop.superheroes.load.scenarios.VillainScenario;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;

@TopCommand
@CommandLine.Command(name = "Cli Super Heroes", mixinStandardHelpOptions = true)
public class CLIMain implements Callable<Integer> {

    @Inject
    VillainScenario villainScenario;

    @Inject
    HeroScenario heroScenario;

    @Inject
    FightScenario fightScenario;

    @Override
    public Integer call() throws InterruptedException {
        while (true) {
            heroScenario.run();
            villainScenario.run();
            fightScenario.run();
            Thread.sleep(500);
        }

  //      return CommandLine.ExitCode.USAGE;
    }
}
