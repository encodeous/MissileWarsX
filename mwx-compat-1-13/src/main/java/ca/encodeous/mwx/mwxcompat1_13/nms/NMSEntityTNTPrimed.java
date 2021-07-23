package ca.encodeous.mwx.mwxcompat1_13.nms;


import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSField;
import ca.encodeous.simplenms.annotations.NMSMethod;

@NMSClass(type = NMSClass.NMSType.NMS, value = "EntityTNTPrimed")
public interface NMSEntityTNTPrimed extends NMSProxy {
    @NMSField(NMSField.Type.SETTER)
    void source(NMSEntityLiving entity);
    @NMSMethod
    public NMSEntity getHandle();
}
