package com.evilco.bukkit.backup.configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
@XmlRootElement (name = "Storage", namespace = BackupStorage.NAMESPACE)
public class BackupStorage {

	/**
	 * Defines the database file.
	 */
	public static final String FILE = "storage.xml";

	/**
	 * Defines the XML namespace.
	 */
	public static final String NAMESPACE = "http://www.evil-co.com/2014/bukkit/backup/storage";

	/**
	 * Stores all created archives.
	 */
	@XmlElement (name = "archive", namespace = NAMESPACE)
	public List<BackupArchive> archiveList = new ArrayList<BackupArchive> ();

	/**
	 * Internal Constructor.
	 */
	protected BackupStorage () { }

	/**
	 * Returns the correct configuration file reference.
	 * @param parentDirectory
	 * @return
	 */
	public static File getDatabaseFile (File parentDirectory) {
		// create directories (if needed)
		if (!parentDirectory.exists ()) parentDirectory.mkdirs ();

		// create file instance
		return (new File (parentDirectory, FILE));
	}

	/**
	 * Loads the storage file.
	 * @param file
	 * @return
	 * @throws ConfigurationLoadException
	 */
	public static BackupStorage load (File file) throws ConfigurationLoadException {
		try {
			// create JAXB context
			JAXBContext context = JAXBContext.newInstance (BackupStorage.class);

			// create un-marshaller
			Unmarshaller unmarshaller = context.createUnmarshaller ();

			// load from file
			return ((BackupStorage) unmarshaller.unmarshal (file));
		} catch (JAXBException ex) {
			throw new ConfigurationLoadException (ex);
		}
	}

	/**
	 * Constructs a new instance.
	 * @param parentDirectory
	 * @return
	 * @throws ConfigurationException
	 */
	public static BackupStorage newInstance (File parentDirectory) throws ConfigurationException {
		// create configuration file reference
		File databaseFile = getDatabaseFile (parentDirectory);

		// try to load file
		try {
			return load (databaseFile);
		} catch (ConfigurationLoadException ex) {
			// create a new instance
			BackupStorage instance = new BackupStorage ();

			// save data
			instance.save (databaseFile);

			// return instance
			return instance;
		}
	}

	/**
	 * Saves the configuration back into the file.
	 * @param file
	 * @throws ConfigurationSaveException
	 */
	public void save (File file) throws ConfigurationSaveException {
		try {
			// create JAXB context
			JAXBContext context = JAXBContext.newInstance (BackupStorage.class);

			// create marshaller
			Marshaller marshaller = context.createMarshaller ();

			// set properties
			marshaller.setProperty (Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// marshal into file
			marshaller.marshal (this, file);
		} catch (JAXBException ex) {
			throw new ConfigurationSaveException (ex);
		}
	}

	/**
	 * Represents a single backup archive.
	 */
	public static class BackupArchive {

		/**
		 * Stores the backup archive ID.
		 */
		@XmlAttribute (name = "archiveID", namespace = NAMESPACE)
		protected String archiveID;

		/**
		 * Stores the backup timestamp.
		 */
		@XmlAttribute (name = "timestamp", namespace = NAMESPACE)
		protected long timestamp;

		/**
		 * Protected Constructor.
		 */
		protected BackupArchive () { }

		/**
		 * Creates a new BackupArchive instance.
		 * @param archiveID
		 */
		public BackupArchive (String archiveID) {
			this (archiveID, (System.currentTimeMillis () / 1000L));
		}

		/**
		 * Constructs a new BackupArchive instance.
		 * @param archiveID
		 * @param timestamp
		 */
		public BackupArchive (String archiveID, long timestamp) {
			this.archiveID = archiveID;
			this.timestamp = timestamp;
		}

		/**
		 * Returns the archive ID.
		 * @return
		 */
		public String getArchiveID () {
			return this.archiveID;
		}

		/**
		 * Returns the creation timestamp.
		 * @return
		 */
		public long getTimestamp () {
			return this.timestamp;
		}
	}
}