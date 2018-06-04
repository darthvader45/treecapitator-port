package bspkrs.bspkrscore.fml.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ModGuiFactoryHandler implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }
    
    // In-game GUI options are not something I care to look into --AstroTibs
    /*
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return GuiBSConfig.class;
    }
	*/
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }
    // In-game GUI options are not something I care to look into --AstroTibs
    /*
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }
    */
    /*
     * Eclipse auto-added the bottom bullshit --AstroTibs
     * (non-Javadoc)
     * @see net.minecraftforge.fml.client.IModGuiFactory#hasConfigGui()
     */
	@Override
	public boolean hasConfigGui() {
		return false;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return null;
	}
}
