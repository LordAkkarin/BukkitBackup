package com.evilco.bukkit.backup.configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.File;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
@XmlRootElement (name = "Configuration", namespace = BackupConfiguration.NAMESPACE)
@XmlType (propOrder = { "storageMethod", "amazonAccessKeyID", "amazonAccessSecretKey", "amazonEndpoint", "amazonBucketName", "backupPeriod", "backupInitial" })
public class BackupConfiguration implements AWSCredentialsProvider {

	/**
	 * Defines the configuration file name.
	 */
	public static final String FILE = "configuration.xml";

	/**
	 * Defines the configuration namespace.
	 */
	public static final String NAMESPACE = "http://www.evil-co.org/2014/configuration";

	/**
	 * Defines the AWS access key.
	 */
	@XmlElement (name = "AmazonAccessKeyID", namespace = NAMESPACE)
	public String amazonAccessKeyID = "12345678";

	/**
	 * Defines the AWS access key.
	 */
	@XmlElement (name = "AmazonAccessSecretKey", namespace = NAMESPACE)
	public String amazonAccessSecretKey = "secret";

	/**
	 * Defines the Glacier Bucket.
	 */
	@XmlElement (name = "AmazonBucket", namespace = NAMESPACE)
	public String amazonBucketName = "example-bucket";

	/**
	 * Defines the glacier endpoint (region).
	 */
	@XmlElement (name = "AmazonEndpoint", namespace = NAMESPACE)
	public String amazonEndpoint = "us-east-1";

	/**
	 * Defines whether the plugin should backup the initial state.
	 */
	@XmlElement (name = "BackupInitialState", namespace = NAMESPACE)
	public boolean backupInitial = true;

	/**
	 * Defines how long it takes between the backups.
	 */
	@XmlElement (name = "Period", namespace = NAMESPACE)
	public long backupPeriod = 43200L;

	/**
	 * Defines the backup storage method.
	 */
	@XmlElement (name = "StorageMethod", namespace = NAMESPACE)
	public BackupMethod storageMethod = BackupMethod.NONE;

	/**
	 * Internal constructor.
	 */
	protected BackupConfiguration () { }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AWSCredentials getCredentials () {
		final String accessKeyID = this.amazonAccessKeyID;
		final String secretKey = this.amazonAccessSecretKey;

		// construct credentials
		return new AWSCredentials () {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getAWSAccessKeyId () {
				return accessKeyID;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getAWSSecretKey () {
				return secretKey;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh () { }

	/**
	 * Returns the correct configuration file reference.
	 * @param parentDirectory
	 * @return
	 */
	public static File getConfigurationFile (File parentDirectory) {
		// create directories (if needed)
		if (!parentDirectory.exists ()) parentDirectory.mkdirs ();

		// create file instance
		return (new File (parentDirectory, FILE));
	}

	/**
	 * Loads the configuration file.
	 * @param file
	 * @return
	 * @throws ConfigurationLoadException
	 */
	public static BackupConfiguration load (File file) throws ConfigurationLoadException {
		try {
			// create JAXB context
			JAXBContext context = JAXBContext.newInstance (BackupConfiguration.class);

			// create un-marshaller
			Unmarshaller unmarshaller = context.createUnmarshaller ();

			// load from file
			return ((BackupConfiguration) unmarshaller.unmarshal (file));
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
	public static BackupConfiguration newInstance (File parentDirectory) throws ConfigurationException {
		// create configuration file reference
		File configurationFile = getConfigurationFile (parentDirectory);

		// try to load file
		try {
			return load (configurationFile);
		} catch (ConfigurationLoadException ex) {
			// create a new instance
			BackupConfiguration instance = new BackupConfiguration ();

			// save data
			instance.save (configurationFile);

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
			JAXBContext context = JAXBContext.newInstance (BackupConfiguration.class);

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
}