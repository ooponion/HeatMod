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
//import net.minecraft.core.BlockPos;
//
//import java.util.Objects;
//
///**
// * Pillar Temperature Adjust, would adjust temperature in a pillar.
// */
//@Getter
//public class PillarHeatArea extends CubicHeatArea {
//    public static Codec<PillarHeatArea> CODEC = RecordCodecBuilder.create(t -> t.group(BlockPos.CODEC.fieldOf("pos").forGetter(o -> o.center),
//            Codec.INT.fieldOf("r").forGetter(o -> o.r),
//            Codec.INT.fieldOf("u").forGetter(o -> o.upper),
//            Codec.INT.fieldOf("d").forGetter(o -> o.lower),
//            Codec.INT.fieldOf("v").forGetter(o -> o.value)).apply(t, PillarHeatArea::new));
//    long r2;
//    int upper;
//    int lower;
//
//    public PillarHeatArea(BlockPos heatPos, int range, int u, int d, int tempMod) {
//        super(heatPos, range, tempMod);
//        r2 = (long) r * r;
//        this.upper = u;
//        this.lower = d;
//    }
//
//    @Override
//    public boolean isEffective(int x, int y, int z) {
//        if (y > upper + getCenterY() || y < getCenterY() - lower) return false;
//        long l = (long) Math.pow(x - getCenterX(), 2);
//        l += (long) Math.pow(z - getCenterZ(), 2);
//        return l <= r2;
//    }
//
//    @Override
//    public String toString() {
//        return "PillarHeatArea [center=" + center + ", r=" + r + ", value=" + value + ", upper=" + upper + ", lower=" + lower + "]";
//    }
//
//    @Override
//    public float[] getStructData() {
//        return new float[] {getCenterX() + 0.5f, getCenterY() + 0.5f, getCenterZ() + 0.5f, 1, value-20, getRadius(), getCenterY()+upper+0.005f, getCenterY()-lower-0.005f};
//    }
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(lower, upper);
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (obj == null) return false;
//		if (getClass() != obj.getClass()) return false;
//		PillarHeatArea other = (PillarHeatArea) obj;
//		return lower == other.lower && upper == other.upper;
//	}
//}
