# TopoIndex

Android app for managing a personal collection of USGS maps.

Status: **alpha!** use with care...

## How to Use

### Build a map collection
1) Acquire GeoPDF files. USGS maps can be downloaded using [TopoView](https://ngmdb.usgs.gov/topoview/viewer/) (or found [other ways](https://www.usgs.gov/faqs/how-do-i-find-and-download-us-topo-maps-and-historical-topographic-maps)).
2) Copy maps onto your sdcard. The default collection location is `/sdcard/maps`
3) Repeat until your collection becomes unwieldy (and finding the right map becomes difficult).

  
### Setup the app
1) `Settings -> Index -> Update Index`  
2) `Settings -> General -> Scan Files`
3) `Location -> Auto -> On`

The index is created from a csv file published by the USGS. You can either `Update from assets` to use the file packaged with the app, or `Update from file` to select a previously downloaded [csv](http://geonames.usgs.gov/pls/topomaps/).
The update process takes several minutes. You can close the app while waiting - a notification will be shown when the update is complete.

The app scans your collection (`/sdcard/maps`) and cross references files with the index (using the map's `scanID` or `GDA Item ID`). This takes a few moments depending on the size of your collection. You should trigger a scan after adding or removing files. 

The app can use the `PASSIVE_PROVIDER` to determine your current location. To use `Auto Location`, turn on `location services` and use a separate app (e.g. SatStat) to get location updates.   

### Use the app to..

* Search for maps (by `location`, `map name`, `state`, or `map scale`).
* Select a map and display a grid of adjacent maps (Quadrangles).
* Open a map from the local collection (requires a pdf viewer).
* Download a map from the index (requires a browser).

 TopoIndex can help manage a large collection of maps. It assists in finding and opening maps, or discovering maps for download.

**TopoIndex is not a viewer.** A pdf viewer is still required to open and view GeoPDF files.<br />
**TopoIndex is not a downloader.** A browser is still required to open urls and download files.<br/>
