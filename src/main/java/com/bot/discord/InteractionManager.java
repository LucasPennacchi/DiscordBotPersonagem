package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import java.util.Collections;
import java.util.Optional;

/**
 * Classe auxiliar para tratar eventos de interação de componentes, como botões.
 * Esta classe centraliza a lógica de resposta a interações contínuas.
 */
public final class InteractionManager {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private InteractionManager() {}

    /**
     * Processa todos os eventos de clique em botão ({@link ButtonInteractionEvent}).
     * <p>
     * Roteia a ação com base no ID customizado do botão. Contém a lógica para
     * a confirmação de exclusão (/deletar) e para o incremento de atributos (/atributos).
     * Inclui uma verificação de segurança para que apenas o usuário original possa interagir.
     *
     * @param event O objeto do evento de interação de botão.
     * @param service A instância do {@link PersonagemService} para executar ações de negócio.
     */
    public static void handleButtonInteraction(ButtonInteractionEvent event, PersonagemService service) {
        String[] parts = event.getComponentId().split(":");
        String action = parts[0];
        String targetUserId = parts[1];

        // Medida de segurança: Apenas o usuário que iniciou o comando pode interagir.
        if (!event.getUser().getId().equals(targetUserId)) {
            event.reply("Você não pode interagir com os botões de outro usuário.").setEphemeral(true).queue();
            return;
        }

        // Deferir a edição para evitar que a interação falhe por tempo.
        event.deferEdit().queue();

        // Roteador de ações com base no ID do botão
        switch (action) {
            case "delete-confirm":
                service.deletar(targetUserId);
                event.getHook().editOriginal("Personagem deletado com sucesso.")
                        .setComponents(Collections.emptyList()).queue();
                break;

            case "delete-cancel":
                event.getHook().editOriginal("Ação cancelada.")
                        .setComponents(Collections.emptyList()).queue();
                break;

            case "attr-add":
                String attributeToUpgrade = parts[2]; // Pega o nome do atributo do ID do botão
                handleAttributeUpgrade(event, service, targetUserId, attributeToUpgrade);
                break;
        }
    }

    /**
     * Método auxiliar para lidar especificamente com a lógica de upgrade de atributos.
     */
    private static void handleAttributeUpgrade(ButtonInteractionEvent event, PersonagemService service, String userId, String attributeToUpgrade) {
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);
        if (personagemOpt.isEmpty()) {
            event.getHook().editOriginal("Erro: Personagem não encontrado.")
                    .setComponents(Collections.emptyList()).queue();
            return;
        }

        Personagem p = personagemOpt.get();

        // Verifica se há pontos e se a regra de negócio permite o aumento
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

        // Atualiza o embed e os botões (desabilitando-os se os pontos acabarem)
        boolean hasPoints = p.getPontosDisponiveis() > 0;
        event.getHook().editOriginalEmbeds(EmbedManager.buildAtributosEmbed(p))
                .setComponents(
                        event.getMessage().getActionRows().get(0).withDisabled(!hasPoints)
                ).queue();
    }
}