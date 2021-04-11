package net.himeki.mcmtfabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.himeki.mcmtfabric.command.ConfigCommand;

public class MCMT implements ModInitializer {
	@Override
	public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated) {
                ConfigCommand.register(dispatcher);
            }
        });
	}
}
