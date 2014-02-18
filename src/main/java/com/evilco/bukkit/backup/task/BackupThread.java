package com.evilco.bukkit.backup.task;

import com.evilco.bukkit.backup.BackupPlugin;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public class BackupThread extends Thread {

	/**
	 * Defines the folder to store backups in.
	 */
	public static final String BACKUP_DIRECTORY = "storage";

	/**
	 * Defines the backup file name template.
	 */
	public static final String BACKUP_FILE_TEMPLATE = "yyyy-mm-dd_hh-mm-ss";

	/**
	 * Stores the parent plugin instance.
	 */
	protected BackupPlugin plugin = null;

	/**
	 * Stores the storage task selected for backup archives.
	 */
	protected IStorageTask storageTask = null;

	/**
	 * Constructs a new backup task.
	 * @param task
	 */
	public BackupThread (BackupPlugin plugin, IStorageTask task) {
		this.plugin = plugin;
		this.storageTask = task;
	}

	/**
	 * Returns the current backup file.
	 * @return
	 */
	public File getBackupFile () {
		// get parent directory
		File parentDirectory = new File (this.plugin.getDataFolder (), BACKUP_DIRECTORY);

		// create directory (if needed)
		if (!parentDirectory.exists ()) parentDirectory.mkdirs ();

		// get date format
		SimpleDateFormat format = new SimpleDateFormat (BACKUP_FILE_TEMPLATE);

		// create correct reference
		return (new File (parentDirectory, format.format ((new Date ())) + ".tar.gz"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run () {
		// get file
		File backupFile = this.getBackupFile ();

		// create backup
		FileOutputStream outputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		GzipCompressorOutputStream compressorOutputStream = null;
		TarArchiveOutputStream tarArchiveOutputStream = null;

		try {
			// create streams
			outputStream = new FileOutputStream (backupFile);
			bufferedOutputStream = new BufferedOutputStream (outputStream);
			compressorOutputStream = new GzipCompressorOutputStream (bufferedOutputStream);
			tarArchiveOutputStream = new TarArchiveOutputStream (compressorOutputStream);

			// get server directory
			File serverDirectory = this.plugin.getDataFolder ().getParentFile ().getParentFile ();

			// iterate over plugins
			Iterator<File> pluginIterator = FileUtils.iterateFiles (this.plugin.getDataFolder ().getParentFile (), null, true);

			while (pluginIterator.hasNext ()) {
				// get current file
				File current = pluginIterator.next ();

				try {
					// omit own plugins directory
					if (current.equals (this.plugin.getDataFolder ()) || (current.getParentFile () != null && current.getParentFile ().equals (this.plugin.getDataFolder ())) || (current.getParentFile () != null && current.getParentFile ().getParentFile () != null && current.getParentFile ().getParentFile ().equals (this.plugin.getDataFolder ()))) {
						this.plugin.getLogger ().finest ("Omitting Backup plugin directory due to security reasons: " + current.getAbsolutePath ());
						continue;
					}

					// build path
					File currentParent = current;
					String prefix = "";

					do {
						// store new parent
						currentParent = currentParent.getParentFile ();

						// append to path
						prefix = currentParent.getName () + "/" + prefix;
					} while (!currentParent.equals (this.plugin.getDataFolder ().getParentFile ()));

					// create tar entry
					TarArchiveEntry archiveEntry = new TarArchiveEntry (current, prefix + current.getName ());

					// store archive
					tarArchiveOutputStream.putArchiveEntry (archiveEntry);

					// add data
					if (current.isFile ()) {
						// write file
						IOUtils.copy ((new FileInputStream (current)), tarArchiveOutputStream);
					}

					// close archive entry
					tarArchiveOutputStream.closeArchiveEntry ();
				} catch (IOException ex) {
					this.plugin.getLogger ().log (Level.WARNING, "Could not backup file " + current.getName () + ".", ex);
				} finally {
					try {
						tarArchiveOutputStream.closeArchiveEntry ();
					} catch (Exception ex) { }
				}
			}

			// backup world
			for (World world : this.plugin.getServer ().getWorlds ()) {
				// construct file for correct world
				File worldDirectory = world.getWorldFolder ();

				// iterate over all files
				Iterator<File> worldFiles = FileUtils.iterateFiles (worldDirectory, null, true);

				// log
				this.plugin.getLogger ().info ("Backing up world " + world.getName ());

				// iterate over world files
				while (worldFiles.hasNext ()) {
					// get next file
					File current = worldFiles.next ();

					// build path
					try {
						// build path
						File currentParent = current;
						String prefix = "";

						do {
							// store new parent
							currentParent = currentParent.getParentFile ();

							// append to path
							prefix = currentParent.getName () + "/" + prefix;
						} while (!currentParent.equals (worldDirectory));

						// create tar entry
						TarArchiveEntry archiveEntry = new TarArchiveEntry (current, prefix + current.getName ());

						// store archive
						tarArchiveOutputStream.putArchiveEntry (archiveEntry);

						// add data
						if (current.isFile ()) {
							// write file
							IOUtils.copy ((new FileInputStream (current)), tarArchiveOutputStream);
						}

						// close archive entry
						tarArchiveOutputStream.closeArchiveEntry ();
					} catch (IOException ex) {
						this.plugin.getLogger ().log (Level.WARNING, "Could not backup file " + current.getName () + ".", ex);
					} finally {
						try {
							tarArchiveOutputStream.closeArchiveEntry ();
						} catch (Exception ex) { }
					}
				}
			}
		} catch (FileNotFoundException ex) {
			this.plugin.getLogger ().log (Level.SEVERE, "Could not create backup in file " + backupFile.getAbsolutePath () + ".", ex);
		} catch (IOException ex) {
			this.plugin.getLogger ().log (Level.SEVERE, "Could not create backup in file " + backupFile.getAbsolutePath () + ".", ex);
		} finally {
			// close tar stream
			try {
				tarArchiveOutputStream.finish ();
				tarArchiveOutputStream.close ();
			} catch (Exception ex) { }

			// close gzip stream
			try {
				compressorOutputStream.finish ();
				compressorOutputStream.close ();
			} catch (Exception ex) { }

			// close buffered stream
			try {
				bufferedOutputStream.flush ();
				bufferedOutputStream.close ();
			} catch (Exception ex) { }

			// close file stream
			try {
				outputStream.flush ();
				outputStream.close ();
			} catch (Exception ex) { }
		}

		// store
		if (backupFile.exists () && this.storageTask != null) this.storageTask.store (backupFile);

		// finished
		this.plugin.getLogger ().info ("Finished backup (stored into file " + backupFile.getName () + ").");
	}
}
