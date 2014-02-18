package com.evilco.bukkit.backup.task;

import java.io.File;

/**
 * @auhtor Johannes Donath <johannesd@evil-co.com>
 * @copyright Copyright (C) 2014 Evil-Co <http://www.evil-co.org>
 */
public interface IStorageTask {

	/**
	 * Stores the backup archive.
	 * @param sourceFile
	 */
	public void store (File sourceFile);
}
