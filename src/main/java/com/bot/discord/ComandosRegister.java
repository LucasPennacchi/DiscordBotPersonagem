package com.bot.discord;

import com.bot.discord.comandos.*; // Garante que todas as suas classes de comando sejam importadas
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária responsável por descobrir, construir e registrar
 * todos os slash commands da aplicação na API do Discord.
 * <p>
 * Esta classe centraliza o registro de comandos, tornando o processo de
 * adicionar novos comandos mais simples e organizado.
 */
public final class ComandosRegister {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private ComandosRegister() {}

    /**
     * Carrega e instancia todos os comandos disponíveis na aplicação.
     * <p>
     * <b>Importante:</b> Para que um novo comando seja registrado e funcione,
     * uma nova instância dele deve ser adicionada a esta lista.
     *
     * @return Uma lista contendo uma instância de cada comando que implementa {@link ICommand}.
     */
    public static List<ICommand> loadCommands() {
        return List.of(
                // Comandos de Usuário
                new CriarCommand(),
                new PersonagemCommand(),
                new NomeCommand(),
                new FotoCommand(),
                new DeletarCommand(),
                new AtributosCommand(),

                // Comandos de Administrador
                new VerCommand(),
                new UparCommand()
        );
    }

    /**
     * Constrói a lista de dados dos comandos e os envia para o Discord para
     * serem registrados ou atualizados.
     * <p>
     * Este método lê a lista de comandos de {@link #loadCommands()}, constrói
     * a estrutura de dados que a JDA necessita (incluindo nome, descrição, opções
     * e permissões) e enfileira a requisição de atualização para o Discord.
     *
     * @param jda A instância principal da JDA, necessária para se comunicar com o Discord.
     */
    public static void register(JDA jda) {
        List<ICommand> commands = loadCommands();
        List<CommandData> commandDataList = new ArrayList<>();

        for (ICommand command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.getName(), command.getDescription());
            slashCommand.addOptions(command.getOptions());

            // Verifica se o comando é restrito para administradores
            if (command.isAdminCommand()) {
                slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
            }

            commandDataList.add(slashCommand);
        }

        // Envia a lista de comandos para o Discord.
        // O Discord irá criar os novos e atualizar os existentes.
        jda.updateCommands().addCommands(commandDataList).queue(
                success -> System.out.println("Comandos globais registrados com sucesso!"),
                error -> System.err.println("Erro ao registrar comandos globais: " + error)
        );
    }
}