TODO

- doku (Latex) setup
- evaluation uml tool



-------------
Project setup
-------------

- Install eclipse
- Install subversive
- Install maven
     http://download.eclipse.org/technology/m2e/releases
- Get project data from SVN Repository
- Import code style formatter
   Windows / Preferences / Java / Codestyle / Formmater / Import
- Eclipse Save-Action code formatting einricht
- Latex
-- TexLife Distribution
-- TexLibs Eclipse Plugin

- ArgoUML (kann svg export)

- WindowBuilder https://developers.google.com/java-dev-tools/wbpro/

   
-------------
File Format
TODO: move this to our documentation
-------------


Variante I
----------

- Aus Pfad "/user/home/file.txt" String wird ein Hash erstellt
- Hash wird in b-Baum eingefügt mit referenz auf DataNode

- Aus Pfad "/user/home/subfolder" String wird ein Hash erstellt
- Hash wird in b-Baum eingefügt mit referenz auf (Folder)DataNode
- (Folder)DataNode enthält Liste mit Subfolders und enthaltenen Files

Nachteil: Liste ist langsam, sollte aber nicht so tragisch sein, da wir pro Listeneintrag "nur" 8 bytes schreiben
-> Insert in Folder mit 10'000 Files/Folder verschiebt 80kbytes

Vergleich: ext4 kann 64'000 files pro Folder

--------------------------

VFS File Header
- Info | 50 byte ASCII String
  Contains something like Badger VFS 2013 V1.0
- Version | 10 byte ASCII String
  Contains something like "1.0"
- Compression used | 20 byte ASCII String
  null or indicates compression used for this file
- Encryption used | 20 byte ASCII String
  null or indicates encryption used for this file
- IndexSectionOffset | double (8 byte)
  File offset where our Index Section starts
- DataSectionOffset | double (8 byte)
  File offset where our Data Section starts
- SaltString | 8 bytes 
  Salt used to hash Username & password randomly string generated while creating this file
- password | xx bytes 
  CryptoHash (SHA-whatever) of Password+SaltString
  
Index Section


Data Section
- Data section is split into blocks a X bytes
- Block layout
  - BlockHeader | 1 byte
    LSB indicates the Header-Bit
  - NextDataBlock | 8 bytes long
    Adress of the next Data Block (linked list)
    \x0000 0000 if this is the last Data block of a certain fFile or folder
  - HeaderLengthIndicator | 4 bytes
    indicates the lenght of the DataBlock Header in bytes
    This field only exists if BlockHeader Bit is set to 1
  - Header | variable number of bytes
    Header Informationen creation date, modification date, file name
    This field only exists if BlockHeader Bit is set to 1
  - DataLenghtIndicator | 4 bytes
    Indicates the number of data is saved on this DataBlock
