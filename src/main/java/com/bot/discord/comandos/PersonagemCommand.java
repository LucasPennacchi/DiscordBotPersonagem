package com.bot.discord.comandos;

import com.bot.Bot;
import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementa a lógica para o comando /personagem de forma assíncrona.
 * Exibe a ficha de personagem em etapas para uma experiência de usuário mais responsiva.
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

    /**
     * Executa a lógica do comando /personagem em um fluxo assíncrono de 3 etapas.
     * <p>
     * 1. **Resposta Imediata:** Envia o embed contendo apenas o texto da ficha.
     * 2. **Processamento em Background:** Inicia a tarefa de geração da imagem em uma thread separada.
     * 3. **Edição Final:** Quando a imagem está pronta, edita a mensagem original para adicioná-la,
     * juntamente com os botões de interação, se aplicável.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // Usamos deferReply() para ter um 'gancho' de resposta estável.
        // A primeira resposta visível será enviada com sendMessageEmbeds() abaixo.
        event.deferReply().setEphemeral(true).queue();

        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    // ETAPA 1: RESPOSTA IMEDIATA (APENAS TEXTO)
                    MessageEmbed textOnlyEmbed = EmbedManager.buildPersonagemEmbedTextOnly(personagem, user);

                    // Envia o embed de texto como a primeira resposta visível ao usuário.
                    event.getHook().sendMessageEmbeds(textOnlyEmbed).queue(message -> {
                        // O código dentro deste bloco é executado após a mensagem de texto ser enviada.

                        // ETAPA 2: GERAÇÃO DA IMAGEM (EM SEGUNDO PLANO)
                        // Submetemos a tarefa de gerar a imagem para o nosso pool de threads.
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return ImageGenerator.generatePersonagemAttributesImage(personagem);
                            } catch (Exception e) {
                                // Encapsula exceções checadas para o CompletableFuture poder lidar com elas.
                                throw new RuntimeException(e);
                            }
                        }, Bot.EXECUTOR).thenAccept(imageBytes -> {
                            // ETAPA 3: EDIÇÃO FINAL (COM IMAGEM E BOTÕES)
                            // Este bloco é executado quando a imagem (imageBytes) fica pronta.

                            // Constrói o embed final, que agora referencia a imagem.
                            MessageEmbed finalEmbed = EmbedManager.buildPersonagemEmbedWithImage(personagem, user);

                            var editAction = message.editMessageEmbeds(finalEmbed)
                                    .setFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"));

                            // Adiciona os botões de interação se o personagem tiver pontos.
                            if (personagem.getPontosDisponiveis() > 0) {
                                Button corpoBtn = Button.secondary("attr-add:" + user.getId() + ":corpo", "💪 Corpo");
                                Button destrezaBtn = Button.secondary("attr-add:" + user.getId() + ":destreza", "👟 Destreza");
                                Button menteBtn = Button.secondary("attr-add:" + user.getId() + ":mente", "🧠 Mente");
                                Button vontadeBtn = Button.secondary("attr-add:" + user.getId() + ":vontade", "🌊 Vontade");
                                editAction.setComponents(ActionRow.of(corpoBtn, destrezaBtn, menteBtn, vontadeBtn));
                            }

                            editAction.queue();

                        }).exceptionally(ex -> {
                            // Bloco executado se ocorrer um erro durante a geração da imagem.
                            System.err.println("Erro ao gerar imagem em background para a ficha:");
                            ex.printStackTrace();
                            // Não fazemos nada na mensagem do Discord para não perturbar o usuário,
                            // ele ficará apenas com a versão em texto da ficha.
                            return null;
                        });
                    });
                },
                () -> event.getHook().sendMessage("Você não possui um personagem. Use `/criar` para começar!").queue()
        );
    }
}