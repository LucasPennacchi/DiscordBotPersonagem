package com.bot.discord;

import com.bot.discord.comandos.ICommand;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Classe principal que ouve os eventos da API do Discord e os delega para os
 * handlers (gerenciadores) apropriados.
 * <p>
 * Após a refatoração, esta classe não contém mais a lógica de negócio dos comandos.
 * Sua responsabilidade é puramente de despachar eventos:
 * <ul>
 * <li>Eventos de Slash Command são roteados para a implementação de {@link ICommand} correspondente.</li>
 * <li>Eventos de Botão e Reação são roteados para o {@link InteractionManager}.</li>
 * </ul>
 * Ela também gerencia o estado de interações contínuas, como o rastreamento de
 * mensagens do comando /atributos.
 */
public class ComandosListener extends ListenerAdapter {

    private final PersonagemService service;

    /**
     * Mapeia o nome de um comando (String) para sua respectiva implementação (ICommand).
     * Isso permite uma busca e execução de comandos de forma eficiente (O(1)).
     */
    private final Map<String, ICommand> commandMap;

    /**
     * Mapa para rastrear mensagens ativas do comando /atributos.
     * Chave: ID da Mensagem, Valor: ID do Usuário que executou o comando.
     * Usamos ConcurrentHashMap para segurança em ambientes com múltiplas threads.
     */
    private final Map<Long, String> atributosMessageTracker;

    /**
     * Constrói o listener de comandos.
     * Este construtor injeta o serviço de personagem e inicializa o mapa de comandos,
     * carregando todas as implementações de {@link ICommand} disponíveis através do
     * {@link ComandosRegister}.
     *
     * @param service A instância de {@link PersonagemService} que contém a lógica de negócio.
     */
    public ComandosListener(PersonagemService service) {
        this.service = service;
        this.atributosMessageTracker = new ConcurrentHashMap<>();

        // Carrega a lista de comandos do registrador e a converte em um mapa
        // para acesso rápido e eficiente pelo nome do comando.
        List<ICommand> commands = ComandosRegister.loadCommands();
        this.commandMap = commands.stream()
                .collect(Collectors.toMap(ICommand::getName, Function.identity()));
    }

    /**
     * Intercepta todos os eventos de Slash Command.
     * O método busca o comando correspondente no {@code commandMap} e delega a
     * execução para o método {@code execute} da implementação encontrada.
     *
     * @param event O evento de interação de slash command.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Obtém o nome do comando invocado pelo usuário
        String commandName = event.getName();

        // Procura o objeto de comando correspondente no mapa
        ICommand command = commandMap.get(commandName);

        if (command != null) {
            // Se o comando for encontrado, executa-o, passando o evento e o serviço
            command.execute(event, service);
        } else {
            // Caso raro em que um comando é invocado mas não está mapeado
            event.reply("Erro: O comando '" + commandName + "' não foi encontrado no sistema.")
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Intercepta todos os eventos de clique em botão.
     * Este método simplesmente encaminha o evento para o {@link InteractionManager}
     * para que a lógica seja processada lá.
     *
     * @param event O evento de interação de botão.
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Passa a responsabilidade de tratar o evento para a classe especializada
        InteractionManager.handleButtonInteraction(event, service);
    }

    /**
     * Intercepta todos os eventos de adição de reação em mensagens.
     * Este método encaminha o evento para o {@link InteractionManager}, passando também
     * o mapa de rastreamento de mensagens para que a lógica possa ser aplicada apenas
     * às mensagens relevantes.
     *
     * @param event O evento de adição de reação.
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Passa a responsabilidade junto com o estado necessário (o tracker)
        InteractionManager.handleReactionAdd(event, service, atributosMessageTracker);
    }
}