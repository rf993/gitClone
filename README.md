# VERSCommon

This package is part of the Victorian Electronic Records Strategy (VERS)
software release. For more information about VERS see
[here](https://prov.vic.gov.au/recordkeeping-government/vers).

The software release is Java code to construct, analyse, and process VERS
Version 2 (VERS V2) or Version 3 (VERS V3) VERS Encapsulated Objects (VEOs).

This package contains common components for VERS V2 and VERS V3. It needs to
be downloaded and placed in the same directory as the neoVEO  (VERS V3) package.
Structurally, the package is an Apache Netbeans project.

The two main components
- VERSSupportFiles. This is a directory of support files for VERS V2 and VERS V3.
This includes logging configuration, the VERS V2 DTD, the VERS V3 Schema definitions, the standard VERS V3 VEOReadme.txt file, and the list of valid Long Term Sustainable Formats accepted by VERS.
- src/VERSCommon. These are a set of Java utility classes.