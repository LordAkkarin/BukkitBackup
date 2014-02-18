package com.evilco.bukkit.backup.task;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.evilco.bukkit.backup.BackupPlugin;
import com.evilco.bukkit.backup.configuration.BackupStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class AmazonGlacierUploadTask implements IStorageTask {

	/**
	 * Defines the amazon endpoint to use.
	 */
	public static final String ENDPOINT = "https://glacier.%s.amazonaws.com/";

	/**
	 * Stores the glacier client instance.
	 */
	protected AmazonGlacierClient client = null;

	/**
	 * Stores the parent plugin instance.
	 */
	protected BackupPlugin plugin = null;

	/**
	 * Constructs a new AmazonGlacierUploadTask.
	 * @param plugin
	 */
	public AmazonGlacierUploadTask (BackupPlugin plugin) {
		this.plugin = plugin;

		// construct client
		this.client = new AmazonGlacierClient (plugin.getConfiguration ());
		this.client.setEndpoint (String.format (ENDPOINT, this.plugin.getConfiguration ().amazonEndpoint));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store (File sourceFile) {
		// notify
		this.plugin.getLogger ().info ("Uploading backup file (" + sourceFile.getName () + ") to Amazon Glacier ...");

		// create transfer manager
		ArchiveTransferManager archiveTransferManager = new ArchiveTransferManager (this.client, this.plugin.getConfiguration ());

		// upload
		try {
			UploadResult result = archiveTransferManager.upload (this.plugin.getConfiguration ().amazonBucketName, "Bukkit Update " + sourceFile.getName (), sourceFile);

			// store to database
			this.plugin.getStorage ().archiveList.add ((new BackupStorage.BackupArchive (result.getArchiveId ())));

			// log
			this.plugin.getLogger ().info ("Finished upload to Amazon Glacier (archive " + result.getArchiveId () + ").");
		} catch (FileNotFoundException ex) {
			this.plugin.getLogger ().log (Level.SEVERE, "Could not upload backup archive to Amazon Glacier.", ex);
		}
	}
}
