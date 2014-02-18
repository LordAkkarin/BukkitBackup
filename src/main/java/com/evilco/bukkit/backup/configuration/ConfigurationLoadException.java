package com.evilco.bukkit.backup.configuration;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class ConfigurationLoadException extends ConfigurationException {

	public ConfigurationLoadException () {
		super ();
	}

	public ConfigurationLoadException (String message) {
		super (message);
	}

	public ConfigurationLoadException (String message, Throwable cause) {
		super (message, cause);
	}

	public ConfigurationLoadException (Throwable cause) {
		super (cause);
	}

	protected ConfigurationLoadException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super (message, cause, enableSuppression, writableStackTrace);
	}
}
