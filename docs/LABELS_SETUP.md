# GitHub Labels Setup Guide

This guide explains how to set up GitHub labels for the repository.

## Why Labels?

Labels help organize issues and pull requests by:
- Categorizing work (bug, enhancement, documentation)
- Identifying service areas (frontend, backend, core-service, etc.)
- Setting priorities (high, medium, low)
- Marking automated PRs (dependencies, github-actions)

## Setup Methods

### Method 1: Using GitHub CLI (Recommended)

1. **Install GitHub CLI**
   ```powershell
   winget install GitHub.cli
   ```
   Or download from: https://cli.github.com/

2. **Authenticate**
   ```bash
   gh auth login
   ```

3. **Run the setup script**
   ```powershell
   .\scripts\setup-labels.ps1
   ```
   Or on Unix/Mac:
   ```bash
   chmod +x scripts/setup-labels.sh
   ./scripts/setup-labels.sh
   ```

### Method 2: Manual Creation on GitHub

1. Go to your repository on GitHub
2. Click **Issues** → **Labels**
3. Click **New label**
4. Create each label with the name, color, and description from the table below

### Method 3: Using GitHub API (Advanced)

You can use the GitHub API to create labels programmatically. See the `scripts/setup-labels.ps1` file for reference.

## Required Labels

The following labels are defined in `.github/labels.json`:

| Label Name | Color | Description |
|------------|-------|-------------|
| `bug` | #d73a4a | Something isn't working |
| `enhancement` | #a2eeef | New feature or request |
| `documentation` | #0075ca | Improvements or additions to documentation |
| `frontend` | #0e8a16 | Related to React frontend |
| `backend` | #0e8a16 | Related to Spring Boot backend |
| `api-gateway` | #0e8a16 | Related to API Gateway |
| `core-service` | #0e8a16 | Related to Core Service |
| `support-service` | #0e8a16 | Related to Support Service |
| `good first issue` | #7057ff | Good for newcomers |
| `help wanted` | #008672 | Extra attention is needed |
| `priority: high` | #b60205 | High priority issue |
| `priority: medium` | #fbca04 | Medium priority issue |
| `priority: low` | #0e8a16 | Low priority issue |
| `dependencies` | #0366d6 | Pull requests that update a dependency file |
| `github-actions` | #0366d6 | GitHub Actions related |
| `automated` | #ededed | Automated pull requests |

## After Labels Are Created

Once labels are created, you can:

1. **Re-enable labels in Dependabot** (if they were commented out):
   - Edit `.github/dependabot.yml`
   - Uncomment the `labels:` sections
   - Commit and push

2. **Use labels on issues and PRs**:
   - Manually add labels when creating issues/PRs
   - Labels help organize and filter work

## Troubleshooting

### Labels not showing up?
- Make sure you have write access to the repository
- Check that labels were created successfully on GitHub
- Refresh the GitHub page

### Dependabot still complaining about labels?
- Make sure all three labels exist: `dependencies`, `automated`, `github-actions`
- Check spelling (case-sensitive)
- Wait a few minutes for GitHub to sync

---

**Note:** Labels are optional but recommended for better project organization.

