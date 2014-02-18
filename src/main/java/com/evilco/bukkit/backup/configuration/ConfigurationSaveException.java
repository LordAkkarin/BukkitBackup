package com.evilco.bukkit.backup.configuration;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class ConfigurationSaveException extends ConfigurationException {

	public ConfigurationSaveException () {
		super ();
	}

	public ConfigurationSaveException (String message) {
		super (message);
	}

	public ConfigurationSaveException (String message, Throwable cause) {
		super (message, cause);
	}

	public ConfigurationSaveException (Throwable cause) {
		super (cause);
	}

	protected ConfigurationSaveException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super (message, cause, enableSuppression, writableStackTrace);
	}
}
