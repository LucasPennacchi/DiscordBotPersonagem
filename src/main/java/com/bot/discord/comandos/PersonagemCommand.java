package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Optional;

/**
 * Implementa a lógica para o comando /personagem.
 * Exibe a ficha de personagem do usuário, incluindo uma imagem dinâmica dos atributos.
 */
public class PersonagemCommand implements ICommand {

    @Override
    public String getName() {
        return "personagem";
    }

    @Override
    public String getDescription() {
        return "Mostra a ficha do seu personagem.";
    }

    /**
     * Executa a lógica do comando /personagem.
     * O fluxo busca o personagem do usuário. Se encontrado, chama o {@link ImageGenerator}
     * para criar a imagem dos atributos e o {@link EmbedManager} para criar o embed
     * de texto que inclui a imagem. Por fim, envia ambos na mesma resposta.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    try {
                        // 1. Gera a imagem dinâmica com os atributos
                        byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);

                        // 2. Constrói o embed de texto, passando os bytes da imagem
                        MessageEmbed embed = EmbedManager.buildPersonagemEmbed(personagem, user, imageBytes);

                        // 3. Envia a imagem como arquivo (FileUpload) e o embed juntos.
                        // O nome "ficha_atributos.png" deve corresponder ao usado no EmbedManager.
                        event.getHook().sendFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"))
                                .addEmbeds(embed)
                                .queue();

                    } catch (Exception e) { // Captura Exception genérica por causa do Batik
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