# Usage: agy-run.ps1 <promptFile> [extra agy args...]  -- runs agy headless from the repo with a fresh PATH
param([string]$PromptFile, [Parameter(ValueFromRemainingArguments=$true)]$Rest)
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME","User")
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo
$p = Get-Content -Raw $PromptFile
& "$env:LOCALAPPDATA\agy\bin\agy.exe" -p $p --mode accept-edits --model gemini-3.1-pro-high --print-timeout 25m @Rest
