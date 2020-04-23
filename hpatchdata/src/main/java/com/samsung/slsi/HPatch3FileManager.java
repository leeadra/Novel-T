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
 * @file		HPatch3FileManager.java
 * @brief		SPatch3 FileManager
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/24
 *
 * <b>revision history :</b>
 * - 2016/12/24 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;

public class HPatch3FileManager implements HPatchFileManager {

    private HPatchHostOS hostOS;
    private int totalSamples;

    public HPatch3FileManager(HPatchHostOS hostOS) {
        this.hostOS = hostOS;
    }

    ////////////////////
    // Export / Import

    @Override
    public HPatchDataManager importSPatch(String path, String fileName) throws IOException {
        HPatchDataManager dataManager = null;
        /*String importType = "SP";

        int n = fileName.lastIndexOf(".");
        if (n >= 0) {
            String extension = fileName.substring(n + 1);
            if (extension.equalsIgnoreCase("dat")
                    || extension.equalsIgnoreCase("sig")
                    || extension.equalsIgnoreCase("hea")) {
                importType = "MITBIH";
            }
        }

        // Now only MIT BIH format support
        if (importType.equalsIgnoreCase("MITBIH")) {
            HPatchImportMITBIH mitbih = new HPatchImportMITBIH(hostOS);
            dataManager = mitbih.importMITBIH(path, fileName);
            totalSamples = mitbih.getNumberOfSamples();
        } else {
            //Assume Import Type is "SP"
            HPatchData sPatchData = HPatchData.restore(hostOS, path, fileName);
            if (sPatchData != null) {
                HPatch sPatch = new FileHPatch(sPatchData.getId(),
                        sPatchData.getUnitMilliVoltage(),
                        sPatchData.getEcgTransferSamplesPerSecond());

                dataManager = new HPatch3DataManager(hostOS, sPatch, path, sPatchData);

                totalSamples = sPatchData.getECGSignal().getTotalCount();
            }
        }*/
        return dataManager;
    }

    @Override
    public int getTotalSamples() {
        return totalSamples;
    }

    @Override
    public void exportSPatch(String exportType, String path, String fileName, HPatchDataManager dataManager) throws IOException {
        //ToDo: Now only MIT BIH format support

        /*if (exportType.equalsIgnoreCase("MITBIH")) {
            HPatchECGDataManager ecgDataManager = dataManager.getECGDataManager();

            HPatchExportMITBIH mitbih = new HPatchExportMITBIH(hostOS, ecgDataManager);
            mitbih.createHEA(path, fileName, dataManager.getRecordName(), dataManager.getSamplesPerSecond(), ecgDataManager.getUnitMV());
            mitbih.createDat(path, fileName);
        }

        //ToDo: Test EDF Export
        {
            HPatchECGDataManager ecgDataManager = dataManager.getECGDataManager();

            HPatchExportEDF edf = new HPatchExportEDF(hostOS, ecgDataManager);
            edf.create(path, fileName + ".edf", dataManager.getRecordName(), dataManager.getSamplesPerSecond(), ecgDataManager.getUnitMV());
        }*/
    }
}
