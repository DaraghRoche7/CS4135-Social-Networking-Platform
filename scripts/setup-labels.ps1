# PowerShell script to set up GitHub labels for the repository
# Usage: .\scripts\setup-labels.ps1

Write-Host "Setting up GitHub labels..." -ForegroundColor Yellow

# Check if gh CLI is installed
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "GitHub CLI (gh) is not installed. Please install it first:" -ForegroundColor Red
    Write-Host "  winget install GitHub.cli" -ForegroundColor Yellow
    Write-Host "  Or visit: https://cli.github.com/manual/installation" -ForegroundColor Yellow
    exit 1
}

# Check if authenticated
try {
    $null = gh auth status 2>&1
} catch {
    Write-Host "Please authenticate with GitHub first:" -ForegroundColor Red
    Write-Host "  gh auth login" -ForegroundColor Yellow
    exit 1
}

# Get repository info
$repo = gh repo view --json nameWithOwner -q .nameWithOwner

if (-not $repo) {
    Write-Host "Could not determine repository. Make sure you're in the repository directory." -ForegroundColor Red
    exit 1
}

Write-Host "Repository: $repo" -ForegroundColor Cyan

# Create labels from JSON file
if (Test-Path ".github/labels.json") {
    Write-Host "Creating labels from .github/labels.json..." -ForegroundColor Green
    
    $labels = Get-Content ".github/labels.json" | ConvertFrom-Json
    
    foreach ($label in $labels) {
        $name = $label.name
        $color = $label.color
        $description = $label.description
        
        if ($description) {
            gh label create $name --color $color --description $description --force 2>$null
        } else {
            gh label create $name --color $color --force 2>$null
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Created label: $name" -ForegroundColor Green
        } else {
            Write-Host "  Label '$name' already exists or could not be created" -ForegroundColor Yellow
        }
    }
    
    Write-Host "Labels setup complete!" -ForegroundColor Green
} else {
    Write-Host "Error: .github/labels.json not found" -ForegroundColor Red
    exit 1
}



