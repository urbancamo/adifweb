**Version 1.3.0, released 03-FEB-2024**
- migrated to Spring 3.2.2 platform from Spring 2.x

**Version 1.2.14, released 03-FEB-2024**
- add max power angle to antenna model
- make antenna model HF only
- vertically align form labels and input fields
- 
**Version 1.2.13, released 22-JAN-2024**
- fix bug introduced in last release
- refresh POTA park list with latest editions

**Version 1.2.12, released 21-JAN-2024**
- check whether my gridsquare if specified in the record is the same as a gridsquare calculated from QRZ lat/long. If they are the same, use the QRZ lat/long as it is likely more accurate.

**Version 1.2.11, released 16-JAN-2024**
- add option for more compact station display in Google Earth

**Version 1.2.10, released 13-JAN-2024**
- security credentials rotation

**Version 1.2.9, released 13-JAN-2024**
- fix propagation prediction for groundwave or LoS signals

**Version 1.2.8, released 13-JAN-2024**
- handle invalid mode of LSB/USB in input file - convert to SSB/LSB and SSB/USB mode/submode

**Version 1.2.7, released 31-DEC-2023**
- handle multiple POTA references in a comment
- show Time Off accurately in contact info panel

**Version 1.2.6, released 30-DEC-2023**
 - add Bunkers on the Air as a supported activity
 - improve resilience when reading ADIF files with empty fields
 - fix issue where no time is specified for a QSO
 - fix form submit button in Firefox
 - upgrade to Java 17