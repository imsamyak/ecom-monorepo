# Usage: agy-impl-loop.ps1 <promptFile>
# agy implements; we run mvn; failures go back to agy (--continue) until the suite is green. No retry limit;
# only stops early if the same failure signature repeats 5 times in a row (no progress).
param([string]$PromptFile)

# Configure error action preference
$ErrorActionPreference = "Continue"

# Refresh PATH and JAVA_HOME from the machine and user environment
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME","User")

# Set the working directory to the repository root
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

# Create the logs folder if it does not exist
if (-not (Test-Path "$repo\logs")) { New-Item -ItemType Directory -Path "$repo\logs" | Out-Null }

# Write a start line to the progress log
$startTime = Get-Date
"[$($startTime.ToString('HH:mm:ss'))] agy-impl-loop.ps1 started with prompt: $PromptFile" | Out-File -FilePath "$repo\logs\agy-progress.log" -Append -Encoding ASCII

# Initialize the round file with 0
"0" | Out-File -FilePath "$repo\logs\agy-round.txt" -Encoding ASCII

# Define the background job to poll and write progress
$jobScript = {
    param($repoPath, $start)
    # Run indefinitely until stopped by the main script
    while ($true) {
        # Wait 30 seconds before checking again
        Start-Sleep -Seconds 30
        
        # Calculate elapsed time in minutes
        $elapsed = [math]::Floor((Get-Date).Subtract($start).TotalMinutes)
        $now = (Get-Date).ToString('HH:mm:ss')
        $cliLog = "$env:USERPROFILE\.gemini\antigravity-cli\cli.log"
        $stepInfo = "no step info"
        $fileInfo = "no file written"
        $roundInfo = "round ?"
        
        # Read the current round from the round file
        if (Test-Path "$repoPath\logs\agy-round.txt") {
            $roundInfo = "round " + (Get-Content "$repoPath\logs\agy-round.txt").Trim()
        }
        
        # Read the last 300 lines of the log file if it exists
        if (Test-Path $cliLog) {
            $lines = Get-Content $cliLog -Tail 300 -ErrorAction SilentlyContinue
            if ($null -ne $lines) {
                # Find the last line mentioning the step number
                $lastStep = $lines | Where-Object { $_ -match "at step \d+" } | Select-Object -Last 1
                if ($lastStep) { $stepInfo = $lastStep.Trim() }
                
                # Find the last line mentioning a file write
                $lastWrite = $lines | Where-Object { $_ -match "file write" } | Select-Object -Last 1
                if ($lastWrite) { $fileInfo = $lastWrite.Trim() }
            }
        }
        
        # Append the progress line to the progress log
        "[$now] ${elapsed}m - $roundInfo - $stepInfo - $fileInfo" | Out-File -FilePath "$repoPath\logs\agy-progress.log" -Append -Encoding ASCII
    }
}

# Start the background job and keep its reference
$job = Start-Job -ScriptBlock $jobScript -ArgumentList $repo, $startTime

try {
    # First call: the implementation prompt
    $out = & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-run.ps1 $PromptFile 2>&1 | Out-String
    Write-Output ("agy round 0 done: " + ($out.Trim().Split("`n")[0]))
    $last = ""; $same = 0; $round = 0
    
    # Loop to run tests and retry until green
    while ($true) {
        # Run the whole test suite and keep only the useful lines
        Set-Location "$repo\service"
        $log = & mvn test 2>&1 | Out-String
        Set-Location $repo
        
        # Check if the tests passed and break if they did
        if ($LASTEXITCODE -eq 0 -or $log -match "BUILD SUCCESS") { 
            Write-Output "GREEN after $round fix round(s)"
            ($log -split "`n" | Select-String "Tests run:.*Fail" | Select-Object -Last 4) -join "`n"
            break 
        }
        
        # Extract the failure signature to detect stuck loops
        $fail = ($log -split "`n" | Select-String -Pattern "ERROR|FAIL|expected|Caused by" | Select-Object -First 40) -join "`n"
        $fail = $fail.Replace([string][char]34, "'")
        $sig = $fail.Substring(0, [Math]::Min(600, $fail.Length))
        
        # Check for repeated failures and break if stuck
        if ($sig -eq $last) { $same++ } else { $same = 0; $last = $sig }
        if ($same -ge 5) { 
            Write-Output "STUCK: same failure 5 rounds in a row"
            Write-Output $fail
            break 
        }
        
        # Increment the round and update the round file
        $round++
        $round.ToString() | Out-File -FilePath "$repo\logs\agy-round.txt" -Encoding ASCII
        
        # Send the failures back to agy; tests are fixed unless they are truly wrong
        $fb = "The test run failed. Fix the PRODUCTION code so the committed tests pass. Do not edit tests unless a test is provably wrong (then say so explicitly). Keep following AGENTS.md. NEVER call the run-command tool, use only file tools, do not commit. Failures:`n$fail`nYour final answer must be one JSON object that matches scripts/prompts/report.schema.json."
        # Use a prompt file name that contains implement so agy-run applies the JSON schema
        $fbFile = "logs/agy-prompt-implement-fix.txt"
        $fb | Out-File -FilePath "$repo\$fbFile" -Encoding ASCII
        $o = & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-run.ps1 "$repo\$fbFile" --continue 2>&1 | Out-String
        Write-Output ("round ${round}: " + ($o.Trim().Split("`n")[0]))
        # We also need to preserve the JSON output to $out so agy-chunk can parse it at the end
        $out = $o
    }
} finally {
    # Stop and remove the background job
    Stop-Job -Job $job
    Remove-Job -Job $job
    
    # Record the exit code and duration in the progress log
    $endTime = Get-Date
    $duration = [math]::Floor($endTime.Subtract($startTime).TotalSeconds)
    "[$($endTime.ToString('HH:mm:ss'))] agy-impl-loop.ps1 finished with exit code $LASTEXITCODE in ${duration}s" | Out-File -FilePath "$repo\logs\agy-progress.log" -Append -Encoding ASCII
}
