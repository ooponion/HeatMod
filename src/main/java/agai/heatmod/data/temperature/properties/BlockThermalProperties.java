package agai.heatmod.data.temperature.properties;

import agai.heatmod.annotators.ApiDoc;
import agai.heatmod.annotators.InTest;
import agai.heatmod.annotators.InWorking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.Serializable;

/**可能以后会重新使用*/
@Deprecated
@ApiDoc(description = "* 职责：定义每种方块的热学属性\n" +
        " * 核心功能：\n" +
        " * 存储比热容、导热系数、 emissivity (辐射率)\n" +
        " * 提供隔热等级数据\n" )
public class BlockThermalProperties implements Serializable {
    public static final Codec<BlockThermalProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("emissivity").forGetter(prop -> prop.emissivity),
                    Codec.FLOAT.fieldOf("specificHeatCapacity").forGetter(prop -> prop.specificHeatCapacity),
                    Codec.FLOAT.fieldOf("thermalConductivity").forGetter(prop -> prop.thermalConductivity),
                    Codec.FLOAT.fieldOf("mass").forGetter(prop -> prop.mass),
                    Codec.INT.fieldOf("insulationLevel").forGetter(prop -> prop.insulationLevel),
                    Codec.BOOL.fieldOf("isActiveSource").forGetter(prop -> prop.isActiveSource),
                    Codec.FLOAT.fieldOf("sourcePower").forGetter(prop -> prop.sourcePower),
                    Codec.FLOAT.fieldOf("temperature").forGetter(prop -> prop.temperature)
            ).apply(instance, BlockThermalProperties::new)
    );
    private float temperature;

    private float emissivity;//(0~1)
    private float specificHeatCapacity;
    private float thermalConductivity;
    private float mass;

    private int insulationLevel;//0-10
    private boolean isActiveSource;
    private float sourcePower;

    public BlockThermalProperties( float emissivity, float specificHeatCapacity,
                                  float thermalConductivity, float mass, int insulationLevel,
                                  boolean isActiveSource, float sourcePower,float temperature) {

        this.emissivity = emissivity;
        this.specificHeatCapacity = specificHeatCapacity;
        this.thermalConductivity = thermalConductivity;
        this.mass = mass;
        this.insulationLevel = insulationLevel;
        this.isActiveSource = isActiveSource;
        this.sourcePower = sourcePower;
        this.temperature = temperature;
    }

    public float getSourceTemperature() {
        return temperature;
    }

    public void setSourceTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getEmissivity() {
        return emissivity;
    }

    public void setEmissivity(float emissivity) {
        this.emissivity = emissivity;
    }

    public float getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    public void setSpecificHeatCapacity(float specificHeatCapacity) {
        this.specificHeatCapacity = specificHeatCapacity;
    }

    public float getThermalConductivity() {
        return thermalConductivity;
    }

    public void setThermalConductivity(float thermalConductivity) {
        this.thermalConductivity = thermalConductivity;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public int getInsulationLevel() {
        return insulationLevel;
    }

    public void setInsulationLevel(int insulationLevel) {
        this.insulationLevel = insulationLevel;
    }

    public float getHeatCapacity() {
        return specificHeatCapacity*mass;
    }

    public boolean isActiveSource() {
        return isActiveSource;
    }

    public void setActiveSource(boolean activeSource) {
        isActiveSource = activeSource;
    }

    public float getSourcePower() {
        return sourcePower;
    }

    public void setSourcePower(float sourcePower) {
        this.sourcePower = sourcePower;
    }
}
