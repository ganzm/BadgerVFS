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


Variante II
-----------

- Wir unterscheiden zwischen Index-Node und Data-Node

- Jeder Node im b-Baum ist ein File/Folder
- Key ist File/Folder Name
- Folder hat Referenz auf Parent



Notizen

/aux/
/abc.txt
/blub/
/blub/abc.txt
/blub/subblub/
/blub/subblub/text.txt
/blub/xyz.raw
/cd/
/cd/data.iso

----------------------



Header
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
- Username | xx bytes 
  CryptoHash (SHA-whatever) of Username+SaltString
- Username | xx bytes 
  CryptoHash (SHA-whatever) of Password+SaltString
  
Index Section


Data Section
  
  