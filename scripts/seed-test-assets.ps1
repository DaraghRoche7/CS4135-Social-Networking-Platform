param(
  [string]$ModuleCode = "CS4135",
  [string]$GatewayBaseUrl = "http://localhost:8080",
  [string]$TestEmail = "seedbot@studentmail.ul.ie",
  [string]$TestPassword = "StudyHub!2026",
  [string]$TestName = "Seed Bot"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$assetsDir = Join-Path $repoRoot "test-assets"

if (!(Test-Path $assetsDir)) {
  throw "test-assets folder not found at: $assetsDir"
}

$files = Get-ChildItem -Path $assetsDir -File | Where-Object { $_.Name -ne ".gitkeep" }
if ($files.Count -eq 0) {
  throw "No files found in test-assets/ (besides .gitkeep)."
}

Write-Host "Seeding $($files.Count) assets as posts under module '$ModuleCode'..."

# 1) Register (idempotent-ish: if already exists, login)
$registerBody = @{
  name = $TestName
  email = $TestEmail
  password = $TestPassword
} | ConvertTo-Json

$auth = $null
try {
  $auth = Invoke-RestMethod -Uri "$GatewayBaseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $registerBody
  Write-Host "Registered test user: $($auth.userId)"
} catch {
  # If already registered, login instead.
  $loginBody = @{ email = $TestEmail; password = $TestPassword } | ConvertTo-Json
  $auth = Invoke-RestMethod -Uri "$GatewayBaseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
  Write-Host "Logged in test user: $($auth.userId)"
}

if (-not $auth.userId) {
  throw "Auth response did not include userId. Cannot seed."
}

$uploaderUserId = [string]$auth.userId
$module = $ModuleCode.Trim().ToUpperInvariant()

# 2) Insert into support-service DB (posts table)
# We store the filenames as titles/descriptions so they appear in the feed like notes.
$values = @()
$i = 0
foreach ($f in $files) {
  $i++
  $title = $f.Name.Replace("'", "''")
  $desc = ("Seeded test note from test-assets: " + $f.Name).Replace("'", "''")
  # stagger times so ordering is visible
  $minutesAgo = ($i - 1) * 7
  $values += "('$title', '$desc', '$module', '$uploaderUserId', now() - interval '$minutesAgo minutes', 0)"
}

$sql = @"
INSERT INTO posts (title, description, module_code, uploader_user_id, created_at, like_count)
VALUES
  $(($values -join ",`n  "));
"@

docker exec -i studyhub-postgres psql -U studyhub -d studyhub -c $sql | Out-Null

Write-Host "Done."
Write-Host "Next: log into your main account, follow module '$module', and refresh the feed."

