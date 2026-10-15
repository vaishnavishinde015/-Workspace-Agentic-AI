$ErrorActionPreference = "Stop"

$javaHome = $env:JAVA_HOME
if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\java.exe'))) {
    $jdkRoots = @(
        (Join-Path $env:ProgramFiles 'Microsoft'),
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $HOME '.jdk')
    )
    $javaHome = $jdkRoots |
        Where-Object { Test-Path $_ } |
        ForEach-Object { Get-ChildItem -LiteralPath $_ -Directory -Filter 'jdk-25*' -ErrorAction SilentlyContinue } |
        Sort-Object Name -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\java.exe'))) {
    throw "Could not find JDK 25. Install Java 25 or set JAVA_HOME to a JDK 25 installation."
}

$javaExecutable = Join-Path $javaHome 'bin\java.exe'
$versionOutput = & $env:ComSpec /d /c "`"$javaExecutable`" -version 2>&1"
if (($versionOutput -join "`n") -notmatch 'version "25(?:\.|")') {
    throw "This project requires Java 25. Detected runtime at $javaHome."
}

$env:JAVA_HOME = $javaHome
$env:Path = "$(Join-Path $javaHome 'bin');$env:Path"
& mvn spring-boot:run
exit $LASTEXITCODE
