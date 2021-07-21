package ca.encodeous.mwx.mwxcompat1_13.nms;

import me.theminecoder.minecraft.nmsproxy.NMSProxy;
import me.theminecoder.minecraft.nmsproxy.annotations.NMSClass;
import me.theminecoder.minecraft.nmsproxy.annotations.NMSMethod;

@NMSClass(type = NMSClass.Type.NMS, className = "EntityLiving")
public interface NMSEntityLiving extends NMSProxy {
    @NMSMethod
    public NMSEntity getHandle();
}
