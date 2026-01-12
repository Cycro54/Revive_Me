package invoker54.reviveme.common;

import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.ChatFormatting;

import java.util.Arrays;
import java.util.stream.Stream;

public class InvoTextFormat {

    public static ChatFormatting[] filter(ChatFormatting... styles){
        Stream<ChatFormatting> formatStream = Arrays.stream(styles);
        //Filter bold text
        formatStream = formatStream.filter(f -> f != ChatFormatting.BOLD || ReviveMeConfig.useBoldText);

        return formatStream.toArray(ChatFormatting[]::new);
    }
}
