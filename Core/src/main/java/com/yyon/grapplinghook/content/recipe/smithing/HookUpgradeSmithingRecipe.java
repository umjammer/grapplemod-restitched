package com.yyon.grapplinghook.content.recipe.smithing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yyon.grapplinghook.content.customization.data.HookCustomization;
import com.yyon.grapplinghook.content.customization.type.CustomizationProperty;
import com.yyon.grapplinghook.content.item.GrapplehookItem;
import com.yyon.grapplinghook.content.registry.internal.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Smithing recipe that turns on customization properties of a grappling hook.
 * Based on SimpleSmithingRecipe: SmithingTransformRecipe's serializer type can no longer be overridden.
 */
public class HookUpgradeSmithingRecipe extends SimpleSmithingRecipe {

    private static final Codec<List<CustomizationProperty<?>>> PROPERTY_LIST_CODEC = CustomizationProperty.KEY_CODEC.listOf();

    public static final MapCodec<HookUpgradeSmithingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            Ingredient.CODEC.fieldOf("template").forGetter(HookUpgradeSmithingRecipe::template),
            Ingredient.CODEC.fieldOf("addition").forGetter(HookUpgradeSmithingRecipe::addition),
            PROPERTY_LIST_CODEC.fieldOf("applies").forGetter(HookUpgradeSmithingRecipe::applies),
            PROPERTY_LIST_CODEC.optionalFieldOf("excludes_if_any", List.of()).forGetter(HookUpgradeSmithingRecipe::excludesIfAny)
    ).apply(inst, HookUpgradeSmithingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<CustomizationProperty<?>>> PROPERTY_LIST_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(PROPERTY_LIST_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, HookUpgradeSmithingRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, HookUpgradeSmithingRecipe::template,
            Ingredient.CONTENTS_STREAM_CODEC, HookUpgradeSmithingRecipe::addition,
            PROPERTY_LIST_STREAM_CODEC, HookUpgradeSmithingRecipe::applies,
            PROPERTY_LIST_STREAM_CODEC, HookUpgradeSmithingRecipe::excludesIfAny,
            HookUpgradeSmithingRecipe::new
    );

    public static final RecipeSerializer<HookUpgradeSmithingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient template;
    private final Ingredient addition;
    private final List<CustomizationProperty<?>> applies;
    private final List<CustomizationProperty<?>> excludesIfAny;

    public HookUpgradeSmithingRecipe(Recipe.CommonInfo commonInfo, Ingredient template, Ingredient addition,
                                     List<CustomizationProperty<?>> applies,
                                     List<CustomizationProperty<?>> excludesIfAny) {
        super(commonInfo);
        this.template = template;
        this.addition = addition;
        this.applies = List.copyOf(applies);
        this.excludesIfAny = List.copyOf(excludesIfAny);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ItemStack buildPreviewResult() {
        GrapplehookItem hook = ModItems.GRAPPLING_HOOK.get();
        ItemStack preview = hook.getDefaultInstance();
        HookCustomization custom = new HookCustomization();
        for (CustomizationProperty<?> property : this.applies) {
            custom.set((CustomizationProperty) property, Boolean.TRUE);
        }
        hook.applyCustomizations(preview, custom);
        return preview;
    }

    public Ingredient template() { return this.template; }
    public Ingredient addition() { return this.addition; }
    public List<CustomizationProperty<?>> applies() { return this.applies; }
    public List<CustomizationProperty<?>> excludesIfAny() { return this.excludesIfAny; }

    @Override
    public Optional<Ingredient> templateIngredient() { return Optional.of(this.template); }

    @Override
    public Ingredient baseIngredient() { return Ingredient.of(ModItems.GRAPPLING_HOOK.get()); }

    @Override
    public Optional<Ingredient> additionIngredient() { return Optional.of(this.addition); }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!this.template.test(input.template())) return false;
        if (!input.base().is(ModItems.GRAPPLING_HOOK.get())) return false;
        if (!this.addition.test(input.addition())) return false;

        GrapplehookItem hook = ModItems.GRAPPLING_HOOK.get();
        HookCustomization custom = hook.getCustomizationsOrDefault(input.base());
        for (CustomizationProperty<?> blocker : this.excludesIfAny) {
            if (Boolean.TRUE.equals(custom.get(blocker))) return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack base = input.base().copyWithCount(1);
        GrapplehookItem hook = ModItems.GRAPPLING_HOOK.get();
        HookCustomization custom = HookCustomization.copyAllFrom(hook.getCustomizationsOrDefault(base));
        for (CustomizationProperty<?> property : this.applies) {
            custom.set((CustomizationProperty) property, Boolean.TRUE);
        }
        hook.applyCustomizations(base, custom);
        return base;
    }

    @Override
    public RecipeSerializer<HookUpgradeSmithingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(this.template, this.baseIngredient(), this.addition));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SmithingRecipeDisplay(
                this.template.display(),
                this.baseIngredient().display(),
                this.addition.display(),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(this.buildPreviewResult())),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
        ));
    }
}
