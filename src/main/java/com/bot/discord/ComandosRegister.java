package com.bot.discord;

import com.bot.discord.comandos.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por registrar todos os slash commands na API do Discord.
 */
public final class ComandosRegister {

    private ComandosRegister() {}

    /**
     * Carrega e instancia todos os comandos disponíveis na aplicação.
     * Para que um novo comando funcione, ele deve ser adicionado a esta lista.
     */
    public static List<ICommand> loadCommands() {
        return List.of(
                // Comandos de Usuário
                new CriarCommand(),
                new PersonagemCommand(),
                new NomeCommand(),
                new FotoCommand(),
                new DeletarCommand(),
                // Comandos de Administrador
                new VerCommand(),
                new UparCommand()
        );
    }

    /**
     * Constrói e envia a lista de comandos para o Discord registrar/atualizar.
     * @param jda A instância principal da JDA.
     */
    public static void register(JDA jda) {
        List<ICommand> commands = loadCommands();
        List<CommandData> commandDataList = new ArrayList<>();

        for (ICommand command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.getName(), command.getDescription());
            slashCommand.addOptions(command.getOptions());

            if (command.isAdminCommand()) {
                slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
            }
            commandDataList.add(slashCommand);
        }

        jda.updateCommands().addCommands(commandDataList).queue(
                success -> System.out.println("Comandos globais registrados com sucesso!"),
                error -> System.err.println("Erro ao registrar comandos globais: " + error)
        );
    }
}