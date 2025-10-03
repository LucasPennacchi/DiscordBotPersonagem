package com.bot.discord.comandos;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /upar, que é restrito a administradores.
 * <p>
 * Este comando permite que um administrador aumente o nível de um personagem
 * de qualquer jogador no servidor. Ao fazer isso, o personagem também recebe
 * a quantidade correspondente de pontos de atributo para distribuir (3 por nível).
 */
public class UparCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "upar".
     */
    @Override
    public String getName() {
        return "upar";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "(Admin) Aumenta o nível de um jogador.".
     */
    @Override
    public String getDescription() {
        return "(Admin) Aumenta o nível de um jogador.";
    }

    /**
     * Define as opções (argumentos) para o comando /upar.
     *
     * @return Uma lista contendo as opções "usuario" (User) e "niveis" (Integer),
     * ambas obrigatórias.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "usuario", "O usuário que vai receber o nível.", true),
                new OptionData(OptionType.INTEGER, "niveis", "Quantidade de níveis para adicionar.", true)
        );
    }

    /**
     * Sobrescreve o método padrão para marcar este comando como exclusivo para administradores.
     * O {@code ComandosRegister} usará esta informação para restringir o uso do comando
     * apenas para membros com a permissão de Administrador.
     *
     * @return {@code true} para indicar que é um comando de administrador.
     */
    @Override
    public boolean isAdminCommand() {
        return true;
    }

    /**
     * Executa a lógica do comando /upar.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta de forma efêmera (privada para o administrador).
     * 2. Obtém o usuário alvo e a quantidade de níveis a adicionar das opções.
     * 3. Valida se a quantidade de níveis é um número positivo.
     * 4. Busca o personagem do usuário alvo. Se não existir, envia um erro.
     * 5. Se existir, chama o método {@code uparNivel} do {@link PersonagemService}.
     * 6. Envia uma mensagem de sucesso privada para o administrador confirmando a ação.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // A resposta é efêmera, pois é uma ação administrativa e o feedback
        // deve ser visível apenas para quem executou o comando.
        event.deferReply().setEphemeral(true).queue();

        User targetUser = event.getOption("usuario").getAsUser();
        int niveisParaAdicionar = event.getOption("niveis").getAsInt();

        // 1. Valida os dados de entrada.
        if (niveisParaAdicionar <= 0) {
            event.getHook().sendMessage("A quantidade de níveis para adicionar deve ser um número positivo.").queue();
            return;
        }

        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());

        // 2. Verifica se o personagem do usuário alvo existe.
        if (personagemOpt.isEmpty()) {
            event.getHook().sendMessage("O usuário " + targetUser.getAsMention() + " não possui um personagem.").queue();
            return;
        }

        Personagem personagem = personagemOpt.get();
        int nivelAntigo = personagem.getNivel();

        // 3. Chama o serviço para executar a lógica de negócio.
        service.uparNivel(personagem, niveisParaAdicionar);

        // 4. Envia a confirmação para o administrador.
        String mensagem = String.format(
                "Personagem de %s upado com sucesso!\n> **Nível**: %d -> %d\n> **Pontos Adicionados**: %d",
                targetUser.getAsMention(),
                nivelAntigo,
                personagem.getNivel(),
                niveisParaAdicionar * 3
        );

        event.getHook().sendMessage(mensagem).queue();
    }
}