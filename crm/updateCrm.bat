set SRC_DIR="C:\Projets\Silverpeas_Git\dev\Silverpeas-Components\crm\crm-war\src\main\webapp"
set DEST_DIR="C:\Projets\Silverpeas_Git\jboss403\server\default\deploy\silverpeas.ear\war-ic.war"

copy /Y "%SRC_DIR%\crm\img\*.*" "%DEST_DIR%\crm\img"
copy /Y "%SRC_DIR%\crm\jsp\*.*" "%DEST_DIR%\crm\jsp"
copy /Y "%SRC_DIR%\crm\jsp\styleSheets\*.*" "%DEST_DIR%\crm\jsp\styleSheets"
copy /Y "%SRC_DIR%\util\icons\component\*.*" "%DEST_DIR%\util\icons\component"