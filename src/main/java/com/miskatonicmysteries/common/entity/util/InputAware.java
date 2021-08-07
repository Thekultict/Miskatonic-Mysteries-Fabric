package com.miskatonicmysteries.common.entity.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface InputAware {
    @Environment(EnvType.CLIENT)
    void handleInput(boolean jumping);
}
