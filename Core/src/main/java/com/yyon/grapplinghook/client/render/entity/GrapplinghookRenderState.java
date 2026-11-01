package com.yyon.grapplinghook.client.render.entity;

import com.yyon.grapplinghook.content.customization.type.enums.RopeStyle;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of everything {@link GrapplinghookEntityRenderer} needs, taken during extraction so
 * that submission never touches the live entity.
 */
@Environment(EnvType.CLIENT)
public class GrapplinghookRenderState extends EntityRenderState {

    /** False when the hook has no living holder - nothing is drawn. */
    public boolean visible;

    public final ItemStackRenderState hookItem = new ItemStackRenderState();

    /** Offset applied to the pose before drawing, for hooks rendered via an explicit anchor. */
    public double anchorOffsetX;
    public double anchorOffsetY;
    public double anchorOffsetZ;

    /** World-space hook anchor. */
    public Vec renderAnchor = new Vec(0, 0, 0);
    /** World-space hand position of the holder. */
    public Vec handPosition = new Vec(0, 0, 0);
    public int handDirection;

    /** Normalised direction the hook is facing. */
    public Vec attachDirection = new Vec(0, 0, 1);

    /** World-space rope segments, or null if there is no segment handler. */
    public List<Vec> segments = null;
    public double taut;

    public boolean glowingRope;
    public RopeStyle ropeStyle;

    public void setSegments(List<Vec> source) {
        if (source == null) {
            this.segments = null;
            return;
        }

        List<Vec> copy = new ArrayList<>(source.size());
        for (Vec v : source)
            copy.add(new Vec(v));

        this.segments = copy;
    }
}
