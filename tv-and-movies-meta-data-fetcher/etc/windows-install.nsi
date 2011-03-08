;NSIS Modern User Interface version 1.70
;SimpleText Installer Script
;Written by Stephen Strenn

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "SimpleText"
  OutFile "SimpleTextInstaller.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\Stanwood\SimpleText"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\SimpleText" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
    !define MUI_HEADERIMAGE "C:\temp\SimpleText\installer\SimpleTextInstallerSplash.bmp"
    !define MUI_HEADERIMAGE_BITMAP_NOSTRETCH
    !define MUI_HEADERIMAGE_BITMAP "C:\temp\SimpleText\installer\SimpleTextInstallerSplash.bmp"
    !define MUI_ICON "C:\temp\SimpleText\installer\setup.ico"
    !define MUI_UNICON "C:\temp\SimpleText\installer\setup.ico"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "C:\temp\SimpleText\installer\License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "SimpleText (required)" SecDummy

  SectionIn RO

  ;Files to be installed
  SetOutPath "$INSTDIR"
  
   File "C:\temp\SimpleText\SimpleText_1.0.jar"
   File "C:\temp\SimpleText\images\SimpleText.ico"
    File "C:\temp\SimpleText\swt-win32-3138.dll"

    SetOutPath "$INSTDIR\lib"

    File "C:\temp\SimpleText\lib\org.eclipse.swt.win32.win32.x86_3.1.0.jar"

  SetOutPath "$INSTDIR"

    ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\SimpleText "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SimpleText" "DisplayName" "SimpleText"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SimpleText" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SimpleText" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SimpleText" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"
  CreateDirectory "$SMPROGRAMS\SimpleText"
  CreateShortCut "$SMPROGRAMS\SimpleText\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe"
  CreateShortCut "$SMPROGRAMS\SimpleText\SimpleText.lnk" "$INSTDIR\SimpleText_1.0.jar" "" "$INSTDIR\SimpleText.ico"
SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SimpleText"
  DeleteRegKey HKLM SOFTWARE\SimpleText
  DeleteRegKey /ifempty HKCU "Software\SimpleText"

    ; Remove shortcuts
  RMDir /r "$SMPROGRAMS\SimpleText"

  ; Remove directories used
  RMDir /r "$INSTDIR"

SectionEnd
