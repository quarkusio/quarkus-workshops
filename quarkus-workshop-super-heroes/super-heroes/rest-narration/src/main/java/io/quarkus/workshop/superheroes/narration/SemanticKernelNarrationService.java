package io.quarkus.workshop.superheroes.narration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.connectors.ai.openai.util.ClientType;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.skilldefinition.ReadOnlyFunctionCollection;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;
import com.microsoft.semantickernel.textcompletion.TextCompletion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class SemanticKernelNarrationService implements NarrationService {

    @Inject
    Logger logger;

    @Override
    @Fallback(fallbackMethod = "fallbackNarrate")
    @Timeout(30_000)
    public String narrate(Fight fight) throws Exception {

        // Creates an Azure OpenAI client
        OpenAIAsyncClient client = getClient();
        // OpenAIAsyncClient client = OpenAIClientProvider.getClient();

        // Creates an instance of the TextCompletion service
        TextCompletion textCompletion = SKBuilders.chatCompletion().withOpenAIClient(client).setModelId("deploy-semantic-kernel").build();

        // Instantiates the Kernel
        Kernel kernel = SKBuilders.kernel().withDefaultAIService(textCompletion).build();

        // Registers skills
        ReadOnlyFunctionCollection skill = kernel.importSkillFromDirectory("NarrationSkill", "src/main/resources", "NarrationSkill");
        CompletionSKFunction fightFunction = skill.getFunction("NarrateFight", CompletionSKFunction.class);

        // Ask for a Joke
        SKContext fightContext = SKBuilders.context().build();
        fightContext.setVariable("winner_team", fight.winnerTeam);
        fightContext.setVariable("winner_name", fight.winnerName);
        fightContext.setVariable("winner_powers", fight.winnerPowers);
        fightContext.setVariable("winner_level", String.valueOf(fight.winnerLevel));
        fightContext.setVariable("loser_team", fight.loserTeam);
        fightContext.setVariable("loser_name", fight.loserName);
        fightContext.setVariable("loser_powers", fight.loserPowers);
        fightContext.setVariable("loser_level", String.valueOf(fight.loserLevel));
        Mono<SKContext> result = fightFunction.invokeAsync(fightContext);

        String narration = result.block().getResult();
        logger.info("The narration for the fight is: " + narration);

        return narration;
    }

    public String fallbackNarrate(Fight fight) {
        logger.warn("Falling back on Narration");
        return """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
            Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
            Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
            """;
    }

    private OpenAIAsyncClient getClient() throws ConfigurationException {
        String propertiesFile = "conf.properties";

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(propertiesFile)) {

            Properties properties = new Properties();
            properties.load(is);

            OpenAIClientProvider provider = new OpenAIClientProvider((Map) properties, null);

            return provider.getAsyncClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
