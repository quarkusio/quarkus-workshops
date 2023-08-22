package io.quarkus.workshop.superheroes.narration;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAIClientProvider;
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

@ApplicationScoped
public class SemanticKernelNarrationService implements NarrationService {

    @Inject
    Logger logger;

    @Override
    @Fallback(fallbackMethod = "fallbackNarrate")
    @Timeout(30_000)
    public String narrate(Fight fight) throws Exception {

        // Creates an Azure OpenAI client
        OpenAIAsyncClient client = OpenAIClientProvider.getClient();

        // Creates an instance of the TextCompletion service
        TextCompletion textCompletion = SKBuilders.chatCompletion().withOpenAIClient(client).setModelId("deploy-semantic-kernel").build();

        // Instantiates the Kernel
        Kernel kernel = SKBuilders.kernel().withDefaultAIService(textCompletion).build();

        // Registers skills
        ReadOnlyFunctionCollection skill = kernel.importSkillFromDirectory("NarrationSkill", "src/main/resources", "NarrationSkill");
        CompletionSKFunction fightFunction = skill.getFunction("NarrateFight", CompletionSKFunction.class);

        // Ask for a Joke
        SKContext fightContext = SKBuilders.context().build();
        fightContext.setVariable("villain_name", "Darth Vader");
        fightContext.setVariable("villain_powers", "Accelerated Healing, Agility, Astral Projection, Cloaking, Danger Sense, Durability, Electrokinesis, Energy Blasts, Enhanced Hearing, Enhanced Senses, Force Fields, Hypnokinesis, Illusions, Intelligence, Jump, Light Control, Marksmanship, Precognition, Psionic Powers, Reflexes, Stealth, Super Speed, Telekinesis, Telepathy, The Force, Weapons Master");
        fightContext.setVariable("villain_level", "13");
        fightContext.setVariable("hero_name", "Chewbacca");
        fightContext.setVariable("hero_powers", "Agility, Longevity, Marksmanship, Natural Weapons, Stealth, Super Strength, Weapons Master");
        fightContext.setVariable("hero_level", "5");
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
}
