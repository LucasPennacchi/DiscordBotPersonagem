package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
// --- IMPORTAÇÃO CORRETA ---
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Optional;

/**
 * Implementa a lógica para o comando /personagem.
 * Exibe a ficha de personagem e, se houver pontos disponíveis,
 * exibe também os botões para upgrade de atributos.
 */
public class PersonagemCommand implements ICommand {

    @Override
    public String getName() {
        return "personagem";
    }

    @Override
    public String getDescription() {
        return "Mostra a ficha e permite gerenciar os atributos do seu personagem.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    try {
                        byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);
                        MessageEmbed embed = EmbedManager.buildPersonagemEmbed(personagem, user, imageBytes);

                        // --- INÍCIO DA CORREÇÃO ---
                        // A variável agora é declarada com o tipo correto: WebhookMessageCreateAction
                        WebhookMessageCreateAction messageAction = event.getHook()
                                .sendFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"))
                                .addEmbeds(embed);
                        // --- FIM DA CORREÇÃO ---

                        // Lógica condicional para adicionar os botões
                        if (personagem.getPontosDisponiveis() > 0) {
                            Button corpoBtn = Button.secondary("attr-add:" + user.getId() + ":corpo", "💪 Corpo");
                            Button destrezaBtn = Button.secondary("attr-add:" + user.getId() + ":destreza", "🏃 Destreza");
                            Button menteBtn = Button.secondary("attr-add:" + user.getId() + ":mente", "🧠 Mente");
                            Button vontadeBtn = Button.secondary("attr-add:" + user.getId() + ":vontade", "✨ Vontade");
                            messageAction.addComponents(ActionRow.of(corpoBtn, destrezaBtn, menteBtn, vontadeBtn));
                        }

                        messageAction.queue();

                    } catch (Exception e) {
                        System.err.println("Erro ao gerar ou enviar a imagem da ficha: " + e.getMessage());
                        e.printStackTrace();
                        event.getHook().sendMessage("Ocorreu um erro ao gerar a imagem da sua ficha.").setEphemeral(true).queue();
                    }
                },
                () -> event.getHook().sendMessage("Você não possui um personagem. Use `/criar` para começar sua jornada!")
                        .setEphemeral(true).queue()
        );
    }
}