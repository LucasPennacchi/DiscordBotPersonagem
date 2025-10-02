package com.bot.discord.comandos;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Optional;

public class CriarCommand implements ICommand {
    @Override
    public String getName() {
        return "criar";
    }

    @Override
    public String getDescription() {
        return "Cria um novo personagem de RPG.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "nome", "O nome do seu personagem.", false),
                new OptionData(OptionType.INTEGER, "nivel", "O nível inicial do seu personagem.", false)
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();
        String userId = event.getUser().getId();

        if (service.buscarPorUsuario(userId).isPresent()) {
            event.getHook().sendMessage("Você já possui um personagem! Use `/personagem` para vê-lo.").setEphemeral(true).queue();
            return;
        }

        String nome = Optional.ofNullable(event.getOption("nome")).map(OptionMapping::getAsString).orElse("Aventureiro");
        int nivel = Optional.ofNullable(event.getOption("nivel")).map(OptionMapping::getAsInt).orElse(1);

        if (nivel < 1) {
            event.getHook().sendMessage("O nível não pode ser menor que 1.").setEphemeral(true).queue();
            return;
        }

        Personagem novo = service.criarPersonagem(userId, nome, nivel);
        event.getHook().sendMessage("Personagem **" + novo.getNome() + "** criado com sucesso! Use `/personagem` para ver sua ficha.").queue();
    }
}