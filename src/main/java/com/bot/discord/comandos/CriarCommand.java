package com.bot.discord.comandos;

import com.bot.discord.DisplayManager;
import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /criar.
 * <p>
 * Cria um novo personagem se o usuário não tiver um, ou exibe o personagem
 * existente caso contrário. Em ambos os casos, a ficha completa do personagem é exibida.
 */
public class CriarCommand implements ICommand {

    @Override
    public String getName() {
        return "criar";
    }

    @Override
    public String getDescription() {
        return "Cria um novo personagem ou exibe o seu personagem existente.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "nome", "O nome do seu personagem.", true),
                new OptionData(OptionType.INTEGER, "nivel", "O nível inicial do seu personagem.", false)
        );
    }

    /**
     * Executa a lógica do comando /criar.
     * <p>
     * O fluxo de execução agora é:
     * 1. Verifica se o personagem já existe.
     * 2. Se sim, chama um método auxiliar para exibir a ficha do personagem existente.
     * 3. Se não, cria um novo personagem e então chama o mesmo método auxiliar para
     * exibir a ficha do personagem recém-criado.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        if (personagemOpt.isPresent()) {
            // Caso 1: Personagem já existe. Delega a exibição para o DisplayManager.
            // Passamos 'false' para não mostrar os botões de upgrade aqui.
            DisplayManager.displayCharacterSheet(event.getHook(), user, service, true, "Você já possui um personagem! Aqui está ele:");
        } else {
            // Caso 2: Personagem não existe. Cria e depois delega a exibição.
            String nome = Optional.ofNullable(event.getOption("nome")).map(OptionMapping::getAsString).orElse("Aventureiro");
            int nivel = Optional.ofNullable(event.getOption("nivel")).map(OptionMapping::getAsInt).orElse(1);

            if (nivel < 1) {
                event.getHook().editOriginal("O nível não pode ser menor que 1.").queue();
                return;
            }

            // O service.criarPersonagem() continua aqui
            service.criarPersonagem(userId, nome, nivel);

            // Mas a resposta é delegada para o DisplayManager
            DisplayManager.displayCharacterSheet(event.getHook(), user, service, true, "Personagem criado com sucesso! Aqui está sua ficha:");
        }
    }
}