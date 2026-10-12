package faewulf.itemrename.util;

import faewulf.itemrename.inter.CustomFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Port note: the original Fabric mod injected the {@code faewulf.itemrename.inter.ICustomStyle}
 * interface into {@code net.minecraft.network.chat.Style} (via a class tweaker) and used a Mixin
 * with an {@code @Invoker} of the private Style constructor to create styles with custom
 * formatting, in particular with a full "reset" (clear color and all formatting flags).
 * <p>
 * This class replicates that exact behavior using only the public vanilla {@link Style} API.
 * The public {@code with*} methods copy every other field of the style, just like the original
 * Mixin did when re-creating the Style via its constructor, so the result is functionally
 * equivalent.
 */
public class styleEditor {

    public static Style withCustomFormatting(Style base, CustomFormatting... formattings) {
        Style result = base;

        for (CustomFormatting formatting : formattings) {

            // For default Formatting
            if (formatting.isFormatting()) {
                switch (formatting.getFormatting()) {
                    case OBFUSCATED:
                        result = result.withObfuscated(true);
                        break;
                    case BOLD:
                        result = result.withBold(true);
                        break;
                    case STRIKETHROUGH:
                        result = result.withStrikethrough(true);
                        break;
                    case UNDERLINE:
                        result = result.withUnderlined(true);
                        break;
                    case ITALIC:
                        result = result.withItalic(true);
                        break;
                    case RESET:
                        result = result
                                .withColor((TextColor) null)
                                .withBold(false)
                                .withItalic(false)
                                .withStrikethrough(false)
                                .withUnderlined(false)
                                .withObfuscated(false);
                        break;
                    default:
                        result = result.withColor(TextColor.fromLegacyFormat(formatting.getFormatting()));
                        break;
                }
            } else if (formatting.isCustomColor()) {
                result = result.withColor(TextColor.fromRgb(formatting.getCustomColorCode()));
            }
        }

        return result;
    }
}
