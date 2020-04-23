/*
 * Sensor Product Development Team, System LSI division.
 * Copyright (c) 2014-2017 Samsung Electronics, Inc.
 * All right reserved.
 *
 * This software is the confidential and proprietary information
 * of Samsung Electronics, Inc. (Confidential Information). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Samsung Electronics.
*/
/**
 *******************************************************************************
 * @file		HPatchHostOS.java
 * @brief		HPatch Host OS interface
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/11/21
 *
 * <b>revision history :</b>
 * - 2016/11/21 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;

public interface HPatchHostOS {
    int getRemainedStorageMBSize();

    String getTemporaryPath();

    void storeFile(String path, String fileName, byte[] data, boolean isAppend) throws IOException;
    void storeFile(String path, String fileName, byte[] data, int startIndex) throws IOException;
    byte[] restoreFile(String path, String fileName, int index, int size) throws IOException;
    boolean removeFile(String path, String fileName);

    int getFileSize(String path, String fileName) throws IOException;

    void setPreference(String key, String value);
    String getPreference(String key);

    PermissionResultHandler getPermissionResultHandler();
}
