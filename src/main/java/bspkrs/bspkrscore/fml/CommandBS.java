package bspkrs.bspkrscore.fml;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.ChatComponentText;
import net.minecraft.util.text.TextComponentString; //AstroTibs
import bspkrs.util.ModVersionChecker;

@SuppressWarnings("unchecked")
public class CommandBS extends CommandBase
{
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		// TODO Auto-generated method stub
		// Dunno what to do with this lol --AstroTibs
	}
	
    @SuppressWarnings("rawtypes")
    private static List version = new ArrayList();

    static
    {
        version.add("version");
    }

    @Override
    public String getName()//getCommandName() --AstroTibs
    {
        return "bs";
    }

    @Override
    public String getUsage(ICommandSender sender)//getCommandUsage(ICommandSender sender)
    {
        return "commands.bs.usage";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 1;
    }
    
    /*
     * I don't know what to do with the below bullshit. --AstroTibs
     */
    
    /*
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return true;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException
    {
        if (!bspkrsCoreMod.instance.allowUpdateCheck)
            throw new WrongUsageException("commands.bs.disabled");

        if (args.length != 2)
            throw new WrongUsageException("commands.bs.usage");

        if (!args[0].equalsIgnoreCase("version"))
            throw new WrongUsageException("commands.bs.usage");

        String[] message = ModVersionChecker.checkVersionForMod(args[1]);

        for (String s : message)
            sender.addChatMessage(new TextComponentString(s));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, ModVersionChecker.getVersionCheckerMap().keySet().toArray(new String[] {})) : args.length == 1 ? version : null;
    }
    */
    
    //Dunno if the below will work --AstroTibs
    @Override
    public int compareTo(ICommand object)
    {
        if (object instanceof CommandBase)
            return this.getName().compareTo(((CommandBase) object).getName());

        return 0;
    }
    
}
