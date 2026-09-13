<#
.SYNOPSIS
Maven Wrapper PowerShell Script for BankFlow
Downloads Maven if not already present in ~/.m2/wrapper and runs it.
#>
[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Arguments
)

$ErrorActionPreference = 'Stop'

# Locate JAVA_HOME or java in PATH
$javaExe = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
} else {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaExe = $javaCommand.Source
    } else {
        # Check standard installation directories
        $candidatePaths = @(
            "$env:USERPROFILE\.jdks\temurin-17\*\bin\java.exe",
            "$env:USERPROFILE\.jdks\*\bin\java.exe",
            "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe",
            "C:\Program Files\Java\jdk-17*\bin\java.exe",
            "C:\Program Files\Amazon Corretto\jdk17*\bin\java.exe",
            "C:\Program Files\Microsoft\jdk-17*\bin\java.exe"
        )
        foreach ($pattern in $candidatePaths) {
            $found = Resolve-Path $pattern -ErrorAction SilentlyContinue
            if ($found) {
                $javaExe = $found[0].Path
                $env:JAVA_HOME = Split-Path (Split-Path $javaExe -Parent) -Parent
                break
            }
        }
    }
}

if (-not $javaExe) {
    Write-Error "Java 17 JDK was not found. Please ensure Java 17 is installed."
    exit 1
}

$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $projectRoot) { $projectRoot = Get-Location }

# Check wrapper properties
$wrapperProps = Join-Path $projectRoot ".mvn\wrapper\maven-wrapper.properties"
$mavenVersion = "3.9.6"
if (Test-Path $wrapperProps) {
    $content = Get-Content $wrapperProps -Raw
    if ($content -match "apache-maven-([0-9\.]+)-bin\.zip") {
        $mavenVersion = $matches[1]
    }
}

$userHome = [Environment]::GetFolderPath("UserProfile")
$mavenHome = Join-Path $userHome ".m2\wrapper\dists\apache-maven-$mavenVersion\apache-maven-$mavenVersion"
$mavenBin = Join-Path $mavenHome "bin\mvn.cmd"

if (-not (Test-Path $mavenBin)) {
    Write-Host "Downloading Maven $mavenVersion..." -ForegroundColor Cyan
    $zipUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    $tempZip = Join-Path $env:TEMP "apache-maven-$mavenVersion-bin.zip"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $zipUrl -OutFile $tempZip -UseBasicParsing

    $targetDir = Split-Path $mavenHome -Parent
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    Write-Host "Extracting Maven $mavenVersion..." -ForegroundColor Cyan
    Expand-Archive -Path $tempZip -DestinationPath $targetDir -Force
    Remove-Item $tempZip -Force
}

Write-Host "Running Maven with Java: $javaExe" -ForegroundColor Green
$env:JAVA_HOME = Split-Path (Split-Path $javaExe -Parent) -Parent
& "$mavenBin" @Arguments
exit $LASTEXITCODE
