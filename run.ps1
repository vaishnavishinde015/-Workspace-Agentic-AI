$ErrorActionPreference = "Stop"

function Load-EnvFile {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return
    }

    foreach ($line in Get-Content -Path $Path) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) {
            continue
        }

        if ($trimmed -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
            $name = $Matches[1]
            $value = $Matches[2].Trim()
            if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
        }
    }
}

Load-EnvFile (Join-Path $PSScriptRoot '.env')

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

$defaultPort = if ($env:PORT) { [int]$env:PORT } else { 8080 }
$port = $defaultPort
$originalPort = $port

while ($true) {
    $listener = $null
    try {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $port)
        $listener.Start()
        break
    }
    catch {
        $port++
        if ($port -gt 65535) {
            throw "Could not find an available port to start the app."
        }
    }
    finally {
        if ($null -ne $listener) {
            $listener.Stop()
        }
    }
}

if ($port -ne $originalPort) {
    Write-Host "Port $originalPort is already in use. Starting on port $port instead."
}

$env:PORT = [string]$port
$env:JAVA_HOME = $javaHome
$env:Path = "$(Join-Path $javaHome 'bin');$env:Path"
& mvn spring-boot:run
exit $LASTEXITCODE
