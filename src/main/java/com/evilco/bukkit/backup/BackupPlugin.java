package com.evilco.bukkit.backup;

import com.evilco.bukkit.backup.configuration.BackupConfiguration;
import com.evilco.bukkit.backup.configuration.BackupStorage;
import com.evilco.bukkit.backup.configuration.ConfigurationException;
import com.evilco.bukkit.backup.task.AmazonGlacierUploadTask;
import com.evilco.bukkit.backup.task.BackupThread;
import com.evilco.bukkit.backup.task.IStorageTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class BackupPlugin extends JavaPlugin implements Runnable {

	/**
	 * Stores the backup configuration.
	 */
	protected BackupConfiguration configuration = null;

	/**
	 * Indicates whether the plugin instance is broken.
	 */
	protected boolean isBroken = false;

	/**
	 * Stores a list of backups.
	 */
	protected BackupStorage storage = null;

	/**
	 * Stores the storage task.
	 */
	protected IStorageTask storageTask = null;

	/**
	 * Returns the current configuration instance.
	 * @return
	 */
	public BackupConfiguration getConfiguration () {
		return this.configuration;
	}

	/**
	 * Returns the backup list.
	 * @return
	 */
	public BackupStorage getStorage () {
		return this.storage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDisable () {
		super.onDisable ();

		// abort if something broke
		if (this.isBroken) return;

		// save configuration file
		try {
			this.configuration.save (BackupConfiguration.getConfigurationFile (this.getDataFolder ()));
		} catch (ConfigurationException ex) {
			// log problem
			this.getLogger ().log (Level.SEVERE, "Could not save the backup configuration.", ex);
		}

		// save storage file
		try {
			this.storage.save (BackupStorage.getDatabaseFile (this.getDataFolder ()));
		} catch (ConfigurationException ex) {
			// log problem
			this.getLogger ().log (Level.SEVERE, "Could not save backup storage.", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEnable () {
		super.onEnable ();

		// get configuration file
		try {
			this.configuration = BackupConfiguration.newInstance (this.getDataFolder ());
		} catch (ConfigurationException ex) {
			// log problem
			this.getLogger ().log (Level.SEVERE, "Could not load the backup configuration.", ex);
			this.isBroken = true;

			// disable plugin
			this.getServer ().getPluginManager ().disablePlugin (this);

			// stop execution here
			return;
		}

		// get storage database
		try {
			this.storage = BackupStorage.newInstance (this.getDataFolder ());
		} catch (ConfigurationException ex) {
			// log problem
			this.getLogger ().log (Level.SEVERE, "Could not load the storage database.", ex);
			this.isBroken = true;

			// disable plugin
			this.getServer ().getPluginManager ().disablePlugin (this);

			// stop execution here
			return;
		}

		// create storage task instance
		switch (this.configuration.storageMethod) {
			case AMAZON_GLACIER:
				this.getLogger ().info ("Using Amazon Glacier storage.");

				// construct task
				this.storageTask = new AmazonGlacierUploadTask (this);
				break;
			case NONE:
				this.getLogger ().info ("Using file system storage.");
				break;
		}

		// schedule
		this.getServer ().getScheduler ().scheduleSyncRepeatingTask (this, this, (this.configuration.backupInitial ? 200 : this.configuration.backupPeriod), this.configuration.backupPeriod);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run () {
		Thread backupThread = new BackupThread (this, this.storageTask);

		// announce backup
		this.getLogger ().info ("Backing up server instance.");

		// start
		backupThread.start ();
	}
}
