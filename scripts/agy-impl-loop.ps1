# Usage: agy-impl-loop.ps1 <promptFile>
# agy implements; we run mvn; failures go back to agy (--continue) until the suite is green. No retry limit;
# only stops early if the same failure signature repeats 5 times in a row (no progress).
param([string]$PromptFile)
$ErrorActionPreference = "Continue"
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME","User")
$repo = Split-Path $PSScriptRoot -Parent
$agy = "$env:LOCALAPPDATA\agy\bin\agy.exe"
$extra = @("--mode","accept-edits","--model","gemini-3.1-pro-high","--print-timeout","25m")
Set-Location $repo
# First call: the implementation prompt
$out = & $agy -p (Get-Content -Raw $PromptFile) @extra 2>&1 | Out-String
Write-Output ("agy round 0 done: " + ($out.Trim().Split("`n")[0]))
$last = ""; $same = 0; $round = 0
while ($true) {
    # Run the whole test suite and keep only the useful lines
    Set-Location "$repo\service"
    $log = & mvn test 2>&1 | Out-String
    Set-Location $repo
    if ($LASTEXITCODE -eq 0 -or $log -match "BUILD SUCCESS") { Write-Output "GREEN after $round fix round(s)"; ($log -split "`n" | Select-String "Tests run:.*Fail" | Select-Object -Last 4) -join "`n"; break }
    $fail = ($log -split "`n" | Select-String -Pattern "ERROR|FAIL|expected|Caused by" | Select-Object -First 40) -join "`n"
    $fail = $fail.Replace([string][char]34, "'")
    $sig = $fail.Substring(0, [Math]::Min(600, $fail.Length))
    if ($sig -eq $last) { $same++ } else { $same = 0; $last = $sig }
    if ($same -ge 5) { Write-Output "STUCK: same failure 5 rounds in a row"; Write-Output $fail; break }
    $round++
    # Send the failures back to agy; tests are fixed unless they are truly wrong
    $fb = "The test run failed. Fix the PRODUCTION code so the committed tests pass. Do not edit tests unless a test is provably wrong (then say so explicitly). Keep following AGENTS.md. NEVER call the run-command tool, use only file tools, do not commit. Failures:`n$fail`nReply with one line."
    $o = & $agy -p $fb --continue @extra 2>&1 | Out-String
    Write-Output ("round ${round}: " + ($o.Trim().Split("`n")[0]))
}
