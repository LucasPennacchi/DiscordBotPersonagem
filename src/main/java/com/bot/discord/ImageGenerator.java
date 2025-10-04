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
 */
public final class ImageGenerator {

    private static final String SVG_TEMPLATE_PATH = "/images/ficha_template.svg";

    /**
     * Gera uma imagem da ficha do personagem com os atributos preenchidos.
     *
     * @param personagem O objeto Personagem com os atributos.
     * @return Um array de bytes representando a imagem PNG gerada.
     * @throws Exception Se ocorrer um erro ao ler o template ou renderizar a imagem.
     */
    public static byte[] generatePersonagemAttributesImage(Personagem personagem) throws Exception {
        String svgContent;
        try (InputStream is = ImageGenerator.class.getResourceAsStream(SVG_TEMPLATE_PATH)) {
            if (is == null) {
                throw new IOException("Template SVG não encontrado: " + SVG_TEMPLATE_PATH);
            }
            svgContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

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

    /**
     * Método auxiliar que usa Regex para encontrar um placeholder e substituir
     * tanto o seu conteúdo quanto a cor de preenchimento (fill) da tag que o contém.
     */
    private static String setAttributeValueAndColor(String svg, String placeholder, int value, String hexColor) {
        Pattern pattern = Pattern.compile("(<(text|tspan)[^>]*>)" + placeholder + "</\\2>");
        Matcher matcher = pattern.matcher(svg);

        if (matcher.find()) {
            String openingTag = matcher.group(1);
            String newStyleTag = openingTag.replaceAll("fill:#[0-9a-fA-F]{6};?", "")
                    .replaceAll("fill-opacity:[0-9.]+;?", "") // Remove também a opacidade do fill
                    .replace("style=\"", "style=\"fill:" + hexColor + ";");

            String replacement = newStyleTag + value + "</" + matcher.group(2) + ">";
            return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }
        return svg;
    }
}