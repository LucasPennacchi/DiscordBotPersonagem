package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Collections;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /mostrar.
 * <p>
 * Este comando exibe a ficha de personagem do usuário de forma pública no canal,
 * permitindo que outros jogadores a vejam. Ele não inclui os botões de interação.
 */
public class MostrarCommand implements ICommand {

    @Override
    public String getName() {
        return "mostrar";
    }

    @Override
    public String getDescription() {
        return "Mostra a ficha do seu personagem publicamente no chat.";
    }

    /**
     * Executa a lógica do comando /mostrar.
     * Gera a imagem e o embed da ficha e os envia como uma resposta pública.
     * Mensagens de erro (ex: personagem não encontrado) são enviadas de forma privada.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // Deferimos a resposta como PÚBLICA (padrão, sem o 'true')
        event.deferReply().queue();

        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    try {
                        byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);
                        MessageEmbed embed = EmbedManager.buildPersonagemEmbedWithImage(personagem, user);

                        // Edita a resposta original "pensando..." com a ficha completa.
                        // Como o deferReply foi público, esta resposta também será.
                        // Não adicionamos .addComponents() aqui.
                        event.getHook().editOriginal("") // Limpa o texto "pensando..."
                                .setFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"))
                                .setEmbeds(embed)
                                .queue();

                    } catch (Exception e) {
                        System.err.println("Erro ao gerar ou enviar a imagem da ficha (comando /mostrar): " + e.getMessage());
                        e.printStackTrace();
                        // O erro é enviado de forma privada para não poluir o chat.
                        event.getHook().sendMessage("Ocorreu um erro ao gerar a imagem da sua ficha.").setEphemeral(true).queue();
                    }
                },
                // Se o personagem não for encontrado, a mensagem de erro também é privada.
                () -> event.getHook().sendMessage("Você não possui um personagem. Use `/criar` para começar sua jornada!")
                        .setEphemeral(true).queue()
        );
    }
}