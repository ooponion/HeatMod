//package agai.heatmod.content.temperature.player;
//
//import com.teammoeg.frostedheart.content.climate.player.PlayerTemperatureData.BodyPart;
//import lombok.Getter;
//import lombok.Setter;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.level.Level;
//
//import java.util.EnumMap;
//
//public class HeatingDeviceContext {
//    public static class BodyPartContext {
//        @Getter
//        private float bodyTemperature;
//        @Getter
//        @Setter
//        private float effectiveTemperature;
//        @Getter
//        @Setter
//        private float feelTemperature;
//
//        private BodyPartContext(float bodyTemperature, float effectiveTemperature) {
//            super();
//            this.bodyTemperature = bodyTemperature;
//            this.effectiveTemperature = effectiveTemperature;
//            this.feelTemperature = effectiveTemperature;
//        }
//
//        private BodyPartContext() {
//            super();
//        }
//
//    }
//
//    @Getter
//    ServerPlayer player;
//    public static EnumMap<BodyPart, BodyPartContext> partData = new EnumMap<>(BodyPart.class);
//
//    HeatingDeviceContext(ServerPlayer player) {
//        super();
//        this.player = player;
//        for (BodyPart bp : BodyPart.values()) {
//            partData.put(bp, new BodyPartContext());
//        }
//    }
//
//    public BodyPartContext getPartData(BodyPart part) {
//        return partData.get(part);
//    }
//
//    public void setPartData(BodyPart part, float bodyTemperature, float effectiveTemperature) {
//        BodyPartContext ctx = getPartData(part);
//        ctx.bodyTemperature = bodyTemperature;
//        ctx.effectiveTemperature = effectiveTemperature;
//        ctx.feelTemperature = effectiveTemperature;
//    }
//
//    public float getEffectiveTemperature(BodyPart part) {
//        return getPartData(part).effectiveTemperature;
//    }
//
//    public float getBodyTemperature(BodyPart part) {
//        return getPartData(part).bodyTemperature;
//    }
//
//    public void setEffectiveTemperature(BodyPart part, float value) {
//        getPartData(part).effectiveTemperature = value;
//    }
//
//    public void setBodyTemperature(BodyPart part, float value) {
//        getPartData(part).bodyTemperature = value;
//    }
//
//    public void addEffectiveTemperature(BodyPart part, float value) {
//        getPartData(part).effectiveTemperature += value;
//    }
//
//    public void setFeelTemperature(BodyPart part, float value) {
//        getPartData(part).feelTemperature += value;
//    }
//
//    public float getFeelTemperature(BodyPart part) {
//        return getPartData(part).feelTemperature;
//    }
//
//    public void addFeelTemperature(BodyPart part, float value) {
//        getPartData(part).feelTemperature += value;
//    }
//
//    public Level getLevel() {
//        return player.level();
//    }
//}
