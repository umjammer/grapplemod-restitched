package com.yyon.grapplinghook.util.model;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;

public class ModelPath {

    public static Supplier<Iterator<String>> ROOT_TO_LEFT_LEG = () -> List.of("left_leg").iterator();
    public static Supplier<Iterator<String>> ROOT_TO_RIGHT_LEG = () -> List.of("right_leg").iterator();

    public static Supplier<Iterator<String>> combine(Supplier<Iterator<String>> path, String... continued) {
        LinkedList<String> newPath = new LinkedList<>();
        path.get().forEachRemaining(newPath::add);
        newPath.addAll(Arrays.asList(continued));

        return newPath::iterator;
    }


    public static ModelPartData goTo(ModelData mesh, Iterator<String> path) {
        return goTo(mesh.getRoot(), path);
    }

    public static ModelPartData goTo(ModelPartData root, Iterator<String> path) {
        return path.hasNext()
                ? goTo(root.getChild(path.next()), path)
                : root;
    }

    public static ModelPart goTo(ModelPart root, Iterator<String> path) {
try {
        return path.hasNext()
                ? goTo(root.getChild(path.next()), path)
                : root;
} catch (NoSuchElementException e) {
 return root;
}
    }

}
