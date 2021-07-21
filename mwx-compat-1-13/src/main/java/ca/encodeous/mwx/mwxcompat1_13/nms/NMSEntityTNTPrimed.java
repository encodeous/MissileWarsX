package ca.encodeous.mwx.mwxcompat1_13.nms;

import me.theminecoder.minecraft.nmsproxy.NMSProxy;
import me.theminecoder.minecraft.nmsproxy.annotations.NMSClass;
import me.theminecoder.minecraft.nmsproxy.annotations.NMSField;
import me.theminecoder.minecraft.nmsproxy.annotations.NMSMethod;

@NMSClass(type = NMSClass.Type.NMS, className = "EntityTNTPrimed")
public interface NMSEntityTNTPrimed extends NMSProxy {
    @NMSField(NMSField.Type.SETTER)
    void source(NMSEntityLiving entity);
    @NMSMethod
    public NMSEntity getHandle();
}
