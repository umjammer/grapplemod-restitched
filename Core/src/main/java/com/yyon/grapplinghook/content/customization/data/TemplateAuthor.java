package com.yyon.grapplinghook.content.customization.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TemplateAuthor(String templateId, Component templateDisplayName, Component author) {

    public static final String DEFAULT_TEMPLATE_ID = "user-generated";
    public static final Component DEFAULT_AUTHOR = Component.translatable("grapple_template.author.unknown");
    public static final Component DEFAULT_NAME = Component.translatable("grapple_template.name.unknown");

    //todo: properly implement the codec across the project.

    // ComponentSerialization.FLAT_CODEC (component as a JSON string) was removed. Existing data and
    // templates may still hold that form, so decode it first and fall back to the regular codec.
    private static final Codec<Component> LEGACY_JSON_STRING = Codec.STRING.comapFlatMap(
            str -> {
                try {
                    return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(str));
                } catch (RuntimeException e) {
                    return DataResult.error(() -> "Not a JSON text component: " + str);
                }
            },
            Component::getString
    );

    private static final Codec<Component> COMPONENT_CODEC = Codec.of(
            ComponentSerialization.CODEC,
            Codec.withAlternative(LEGACY_JSON_STRING, ComponentSerialization.CODEC)
    );

    public static final Codec<TemplateAuthor> CODEC = RecordCodecBuilder.create(builder -> builder.apply3(
            TemplateAuthor::new,

            Codec.STRING.optionalFieldOf("template_id", DEFAULT_TEMPLATE_ID).forGetter(TemplateAuthor::templateId),
            COMPONENT_CODEC.optionalFieldOf("display_name", Component.empty()).forGetter(TemplateAuthor::templateDisplayName),
            COMPONENT_CODEC.optionalFieldOf("author", Component.empty()).forGetter(TemplateAuthor::author)
    ));

    public static final StreamCodec<? super RegistryFriendlyByteBuf, TemplateAuthor> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);


    public TemplateAuthor(String templateId, Component templateDisplayName, Component author) {
        this.templateId = templateId == null
                ? DEFAULT_TEMPLATE_ID
                : templateId;
        this.templateDisplayName = templateDisplayName == null
                ? DEFAULT_NAME
                : templateDisplayName;
        this.author = author == null
                ? DEFAULT_AUTHOR
                : author;
    }

    public TemplateAuthor(Component templateDisplayName, Component author) {
        this(DEFAULT_TEMPLATE_ID, templateDisplayName, author);
    }

    public TemplateAuthor() {
        this(DEFAULT_TEMPLATE_ID, DEFAULT_NAME, DEFAULT_AUTHOR);
    }

    public TemplateAuthor adopt(Component author) {
        return new TemplateAuthor(DEFAULT_TEMPLATE_ID, this.templateDisplayName, author);
    }

    public boolean isNameEmpty() {
        return this.templateDisplayName.getString().isEmpty();
    }
}
