package agai.heatmod.content.temperature.player;

import agai.heatmod.utils.Lang;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.EnumMap;
import java.util.Map;

public enum BodyPart implements StringRepresentable {
    HEAD(EquipmentSlot.HEAD, 0.1f, 0.1f, 1), // 10% area
    TORSO(EquipmentSlot.CHEST, 0.45f, 0.5f, 3), // 40% area

    HANDS(EquipmentSlot.MAINHAND, 0.05f, 0.00f, 1), // 5% area
    LEGS(EquipmentSlot.LEGS, 0.35f, 0.4f, 3), // 40% area
    FEET(EquipmentSlot.FEET, 0.05f, 0.00f, 1); // 5% area
    public static final BodyPart[] CoreParts = new BodyPart[]{HEAD, TORSO, LEGS};
    public final EquipmentSlot slot;
    public final float area;
    public final float affectsCore;
    public final int slotNum;
    private final static Map<EquipmentSlot, BodyPart> VANILLA_MAP = Util.make(new EnumMap<>(EquipmentSlot.class), t -> {
        for (BodyPart part : BodyPart.values())
            if (part.slot != null)
                t.put(part.slot, part);
    });

    BodyPart(EquipmentSlot slot, float area, float affectsCore, int slotNum) {
        this.slot = slot;
        this.area = area;
        this.affectsCore = affectsCore;
        this.slotNum = slotNum;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static BodyPart fromVanilla(EquipmentSlot es) {
        if (es == null) return null;
        return VANILLA_MAP.get(es);
    }

    public Component getName() {
        return Lang.translateGui("body_part." + getSerializedName());
    }

    public boolean canGenerateHeat() {
        switch (this) {
            case TORSO:
            case LEGS:
            case HEAD:
                return true;
        }
        return false;

    }

    public boolean isBodyEnd() {
        switch (this) {
            case FEET:
            case HANDS:
                return true;
        }
        return false;
    }

}