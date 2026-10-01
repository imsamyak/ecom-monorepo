param([string]$TaskId)

# Set error action preference
$ErrorActionPreference = "Continue"

# Go to repo root
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

# Read TASKS.md content
$tasksMd = Get-Content -Raw "TASKS.md"

# Find task section and spec
$lines = $tasksMd -split "`r?`n"
$inTask = $false
$taskSpecLines = @()
$taskLevel = 0

# Iterate over TASKS.md lines
foreach ($line in $lines) {
    # Check if the line is a heading
    if ($line -match "^(#+)\s+(.*?)$") {
        $level = $matches[1].Length
        $heading = $matches[2]
        # Break if we hit a heading of the same or higher level
        if ($inTask) {
            if ($level -le $taskLevel) {
                break
            }
        } else {
            # Start collecting if heading matches TaskId
            if ($heading -match $TaskId) {
                $inTask = $true
                $taskLevel = $level
            }
        }
    }
    # Collect task spec lines
    if ($inTask) {
        $taskSpecLines += $line
    }
}

# Fail if task is not found
if (-not $inTask) {
    Write-Error "Task $TaskId not found in TASKS.md"
    exit 1
}

# Join task spec lines
$taskSpec = $taskSpecLines -join "`r`n"

# Replace double quotes with single quotes
$taskSpec = $taskSpec.Replace('"', "'")

# Build and write the tests prompt
$testsTemplate = Get-Content -Raw "scripts/prompts/tests.txt"
$testsPrompt = $testsTemplate.Replace("{TASK_ID}", $TaskId).Replace("{TASK_SPEC}", $taskSpec).Replace('"', "'")
$testsPrompt | Out-File -FilePath "logs/agy-prompt-tests.txt" -Encoding ASCII

# Build and write the implement prompt
$implTemplate = Get-Content -Raw "scripts/prompts/implement.txt"
$implPrompt = $implTemplate.Replace("{TASK_ID}", $TaskId).Replace("{TASK_SPEC}", $taskSpec).Replace('"', "'")
$implPrompt | Out-File -FilePath "logs/agy-prompt-implement.txt" -Encoding ASCII

# Remember start time
$startTime = Get-Date

# Get initial list of changed files
$initialFiles = @()
$statusLines = git status --porcelain -uall
if ($null -ne $statusLines) {
    foreach ($line in $statusLines) {
        $initialFiles += $line.Substring(3).Trim()
    }
}

# Record start time for step 1
$step1Start = Get-Date

# Run step 1 (tests)
$testsOutput = & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-run.ps1 "logs/agy-prompt-tests.txt" 2>&1 | Out-String

# Retry if headless command abort occurs
$retries = 0
if ($testsOutput -match "no output produced") {
    $retries = 1
    $testsOutput += "`n--- RETRY ---`n"
    $testsOutput += & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-run.ps1 "logs/agy-prompt-tests.txt" 2>&1 | Out-String
}

# Calculate step 1 duration
$step1End = Get-Date
$step1Duration = [math]::Floor($step1End.Subtract($step1Start).TotalSeconds)

# Get list of files changed in step 1
$step1Files = @()
$currentFiles = @()
$statusLines = git status --porcelain -uall
if ($null -ne $statusLines) {
    foreach ($line in $statusLines) {
        $currentFiles += $line.Substring(3).Trim()
    }
}
foreach ($f in $currentFiles) {
    if ($initialFiles -notcontains $f) {
        $step1Files += $f
    }
}

# Calculate SHA256 hashes for step 1 files
$step1Hashes = @{}
foreach ($f in $step1Files) {
    if (Test-Path $f -PathType Leaf) {
        $hash = (Get-FileHash -Path $f -Algorithm SHA256).Hash
        $step1Hashes[$f] = $hash
    }
}

# Check if production code changed in tests step
$flags = @()
$prodChangedInTests = $false
foreach ($f in $step1Files) {
    if ($f -match "src/main") {
        $prodChangedInTests = $true
        $flags += "production code changed in tests step"
        break
    }
}

# Collect agy reply lines mentioning remove from tests step
$filesToRemove = @()
$testsOutputLines = $testsOutput -split "`r?`n"
foreach ($line in $testsOutputLines) {
    if ($line -match "remove" -and $line -notmatch "agy-run") {
        $filesToRemove += $line
    }
}

# Stop and report if production code changed in tests step
if ($prodChangedInTests) {
    $reportLines = @()
    $reportLines += "Task: $TaskId"
    $reportLines += "Start: $($startTime.ToString('yyyy-MM-dd HH:mm:ss'))"
    $reportLines += "Step 1 duration: ${step1Duration}s"
    $reportLines += "Retries: $retries"
    $reportLines += "Step 1 files (tests):"
    foreach ($f in $step1Files) { $reportLines += "- $f" }
    $reportLines += "Step 2 files (production):"
    $reportLines += "Step 2 files (tests):"
    $reportLines += "Tests changed during implement:"
    $reportLines += "Status: STUCK"
    $reportLines += "Fix rounds: 0"
    $reportLines += "Last Tests run summaries:"
    $reportLines += "Agy replies mentioning remove:"
    foreach ($line in $filesToRemove) { $reportLines += "- $line" }
    $reportLines += "Flags:"
    foreach ($flag in $flags) { $reportLines += "- $flag" }
    $reportLines -join "`r`n" | Out-File -FilePath "logs/agy-report.md" -Encoding ASCII
    exit 1
}

