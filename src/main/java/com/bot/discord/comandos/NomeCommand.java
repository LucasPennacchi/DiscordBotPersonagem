package com.bot.discord.comandos;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /nome.
 * <p>
 * Este comando permite que um usuário altere o nome do seu personagem existente.
 * Ele requer um argumento de texto com o novo nome desejado. A alteração é
 * persistida no banco de dados.
 */
public class NomeCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "nome".
     */
    @Override
    public String getName() {
        return "nome";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Altera o nome do seu personagem.".
     */
    @Override
    public String getDescription() {
        return "Altera o nome do seu personagem.";
    }

    /**
     * Define as opções (argumentos) para o comando /nome.
     *
     * @return Uma lista contendo a opção "novo_nome" do tipo STRING,
     * que é obrigatória.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "novo_nome", "O novo nome para o personagem.", true)
        );
    }

    /**
     * Executa a lógica do comando /nome.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts.
     * 2. Verifica se o usuário possui um personagem. Se não, envia uma mensagem de erro.
     * 3. Obtém o novo nome a partir das opções do comando.
     * 4. Atualiza o nome no objeto Personagem.
     * 5. Chama o {@link PersonagemService} para salvar a alteração no banco de dados.
     * 6. Envia uma mensagem de sucesso confirmando a alteração.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        String userId = event.getUser().getId();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        // 1. Verifica se o personagem existe.
        if (personagemOpt.isEmpty()) {
            event.getHook().sendMessage("Você precisa ter um personagem para alterar o nome. Use `/criar`.")
                    .setEphemeral(true).queue();
            return;
        }

        // 2. Obtém o novo nome. A opção é obrigatória, então não precisamos checar por nulo.
        String novoNome = event.getOption("novo_nome").getAsString();

        Personagem personagem = personagemOpt.get();
        String nomeAntigo = personagem.getNome();

        // 3. Atualiza o nome do personagem e salva.
        personagem.setNome(novoNome);
        service.salvar(personagem);

        // 4. Envia a confirmação.
        String mensagem = String.format("Nome do personagem alterado de **%s** para **%s**!",
                nomeAntigo, novoNome);

        event.getHook().sendMessage(mensagem).queue();
    }
}