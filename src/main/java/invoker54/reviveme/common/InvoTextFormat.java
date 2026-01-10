package invoker54.reviveme.common;

import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.stream.Stream;

public class InvoTextFormat {

    public static TextFormatting[] filter(TextFormatting... styles){
        Stream<TextFormatting> formatStream = Arrays.stream(styles);
        //Filter bold text
        formatStream = formatStream.filter(f -> f != TextFormatting.BOLD || ReviveMeConfig.useBoldText);

        return formatStream.toArray(TextFormatting[]::new);
    }
}
