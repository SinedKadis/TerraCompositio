package net.sinedkadis.terracompositio.mantle;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public class ModelItem {
    /** Model item for rendering no item */
    private static final ModelItem EMPTY = new ModelItem(new Vector3f(0, 0, 0), 0, 0, 0);

    /** Item center location in pixels */
    @Getter
    private final Vector3f center;
    /** Item size in pixels. If 0, item is skipped */
    @Getter
    private final float size;
    /** X axis rotation, applied first */
    @Getter
    private final float x;
    /** Y axis rotation, applied second */
    @Getter
    private final float y;
    @Getter
    private final ItemDisplayContext transform;

    /** Item center location in percentages, lazy loaded */
    private Vector3f centerScaled;
    /** Item size in percentages, lazy loaded */
    private Float sizeScaled;

    public ModelItem(Vector3f center, float size, float x, float y) {
        this(center, size, x, y, ItemDisplayContext.NONE);
    }

    /**
     * Gets the center for rendering this item, scaled for renderer
     * @return Scaled center
     */
    public Vector3f getCenterScaled() {
        if (centerScaled == null) {
            centerScaled = new Vector3f(center);
            centerScaled.mul(1f / 16f);
        }
        return centerScaled;
    }

    /**
     * Gets the size to render this item, scaled for the renderer
     * @return Size scaled
     */
    public float getSizeScaled() {
        if (sizeScaled == null) {
            sizeScaled = size / 16f;
        }
        return sizeScaled;
    }

    /**
     * Returns true if this model item is hidden, meaning no items should be rendered
     * @return  True if hidden
     */
    public boolean isHidden() {
        return size == 0;
    }

    /** Parses a transform type from a string */
    private static ItemDisplayContext parseTransformType(JsonObject json, String key) {
        String name = GsonHelper.getAsString(json, key, "none");
        switch (name) {
            case "none":   return ItemDisplayContext.NONE;
            case "head":   return ItemDisplayContext.HEAD;
            case "gui":    return ItemDisplayContext.GUI;
            case "ground": return ItemDisplayContext.GROUND;
            case "fixed":  return ItemDisplayContext.FIXED;
            case "thirdperson_righthand": return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            case "thirdperson_lefthand":  return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            case "firstperson_righthand": return ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
            case "firstperson_lefthand":  return ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        }
        throw new JsonSyntaxException("Unknown transform type " + name);
    }

    /**
     * Gets a model item from a JSON object
     * @param json  JSON object instance
     * @return  Model item object
     */
    public static ModelItem fromJson(JsonObject json) {
        // if the size is 0, skip rendering this item
        float size = GsonHelper.getAsFloat(json, "size");
        if (size == 0) {
            return ModelItem.EMPTY;
        }
        Vector3f center = FluidCuboid.arrayToVector(json, "center");
        float x = FluidCuboid.getRotation(json, "x");
        float y = FluidCuboid.getRotation(json, "y");
        ItemDisplayContext transformType = parseTransformType(json, "transform");
        return new ModelItem(center, size, x, y, transformType);
    }

    /**
     * Gets a list of model items from JSON
     * @param parent  Parent JSON object
     * @param key     Name of the array of model item objects
     * @return  List of model items
     */
    public static List<ModelItem> listFromJson(JsonObject parent, String key) {
        return parseList(parent, key, ModelItem::fromJson);
    }
    public static <T> List<T> parseList(JsonObject parent, String name, Function<JsonObject,T> mapper) {
        return parseList(GsonHelper.getAsJsonArray(parent, name), name, mapper);
    }
    public static <T> List<T> parseList(JsonArray array, String name, Function<JsonObject,T> mapper) {
        return parseList(array, name, (element, s) -> mapper.apply(GsonHelper.convertToJsonObject(element, s)));
    }
    public static <T> List<T> parseList(JsonArray array, String name, BiFunction<JsonElement,String,T> mapper) {
        if (array.size() == 0) {
            throw new JsonSyntaxException(name + " must have at least 1 element");
        }
        // build the list
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < array.size(); i++) {
            builder.add(mapper.apply(array.get(i), name + "[" + i + "]"));
        }
        return builder.build();
    }
}

