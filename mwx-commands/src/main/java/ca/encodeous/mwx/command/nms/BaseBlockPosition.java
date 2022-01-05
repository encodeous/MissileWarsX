package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSField;

@NMSClass(type = NMSClass.NMSType.NMS, value = "core.BaseBlockPosition")
public interface BaseBlockPosition extends NMSProxy {
    /**
     * Gets the x coordinate
     */
    @NMSField(NMSField.Type.GETTER)
    public int a();
    /**
     * Gets the y coordinate
     */
    @NMSField(NMSField.Type.GETTER)
    public int b();
    /**
     * Gets the z coordinate
     */
    @NMSField(NMSField.Type.GETTER)
    public int c();
}
