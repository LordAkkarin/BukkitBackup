package com.evilco.bukkit.backup.configuration;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public abstract class ConfigurationException extends Exception {

	public ConfigurationException () {
		super ();
	}

	public ConfigurationException (String message) {
		super (message);
	}

	public ConfigurationException (String message, Throwable cause) {
		super (message, cause);
	}

	public ConfigurationException (Throwable cause) {
		super (cause);
	}

	protected ConfigurationException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super (message, cause, enableSuppression, writableStackTrace);
	}
}
