///*
// * Copyright (c) 2024 TeamMoeg
// *
// * This file is part of Frosted Heart.
// *
// * Frosted Heart is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, version 3.
// *
// * Frosted Heart is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
// *
// */
//
//package agai.heatmod.examples;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import lombok.Getter;
//import lombok.Setter;
//import net.minecraft.core.BlockPos;
//
//import java.util.Objects;
//
//
//public class CubicHeatArea implements IHeatArea {
//    public static Codec<CubicHeatArea> CODEC = RecordCodecBuilder.create(t -> t.group(BlockPos.CODEC.fieldOf("pos").forGetter(o -> o.center),
//            Codec.INT.fieldOf("r").forGetter(o -> o.r),
//            Codec.INT.fieldOf("v").forGetter(o -> o.value)).apply(t, CubicHeatArea::new));
//    BlockPos center;
//    int r;
//    @Getter
//    @Setter
//    int value;
//
//
//    public CubicHeatArea(BlockPos center, int range, int tempMod) {
//        this.center = center;
//        this.r = range;
//        this.value = tempMod;
//    }
//
//
//    public int getCenterX() {
//        return center.getX();
//    }
//
//    public int getCenterY() {
//        return center.getY();
//    }
//
//    public int getCenterZ() {
//        return center.getZ();
//    }
//
//    public int getRadius() {
//        return r;
//    }
//
//    @Override
//    public int getTemperatureAt(int x, int y, int z) {
//        if (isEffective(x, y, z))
//            return value;
//        return 0;
//    }
//
//    @Override
//    public float[] getStructData() {
//        return new float[] {center.getX() + 0.5f, center.getY() + 0.5f, center.getZ() + 0.5f, 0, value-20, getRadius()+0.005f, 0, 0};
//    }
//
//    @Override
//    public float getValueAt(BlockPos pos) {
//        return value;
//    }
//
//    @Override
//    public boolean isEffective(int x, int y, int z) {
//        return Math.abs(x - getCenterX()) <= r && Math.abs(y - getCenterY()) <= r && Math.abs(z - getCenterZ()) <= r;
//    }
//
//    @Override
//    public BlockPos getCenter() {
//        return center;
//    }
//
//
//    @Override
//    public String toString() {
//        return "CubicHeatArea [center=" + center + ", r=" + r + ", value=" + value + "]";
//    }
//
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(center, r, value);
//	}
//
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (getClass() != obj.getClass()) return false;
//		CubicHeatArea other = (CubicHeatArea) obj;
//		return Objects.equals(center, other.center) && r == other.r && value == other.value;
//	}
//
//}
