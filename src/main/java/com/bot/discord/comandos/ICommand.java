package com.bot.discord.comandos;

import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

/**
 * Interface que define o contrato para todos os slash commands do bot.
 * <p>
 * Cada comando individual (ex: /criar, /personagem) deve ser implementado como uma
 * classe que adere a esta interface. Isso permite que o sistema de registro e o
 * listener de comandos tratem todos os comandos de maneira uniforme, promovendo
 * baixo acoplamento e alta coesão.
 */
public interface ICommand {

    /**
     * Retorna o nome do comando, em letras minúsculas.
     * Este nome será usado para registrar e invocar o comando no Discord.
     *
     * @return O nome do comando.
     */
    String getName();

    /**
     * Retorna a descrição do comando que será exibida ao usuário no Discord.
     * Deve ser uma explicação concisa do que o comando faz.
     *
     * @return A descrição do comando.
     */
    String getDescription();

    /**
     * Retorna a lista de opções (argumentos) que o comando aceita.
     * <p>
     * Por padrão, retorna uma lista vazia para comandos que não necessitam de opções.
     * Classes de comando que requerem argumentos devem sobrescrever este método.
     *
     * @return Uma lista de {@link OptionData}, que define os parâmetros do comando.
     */
    default List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    /**
     * Contém a lógica de negócio a ser executada quando o comando é invocado.
     * Este é o método principal que será chamado pelo {@code ComandosListener}.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações
     * da invocação do comando (usuário, canal, opções fornecidas, etc.).
     * @param service A instância do {@link PersonagemService}, fornecendo acesso à
     * lógica de negócio e à camada de persistência.
     */
    void execute(SlashCommandInteractionEvent event, PersonagemService service);

    /**
     * Indica se o comando é restrito a administradores do servidor.
     * <p>
     * Por padrão, os comandos são públicos (retorna {@code false}). Comandos que devem
     * ser restritos a administradores precisam sobrescrever este método e retornar {@code true}.
     * O {@code ComandosRegister} usará esta informação para definir as permissões corretas.
     *
     * @return {@code true} se for um comando de admin, {@code false} caso contrário.
     */
    default boolean isAdminCommand() {
        return false;
    }
}