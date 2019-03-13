/**
    Copyright (C) 2019 Forrest Guice
    This file is part of TopoIndex.

    TopoIndex is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TopoIndex is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TopoIndex.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.topoindex.database.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.database.filetypes.FileTypeHTMC;
import com.forrestguice.topoindex.database.filetypes.FileTypeUSTopo;
import com.forrestguice.topoindex.database.filetypes.TopoIndexFileType;

import java.io.File;
import java.util.Calendar;

public class DatabaseScanTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public static final String EXT_GEOPDF = "geo.pdf";
    public static final String EXT_GEOPDF_TM = "tm_geo.pdf";

    public static final String EXT_GEOJPG = "geo_jpg.zip";
    public static final String EXT_GEOJPG_TM = "tm_geo_jpg.zip";

    public static final String EXT_GEOTIFF = "geo_tiff.zip";
    public static final String EXT_GEOTIFF_TM = "tm_geo_tiff.zip";

    public static final String EXT_GEOKMZ = "geo_kmz.zip";
    public static final String EXT_GEOKMZ_TM = "tm_geo_kmz.zip";

    public static final String[] HTCM_EXTS = new String[] { EXT_GEOPDF, EXT_GEOTIFF, EXT_GEOJPG, EXT_GEOKMZ };
    public static final String[] USTOPO_EXTS = new String[] { EXT_GEOPDF_TM, EXT_GEOTIFF_TM, EXT_GEOJPG_TM, EXT_GEOKMZ_TM };

    public DatabaseScanTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        long bench_start = System.nanoTime();
        ScanResult result = new ScanResult();

        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
        {
            Context context = contextRef.get();
            if (context != null)
            {
                String collectionPath = AppSettings.getCollectionPath(context)[0];    // TODO: support for multiple paths
                File dataDirectory = Environment.getExternalStorageDirectory();
                File mapDirectory = new File(dataDirectory + File.separator + collectionPath);

                if (!mapDirectory.exists()) {
                    if (!mapDirectory.mkdirs())
                    {
                        Log.e(TAG, "scan: failed to create path... " + mapDirectory.getAbsolutePath());
                        return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
                    } else Log.i(TAG, "scan: created path... " + mapDirectory.getAbsolutePath());
                } // else Log.d("DEBUG", "dataDir: " + mapDirectory.getAbsolutePath());

                database.open();
                scanDirectory(mapDirectory, result);
                database.close();

            } else {
                Log.e(TAG, "scan: Context is null!");
                return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
            }
        } else {
            Log.e(TAG, "scan: External storage is unavailable!");
            return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
        }

        long bench_end = System.nanoTime();
        Log.d(TAG, "scan: took " + ((bench_end - bench_start) / 1000000.0) + " ms; " + result.count + " items.");
        return new DatabaseTaskResult(true, result.count, Calendar.getInstance().getTimeInMillis());
    }

    protected void scanDirectory(File dir, ScanResult result)
    {
        if (dir.exists())
        {
            Log.d(TAG, "scanDirectory: " + dir.getAbsolutePath());
            for (File file : dir.listFiles())
            {
                if (file.isDirectory())
                    scanDirectory(file, result);
                else scanFile(file, result);
            }
        }
    }

    protected void scanFile(File file, ScanResult result)
    {
        if (file.exists())
        {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(EXT_GEOPDF))
            {
                if (fileName.endsWith(EXT_GEOPDF_TM))
                {
                    TopoIndexFileType fileType = new FileTypeUSTopo();
                    fileType.scanFile(file, database, result);

                } else {
                    TopoIndexFileType fileType = new FileTypeHTMC();
                    fileType.scanFile(file, database, result);
                }
            } else {
                Log.d(TAG, "scanFile: ignoring " + file.getAbsolutePath());
            }
        }
    }

    public static class ScanResult
    {
        public int count = 0;
    }
}
