package com.bot.discord;

import com.bot.model.Personagem;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitária para gerar imagens dinâmicas da ficha do personagem
 * usando um template SVG e a biblioteca Apache Batik.
 * <p>
 * ESTA VERSÃO implementa um cache estático para o conteúdo do template SVG,
 * lendo o arquivo do disco apenas uma vez na inicialização para otimizar a performance.
 */
public final class ImageGenerator {

    private static final String SVG_TEMPLATE_PATH = "/images/ficha_template.svg";

    // Cache para o conteúdo do template SVG, para evitar leituras repetidas do disco.
    private static final String svgTemplateContent;

    /**
     * Bloco estático que é executado uma única vez quando a classe é carregada pela JVM.
     * Ele lê o arquivo de template SVG e o armazena na variável estática 'svgTemplateContent'.
     */
    static {
        String content;
        try (InputStream is = ImageGenerator.class.getResourceAsStream(SVG_TEMPLATE_PATH)) {
            if (is == null) {
                throw new IOException("Template SVG não encontrado: " + SVG_TEMPLATE_PATH);
            }
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Template SVG '" + SVG_TEMPLATE_PATH + "' carregado para o cache com sucesso!");
        } catch (IOException e) {
            System.err.println("ERRO FATAL: Não foi possível carregar o template SVG para o cache.");
            e.printStackTrace();
            content = null;
        }
        svgTemplateContent = content;
    }

    /**
     * Gera uma imagem da ficha do personagem com os atributos preenchidos.
     *
     * @param personagem O objeto Personagem com os atributos.
     * @return Um array de bytes representando a imagem PNG gerada.
     * @throws Exception Se o template SVG não estiver carregado ou se ocorrer um erro na renderização.
     */
    public static byte[] generatePersonagemAttributesImage(Personagem personagem) throws Exception {
        if (svgTemplateContent == null) {
            throw new Exception("O template SVG não está carregado. Verifique os logs de inicialização.");
        }

        // Usa a versão em cache do SVG em vez de ler o arquivo novamente.
        String svgContent = svgTemplateContent;

        svgContent = setAttributeValueAndColor(svgContent, "_MENTE_", personagem.getMente(), "#d9a066");
        svgContent = setAttributeValueAndColor(svgContent, "_CORPO_", personagem.getCorpo(), "#d85762");
        svgContent = setAttributeValueAndColor(svgContent, "_VONTADE_", personagem.getVontade(), "#639bff");
        svgContent = setAttributeValueAndColor(svgContent, "_DESTREZA_", personagem.getDestreza(), "#37946e");

        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(new StringReader(svgContent));
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        transcoder.transcode(input, output);
        ostream.flush();
        return ostream.toByteArray();
    }

    private static String setAttributeValueAndColor(String svg, String placeholder, int value, String hexColor) {
        Pattern pattern = Pattern.compile("(<(text|tspan)[^>]*>)" + placeholder + "</\\2>");
        Matcher matcher = pattern.matcher(svg);

        if (matcher.find()) {
            String openingTag = matcher.group(1);
            String newStyleTag = openingTag.replaceAll("fill:#[0-9a-fA-F]{6};?", "")
                    .replaceAll("fill-opacity:[0-9.]+;?", "")
                    .replace("style=\"", "style=\"fill:" + hexColor + ";");
            String replacement = newStyleTag + value + "</" + matcher.group(2) + ">";
            return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }
        return svg;
    }
}