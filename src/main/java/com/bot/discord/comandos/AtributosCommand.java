package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Optional;

/**
 * Implementa a lógica para o comando /atributos.
 * Inicia uma interface interativa com uma imagem e botões para o usuário gastar pontos.
 */
public class AtributosCommand implements ICommand {

    @Override
    public String getName() {
        return "atributos";
    }

    @Override
    public String getDescription() {
        return "Gerencia os pontos de atributos do seu personagem.";
    }

    /**
     * Executa a lógica do comando /atributos.
     * Gera a imagem dos atributos, o embed de texto e os botões de interação,
     * enviando tudo em uma única resposta.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        String userId = event.getUser().getId();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        if (personagemOpt.isEmpty()) {
            event.getHook().sendMessage("Você precisa ter um personagem para gerenciar atributos. Use `/criar`.")
                    .setEphemeral(true).queue();
            return;
        }

        Personagem personagem = personagemOpt.get();

        try {
            // 1. Gera a imagem dinâmica com os atributos atuais
            byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);

            // 2. Constrói o embed de texto com as instruções, passando os bytes da imagem
            MessageEmbed embed = EmbedManager.buildAtributosEmbed(personagem, imageBytes);

            // 3. Cria os botões de interação
            boolean hasPoints = personagem.getPontosDisponiveis() > 0;
            Button corpoBtn = Button.secondary("attr-add:" + userId + ":corpo", "💪 Corpo").withDisabled(!hasPoints);
            Button destrezaBtn = Button.secondary("attr-add:" + userId + ":destreza", "🏃 Destreza").withDisabled(!hasPoints);
            Button menteBtn = Button.secondary("attr-add:" + userId + ":mente", "🧠 Mente").withDisabled(!hasPoints);
            Button vontadeBtn = Button.secondary("attr-add:" + userId + ":vontade", "✨ Vontade").withDisabled(!hasPoints);
            ActionRow actionRow = ActionRow.of(corpoBtn, destrezaBtn, menteBtn, vontadeBtn);

            // 4. Envia a imagem, o embed e os botões, tudo na mesma mensagem
            // O nome "atributos.png" deve corresponder ao usado no EmbedManager.
            event.getHook().sendFiles(FileUpload.fromData(imageBytes, "atributos.png"))
                    .addEmbeds(embed)
                    .addComponents(actionRow)
                    .queue();

        } catch (Exception e) { // Captura Exception genérica por causa do Batik
            System.err.println("Erro ao gerar ou enviar a imagem de atributos: " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessage("Ocorreu um erro ao gerar a interface de atributos.").setEphemeral(true).queue();
        }
    }
}