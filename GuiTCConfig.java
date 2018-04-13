import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiTCConfig extends GuiConfig
{
    public GuiTCConfig(GuiScreen parent)
    {
        super(parent, getProps(), Reference.MODID, false, false, GuiConfig.getAbridgedConfigPath(TCConfigHandler.instance().getConfig().toString()));
    }

    private static List<IConfigElement> getProps()
    {
        // Make sure the local objects contain our local settings
        TreecapitatorMod.instance.nbtManager().registerLocalInstances();

        List<IConfigElement> props = new ArrayList<IConfigElement>();
        for (String catName : TCConfigHandler.instance().getConfig().getCategoryNames())
            if (!catName.contains(Configuration.CATEGORY_SPLITTER))
                props.add(new ConfigElement(TCConfigHandler.instance().getConfig().getCategory(catName)));

        return props;
}
