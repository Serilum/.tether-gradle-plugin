package com.serilum.tether;

import com.serilum.tether.functions.LocalElementsFunctions;
import com.serilum.tether.functions.LocalFunctions;
import com.serilum.tether.functions.SettingsFunctions;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

public class TetherLocal implements Plugin<Settings> {
	@Override
	public void apply(Settings settings) {
		if (!SettingsFunctions.isToggledOn(settings, "tether_enabled")) {
			return;
		}

		boolean includedByModBuild = settings.getGradle().getParent() != null;
		if (includedByModBuild) {
			LocalElementsFunctions.addLocalElements(settings.getGradle());
			return;
		}

		if (LocalFunctions.isEnabled(settings)) {
			LocalFunctions.includeLocalLibraries(settings);
		}
	}
}
