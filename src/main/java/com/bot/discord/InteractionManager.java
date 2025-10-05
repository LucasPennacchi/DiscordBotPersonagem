package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Collections;
import java.util.Optional;

/**
 * Classe utilitária para tratar eventos de interação de componentes, como botões.
 * Esta classe centraliza a lógica de resposta a interações contínuas, agindo como
 * um roteador para diferentes tipos de cliques em botões.
 */
public final class InteractionManager {

    private InteractionManager() {}

    /**
     * Processa todos os eventos de clique em botão ({@link ButtonInteractionEvent}).
     * <p>
     * Ele analisa o ID customizado do botão para determinar a ação a ser tomada
     * (ex: "delete-confirm", "attr-add", "reflexo-btn") e delega para o método
     * de tratamento apropriado. Também inclui uma verificação de segurança para
     * garantir que apenas o usuário que iniciou o comando possa clicar nos botões.
     *
     * @param event O objeto do evento de interação de botão.
     * @param service A instância do {@link PersonagemService} para executar ações de negócio.
     */
    public static void handleButtonInteraction(ButtonInteractionEvent event, PersonagemService service) {
        String[] parts = event.getComponentId().split(":");
        String action = parts[0];
        String targetUserId = parts[1];

        // Medida de segurança: Apenas o usuário que pode interagir com os botões.
        if (!event.getUser().getId().equals(targetUserId)) {
            event.reply("Você não pode interagir com os botões de outro usuário.").setEphemeral(true).queue();
            return;
        }

        // Roteia a ação com base na primeira parte do ID do botão.
        switch (action) {
            case "delete-confirm", "delete-cancel":
                event.deferEdit().queue();
                handleDeleteConfirmation(event, service, action, targetUserId);
                break;

            case "attr-add":
                // deferEdit() é chamado dentro do método auxiliar
                String attributeToUpgrade = parts[2];
                handleAttributeUpgrade(event, service, targetUserId, attributeToUpgrade);
                break;
        }
    }

    /**
     * Lida com a confirmação (ou cancelamento) da exclusão de um personagem.
     */
    private static void handleDeleteConfirmation(ButtonInteractionEvent event, PersonagemService service, String action, String userId) {
        if ("delete-confirm".equals(action)) {
            service.deletar(userId);
            event.getHook().editOriginal("Personagem deletado com sucesso.")
                    .setComponents(Collections.emptyList()).queue();
        } else {
            event.getHook().editOriginal("Ação cancelada.")
                    .setComponents(Collections.emptyList()).queue();
        }
    }


    /**
     * Lida com a lógica de upgrade de atributos após um clique no botão.
     * Atualiza o personagem, gera uma nova imagem/embed e edita a mensagem original.
     */
    private static void handleAttributeUpgrade(ButtonInteractionEvent event, PersonagemService service, String userId, String attributeToUpgrade) {
        event.deferEdit().queue();
        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        if (personagemOpt.isEmpty()) {
            event.getHook().editOriginal("Erro: Personagem não encontrado.")
                    .setComponents(Collections.emptyList()).queue();
            return;
        }

        Personagem p = personagemOpt.get();

        if (p.getPontosDisponiveis() > 0 && service.podeAumentarAtributo(p, attributeToUpgrade)) {
            p.setPontosDisponiveis(p.getPontosDisponiveis() - 1);
            switch (attributeToUpgrade) {
                case "corpo" -> p.setCorpo(p.getCorpo() + 1);
                case "destreza" -> p.setDestreza(p.getDestreza() + 1);
                case "mente" -> p.setMente(p.getMente() + 1);
                case "vontade" -> p.setVontade(p.getVontade() + 1);
            }
            service.salvar(p);
        }

        try {
            byte[] newImageBytes = ImageGenerator.generatePersonagemAttributesImage(p);
            MessageEmbed newEmbed = EmbedManager.buildPersonagemEmbedWithImage(p, user);

            WebhookMessageEditAction editAction = event.getHook()
                    .editOriginalAttachments(FileUpload.fromData(newImageBytes, "ficha_atributos.png"))
                    .setEmbeds(newEmbed);

            // Se os pontos acabarem após o upgrade, remove os botões.
            // Caso contrário, mantém os botões para o próximo clique.
            if (p.getPontosDisponiveis() <= 0) {
                editAction.setComponents(Collections.emptyList());
            } else {
                // Pega a fileira de botões da mensagem original para mantê-la
                editAction.setComponents(event.getMessage().getActionRows());
            }

            editAction.queue();

        } catch (Exception e) {
            System.err.println("Erro ao re-gerar imagem de atributos: " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessage("Ocorreu um erro ao atualizar a ficha.").setEphemeral(true).queue();
        }
    }
}