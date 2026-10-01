# Usage: agy-run.ps1 <promptFile> [extra agy args...]  -- runs agy headless from the repo with a fresh PATH
param([string]$PromptFile, [Parameter(ValueFromRemainingArguments=$true)]$Rest)

# Refresh PATH and JAVA_HOME from the machine and user environment
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = [System.Environment]::GetEnvironmentVariable("JAVA_HOME","User")

# Set the working directory to the repository root
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

# Create the logs folder if it does not exist
if (-not (Test-Path "$repo\logs")) { New-Item -ItemType Directory -Path "$repo\logs" | Out-Null }
if (-not (Test-Path "$repo\logs\agy")) { New-Item -ItemType Directory -Path "$repo\logs\agy" | Out-Null }

# Build the run name from the current time and the prompt file name (without extension)
$runName = (Get-Date).ToString("yyyyMMdd-HHmmss") + "-" + [System.IO.Path]::GetFileNameWithoutExtension($PromptFile)

# Write a start line to the progress log
$startTime = Get-Date
"[$($startTime.ToString('HH:mm:ss'))] agy-run.ps1 started with prompt: $PromptFile" | Out-File -FilePath "$repo\logs\agy-progress.log" -Append -Encoding ASCII

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
        "[$now] ${elapsed}m - $stepInfo - $fileInfo" | Out-File -FilePath "$repoPath\logs\agy-progress.log" -Append -Encoding ASCII
    }
}

# Start the background job and keep its reference
$job = Start-Job -ScriptBlock $jobScript -ArgumentList $repo, $startTime

try {
    # Read the prompt from the file
    $p = Get-Content -Raw $PromptFile
    
    $extraArgs = @("--log-file", "logs/agy/$runName.log", "--output-format", "stream-json")
    if ($PromptFile -match "tests" -or $PromptFile -match "implement") {
        $extraArgs += "--json-schema"
        $extraArgs += "scripts/prompts/report.schema.json"
    }
    
    # Run the executor headless with the specified arguments
    & "$env:LOCALAPPDATA\agy\bin\agy.exe" -p $p --mode accept-edits --model gemini-3.1-pro-high --print-timeout 25m @extraArgs @Rest | Out-File -FilePath "$repo\logs\agy\$runName.jsonl" -Encoding UTF8

    $jsonlPath = "$repo\logs\agy\$runName.jsonl"
    if (Test-Path $jsonlPath) {
        # Track whether a result event was found in the stream
        $foundResult = $false
        $resultObj = $null
        $lines = Get-Content $jsonlPath
        foreach ($line in $lines) {
            try {
                $evt = $line | ConvertFrom-Json
                # Extract the result payload when the event is result
                if ($evt.event -eq "result") {
                    $foundResult = $true
                    $resultObj = $evt.result
                }
            } catch { }
        }
        # Print the final response and check for failure status
        if ($foundResult) {
            Write-Output $resultObj.response
            if ($resultObj.status -ne "SUCCESS") {
                Write-Output "agy-run: status $($resultObj.status)"
            }
        } else {
            Write-Output "agy-run: no final result"
        }
    } else {
        Write-Output "agy-run: no final result"
    }
} finally {
    # Stop and remove the background job
    Stop-Job -Job $job
    Remove-Job -Job $job
    
    # Record the exit code and duration in the progress log
    $endTime = Get-Date
    $duration = [math]::Floor($endTime.Subtract($startTime).TotalSeconds)
    "[$($endTime.ToString('HH:mm:ss'))] agy-run.ps1 finished with exit code $LASTEXITCODE in ${duration}s" | Out-File -FilePath "$repo\logs\agy-progress.log" -Append -Encoding ASCII
}
