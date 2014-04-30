package com.nocuous.genworld;

import net.minecraft.util.IProgressUpdate;
import net.minecraft.command.ICommandSender;

public class SaveChunksProgressUpdate implements IProgressUpdate {

	private ICommandSender output;

	public SaveChunksProgressUpdate(ICommandSender sender) {
		output = sender;
	}

	public void close() {
		output = null;
	}

	private void printOutput(String text) {
		output.addChatMessage(new net.minecraft.util.ChatComponentText(text));
	}

	@Override
	public void displayProgressMessage(String var1) {
		// TODO Auto-generated method stub
		printOutput(var1);
	}

	@Override
	public void resetProgressAndMessage(String var1) {
		// TODO Auto-generated method stub
		printOutput("Progress Update - reset: " + var1);
	}

	@Override
	public void resetProgresAndWorkingMessage(String var1) {
		// TODO Auto-generated method stub
		printOutput("Progress Update - reset&working: " + var1);
	}

	@Override
	public void setLoadingProgress(int var1) {
		// TODO Auto-generated method stub
		printOutput("Progress Update - loading: " + var1);
	}

	@Override
	public void func_146586_a() {
		// TODO Auto-generated method stub

	}

}
