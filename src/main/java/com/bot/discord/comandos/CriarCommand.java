package com.bot.discord.comandos;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /criar.
 * <p>
 * Este comando permite que um usuário crie um novo personagem de RPG no sistema.
 * Ele verifica se o usuário já possui um personagem para evitar duplicatas.
 * Os argumentos "nome" e "nível" são opcionais e possuem valores padrão.
 * Esta classe segue o contrato definido pela interface {@link ICommand}.
 */
public class CriarCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "criar".
     */
    @Override
    public String getName() {
        return "criar";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Cria um novo personagem de RPG.".
     */
    @Override
    public String getDescription() {
        return "Cria um novo personagem de RPG.";
    }

    /**
     * Define as opções (argumentos) para o comando /criar.
     *
     * @return Uma lista contendo as opções "nome" (String) e "nível" (Integer),
     * ambas opcionais.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "nome", "O nome do seu personagem.", false),
                new OptionData(OptionType.INTEGER, "nivel", "O nível inicial do seu personagem.", false)
        );
    }

    /**
     * Executa a lógica do comando /criar.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts da interação.
     * 2. Verifica se o usuário já possui um personagem. Se sim, envia um erro.
     * 3. Processa as opções "nome" and "nível", aplicando valores padrão se não forem fornecidas.
     * 4. Valida se o nível é um valor positivo.
     * 5. Chama o {@link PersonagemService} para criar e persistir o novo personagem.
     * 6. Envia uma mensagem de sucesso confirmando a criação.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // Adia a resposta para dar tempo de processar a lógica sem o Discord achar que o bot travou.
        event.deferReply().queue();

        String userId = event.getUser().getId();

        // 1. Verifica se o usuário já possui um personagem.
        if (service.buscarPorUsuario(userId).isPresent()) {
            event.getHook().sendMessage("Você já possui um personagem! Use `/personagem` para vê-lo.")
                    .setEphemeral(true).queue();
            return;
        }

        // 2. Processa as opções com valores padrão usando Optional para evitar NullPointerException.
        String nome = Optional.ofNullable(event.getOption("nome"))
                .map(OptionMapping::getAsString)
                .orElse("Aventureiro"); // Valor padrão para o nome

        int nivel = Optional.ofNullable(event.getOption("nivel"))
                .map(OptionMapping::getAsInt)
                .orElse(1); // Valor padrão para o nível

        // 3. Valida os dados de entrada.
        if (nivel < 1) {
            event.getHook().sendMessage("O nível não pode ser menor que 1.")
                    .setEphemeral(true).queue();
            return;
        }

        // 4. Chama o serviço para criar o personagem.
        Personagem novoPersonagem = service.criarPersonagem(userId, nome, nivel);

        // 5. Envia a confirmação para o usuário.
        event.getHook().sendMessage("Personagem **" + novoPersonagem.getNome() + "** criado com sucesso! Use `/personagem` para ver sua ficha.").queue();
    }
}