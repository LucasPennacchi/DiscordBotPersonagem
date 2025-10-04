package com.bot.discord.comandos;

import com.bot.discord.DisplayManager;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PersonagemCommand implements ICommand {

    @Override
    public String getName() {
        return "personagem";
    }

    @Override
    public String getDescription() {
        return "Mostra sua ficha e permite gerenciar seus atributos.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply(true).queue();
        // Simplesmente chama o DisplayManager para fazer todo o trabalho pesado.
        // Passa 'true' para indicar que queremos mostrar os botões se houver pontos.
        DisplayManager.displayCharacterSheet(event.getHook(), event.getUser(), service, true, "Aqui está sua ficha:");
    }
}