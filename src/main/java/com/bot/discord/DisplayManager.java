package com.bot.discord;

import com.bot.Bot;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Classe utilitária responsável por gerenciar a exibição de componentes complexos,
 * como a ficha de personagem completa.
 */
public class DisplayManager {

    /**
     * Exibe a ficha de personagem completa de forma assíncrona.
     * Primeiro, envia o texto, e depois edita a mensagem para adicionar a imagem
     * e, opcionalmente, os botões de interação.
     *
     * @param hook O InteractionHook da interação original.
     * @param user O usuário dono do personagem a ser exibido.
     * @param service A instância do PersonagemService.
     * @param showButtons Se true, os botões de upgrade serão exibidos se houver pontos.
     * @param initialMessage O texto inicial a ser exibido na resposta.
     */
    public static void displayCharacterSheet(InteractionHook hook, User user, PersonagemService service, boolean showButtons, String initialMessage) {
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    // Etapa 1: Resposta imediata com o texto
                    MessageEmbed textOnlyEmbed = EmbedManager.buildPersonagemEmbedTextOnly(personagem, user);

                    hook.editOriginal(initialMessage).setEmbeds(textOnlyEmbed).queue(message -> {
                        // Etapa 2: Geração da imagem em background
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return ImageGenerator.generatePersonagemAttributesImage(personagem);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, Bot.EXECUTOR).thenAccept(imageBytes -> {
                            // Etapa 3: Edição final com a imagem e botões
                            MessageEmbed finalEmbed = EmbedManager.buildPersonagemEmbedWithImage(personagem, user);

                            var editAction = message.editMessageEmbeds(finalEmbed)
                                    .setFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"));

                            if (showButtons && personagem.getPontosDisponiveis() > 0) {
                                Button corpoBtn = Button.secondary("attr-add:" + user.getId() + ":corpo", "💪 Corpo");
                                Button destrezaBtn = Button.secondary("attr-add:" + user.getId() + ":destreza", "👟 Destreza");
                                Button menteBtn = Button.secondary("attr-add:" + user.getId() + ":mente", "🧠 Mente");
                                Button vontadeBtn = Button.secondary("attr-add:" + user.getId() + ":vontade", "🌊 Vontade");
                                editAction.setComponents(ActionRow.of(corpoBtn, destrezaBtn, menteBtn, vontadeBtn));
                            } else {
                                editAction.setComponents(Collections.emptyList());
                            }
                            editAction.queue();
                        }).exceptionally(ex -> {
                            System.err.println("Erro ao gerar imagem em background:");
                            ex.printStackTrace();
                            return null;
                        });
                    });
                },
                () -> hook.editOriginal("Você não possui um personagem. Use `/criar` para começar sua jornada!").queue()
        );
    }
}