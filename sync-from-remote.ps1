param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRepo,

    [string]$SourceBranch = "main",

    [string]$LocalRepo = (Get-Location).Path,

    [string[]]$ExcludeDirs = @(".git", "node_modules", ".next", "dist", "build"),

    [string[]]$ExcludeFiles = @(".env", ".env.local", "sync-from-remote.ps1"),

    [switch]$DryRun,

    [switch]$AllowDirty
)

$ErrorActionPreference = "Stop"

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command,

        [Parameter(Mandatory = $true)]
        [string[]]$Arguments,

        [int]$MaxSuccessExitCode = 0
    )

    & $Command @Arguments

    if ($LASTEXITCODE -gt $MaxSuccessExitCode) {
        throw "Command failed: $Command $($Arguments -join ' ') exited with code $LASTEXITCODE."
    }
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git was not found in PATH."
}

if (-not (Get-Command robocopy -ErrorAction SilentlyContinue)) {
    throw "Robocopy was not found. This script is intended to run on Windows."
}

$localGitDir = Join-Path $LocalRepo ".git"
if (-not (Test-Path $localGitDir)) {
    throw "Local folder is not a Git repository: $LocalRepo"
}

$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$tempRepo = Join-Path $env:TEMP "repo-source-$stamp"
$backupBranch = "backup-before-sync-$stamp"

Push-Location $LocalRepo

try {
    if (-not $AllowDirty) {
        $changes = git status --porcelain

        if ($changes) {
            throw "Local repo has uncommitted changes. Commit or stash them before running, or use -AllowDirty."
        }
    }

    if (-not $DryRun) {
        Invoke-CheckedCommand -Command "git" -Arguments @("branch", $backupBranch)
    }

    Invoke-CheckedCommand -Command "git" -Arguments @(
        "clone",
        "--depth",
        "1",
        "--branch",
        $SourceBranch,
        $SourceRepo,
        $tempRepo
    )

    $robocopyArgs = @($tempRepo, $LocalRepo, "/MIR")

    if ($DryRun) {
        $robocopyArgs += "/L"
    }

    if ($ExcludeDirs.Count -gt 0) {
        $robocopyArgs += "/XD"
        $robocopyArgs += $ExcludeDirs
    }

    if ($ExcludeFiles.Count -gt 0) {
        $robocopyArgs += "/XF"
        $robocopyArgs += $ExcludeFiles
    }

    & robocopy @robocopyArgs

    if ($LASTEXITCODE -ge 8) {
        throw "Robocopy failed with exit code $LASTEXITCODE."
    }

    git status --short

    Write-Host ""

    if ($DryRun) {
        Write-Host "Dry run finished. No files were copied or deleted."
        Write-Host "Run again without -DryRun to apply the sync."
    }
    else {
        Write-Host "Files synchronized."
        Write-Host "Backup branch created: $backupBranch"
        Write-Host ""
        Write-Host "Review the changes, then run:"
        Write-Host "git add -A"
        Write-Host 'git commit -m "Sync files from external repo"'
        Write-Host "git push origin <your-branch>"
    }
}
finally {
    Pop-Location

    if (Test-Path $tempRepo) {
        Remove-Item $tempRepo -Recurse -Force
    }
}
