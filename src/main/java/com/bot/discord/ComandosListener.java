package com.bot.discord;

import com.bot.discord.comandos.ICommand;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ouve os eventos do Discord e os delega para os handlers apropriados.
 * A responsabilidade desta classe é puramente de despachar eventos.
 */
public class ComandosListener extends ListenerAdapter {

    private final PersonagemService service;
    private final Map<String, ICommand> commandMap;

    /**
     * Constrói o listener de comandos, injetando o serviço de personagem e
     * carregando todos os comandos disponíveis através do {@link ComandosRegister}.
     *
     * @param service A instância de {@link PersonagemService}.
     */
    public ComandosListener(PersonagemService service) {
        this.service = service;
        List<ICommand> commands = ComandosRegister.loadCommands();
        this.commandMap = commands.stream()
                .collect(Collectors.toMap(ICommand::getName, Function.identity()));
    }

    /**
     * Intercepta todos os eventos de Slash Command e os delega para a
     * implementação de {@link ICommand} correspondente.
     *
     * @param event O evento de interação de slash command.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        ICommand command = commandMap.get(event.getName());
        if (command != null) {
            command.execute(event, service);
        } else {
            event.reply("Erro: O comando '" + event.getName() + "' não foi encontrado.").setEphemeral(true).queue();
        }
    }

    /**
     * Intercepta todos os eventos de clique em botão e os encaminha para o
     * {@link InteractionManager} para processamento.
     *
     * @param event O evento de interação de botão.
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        InteractionManager.handleButtonInteraction(event, service);
    }
}