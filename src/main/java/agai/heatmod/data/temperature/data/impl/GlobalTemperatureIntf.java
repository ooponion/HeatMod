package agai.heatmod.data.temperature.data.impl;

import agai.heatmod.annotators.ApiDoc;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@ApiDoc(description = "给forge自动注册用。也是capability用的接口,规定cap能使用的方法。")

@AutoRegisterCapability
public interface GlobalTemperatureIntf {//之后再写
}