# Record start time for step 2
$step2Start = Get-Date

# Run step 2 (implement)
$implOutput = & powershell -NoProfile -ExecutionPolicy Bypass -File scripts/agy-impl-loop.ps1 "logs/agy-prompt-implement.txt" 2>&1 | Out-String

# Calculate step 2 duration
$step2End = Get-Date
$step2Duration = [math]::Floor($step2End.Subtract($step2Start).TotalSeconds)

# Get files changed by step 2
$step2Files = @()
$finalFiles = @()
$statusLines = git status --porcelain -uall
if ($null -ne $statusLines) {
    foreach ($line in $statusLines) {
        $finalFiles += $line.Substring(3).Trim()
    }
}
foreach ($f in $finalFiles) {
    if ($initialFiles -notcontains $f) {
        $changedInStep2 = $false
        if ($step1Hashes.ContainsKey($f)) {
            if (Test-Path $f -PathType Leaf) {
                $newHash = (Get-FileHash -Path $f -Algorithm SHA256).Hash
                if ($step1Hashes[$f] -ne $newHash) {
                    $changedInStep2 = $true
                }
            } else {
                $changedInStep2 = $true
            }
        } else {
            $changedInStep2 = $true
        }
        
        if ($changedInStep2) {
            $step2Files += $f
        }
    }
}

# Split step 2 files into production and tests
$step2ProdFiles = @()
$step2TestFiles = @()
foreach ($f in $step2Files) {
    if ($f -match "src/test") {
        $step2TestFiles += $f
    } else {
        $step2ProdFiles += $f
    }
}

# Compare hashes to find tests changed during implement
$testsChangedInImpl = @()
foreach ($f in $step1Files) {
    if (Test-Path $f -PathType Leaf) {
        $newHash = (Get-FileHash -Path $f -Algorithm SHA256).Hash
        if ($step1Hashes.ContainsKey($f) -and $step1Hashes[$f] -ne $newHash) {
            $testsChangedInImpl += $f
        }
    } elseif ($step1Hashes.ContainsKey($f)) {
        $testsChangedInImpl += $f
    }
}

# Add flag if tests changed during implement
if ($step2TestFiles.Count -gt 0 -or $testsChangedInImpl.Count -gt 0) {
    $flags += "tests changed in implement step"
}

# Parse green or stuck status
$status = "UNKNOWN"
if ($implOutput -match "GREEN") {
    $status = "GREEN"
} elseif ($implOutput -match "STUCK") {
    $status = "STUCK"
}

# Extract fix rounds
$fixRounds = 0
if ($implOutput -match "after (\d+) fix round") {
    $fixRounds = $matches[1]
}

# Collect agy reply lines mentioning remove from implement step
$implOutputLines = $implOutput -split "`r?`n"
foreach ($line in $implOutputLines) {
    if ($line -match "remove" -and $line -notmatch "agy-impl-loop") {
        $filesToRemove += $line
    }
}

# Extract last Tests run summary line per module
$testRunLines = @()
foreach ($line in $implOutputLines) {
    if ($line -match "^\[INFO\] Tests run:.*") {
        $testRunLines += $line
    }
}

# Write final report
$reportLines = @()
$reportLines += "Task: $TaskId"
$reportLines += "Start: $($startTime.ToString('yyyy-MM-dd HH:mm:ss'))"
$reportLines += "Step 1 duration: ${step1Duration}s"
$reportLines += "Step 2 duration: ${step2Duration}s"
$reportLines += "Retries: $retries"
$reportLines += "Step 1 files (tests):"
foreach ($f in $step1Files) { $reportLines += "- $f" }
$reportLines += "Step 2 files (production):"
foreach ($f in $step2ProdFiles) { $reportLines += "- $f" }
$reportLines += "Step 2 files (tests):"
foreach ($f in $step2TestFiles) { $reportLines += "- $f" }
$reportLines += "Tests changed during implement:"
foreach ($f in $testsChangedInImpl) { $reportLines += "- $f" }
$reportLines += "Status: $status"
$reportLines += "Fix rounds: $fixRounds"
$reportLines += "Last Tests run summaries:"
foreach ($line in $testRunLines) { $reportLines += $line }
$reportLines += "Agy replies mentioning remove:"
foreach ($line in $filesToRemove) { $reportLines += "- $line" }
$reportLines += "Flags:"
foreach ($flag in $flags) { $reportLines += "- $flag" }
$reportLines -join "`r`n" | Out-File -FilePath "logs/agy-report.md" -Encoding ASCII

# Exit according to status and flags
if ($status -eq "GREEN" -and $flags.Count -eq 0) {
    exit 0
} else {
    exit 1
}
