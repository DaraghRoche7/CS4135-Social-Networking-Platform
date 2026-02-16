# Manual Label Creation Guide

Since GitHub CLI may not be available, here's how to create labels manually or via GitHub web interface.

## Quick Method: GitHub Web Interface

1. Go to: https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform
2. Click **Issues** → **Labels**
3. Click **New label**
4. Create each label from the list below

## Required Labels for Dependabot

Create these three labels first (they're needed for Dependabot):

1. **dependencies**
   - Color: `#0366d6` (blue)
   - Description: "Pull requests that update a dependency file"

2. **automated**
   - Color: `#ededed` (light gray)
   - Description: "Automated pull requests"

3. **github-actions**
   - Color: `#0366d6` (blue)
   - Description: "GitHub Actions related"

## All Labels (from labels.json)

Create all labels for better organization:

| Name | Color | Description |
|------|-------|-------------|
| bug | #d73a4a | Something isn't working |
| enhancement | #a2eeef | New feature or request |
| documentation | #0075ca | Improvements or additions to documentation |
| frontend | #0e8a16 | Related to React frontend |
| backend | #0e8a16 | Related to Spring Boot backend |
| api-gateway | #0e8a16 | Related to API Gateway |
| core-service | #0e8a16 | Related to Core Service |
| support-service | #0e8a16 | Related to Support Service |
| good first issue | #7057ff | Good for newcomers |
| help wanted | #008672 | Extra attention is needed |
| priority: high | #b60205 | High priority issue |
| priority: medium | #fbca04 | Medium priority issue |
| priority: low | #0e8a16 | Low priority issue |
| dependencies | #0366d6 | Pull requests that update a dependency file |
| github-actions | #0366d6 | GitHub Actions related |
| automated | #ededed | Automated pull requests |

## After Creating Labels

Once labels are created:

1. **Re-enable in dependabot.yml** (if commented out):
   - Edit `.github/dependabot.yml`
   - Uncomment the `labels:` sections
   - Commit and push

2. **Verify Dependabot works**:
   - Check that new Dependabot PRs have labels
   - If errors persist, wait a few minutes for GitHub to sync

---

**Note:** You only need the 3 Dependabot labels (`dependencies`, `automated`, `github-actions`) for Dependabot to work. The others are optional but recommended.

