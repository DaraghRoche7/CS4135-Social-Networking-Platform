#!/bin/bash

# Script to set up GitHub labels for the repository
# Usage: ./scripts/setup-labels.sh

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Setting up GitHub labels...${NC}"

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo "GitHub CLI (gh) is not installed. Please install it first:"
    echo "  Windows: winget install GitHub.cli"
    echo "  macOS: brew install gh"
    echo "  Linux: https://cli.github.com/manual/installation"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo "Please authenticate with GitHub first:"
    echo "  gh auth login"
    exit 1
fi

# Get repository info
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)

if [ -z "$REPO" ]; then
    echo "Could not determine repository. Make sure you're in the repository directory."
    exit 1
fi

echo "Repository: $REPO"

# Create labels from JSON file
if [ -f ".github/labels.json" ]; then
    echo -e "${GREEN}Creating labels from .github/labels.json...${NC}"
    
    # Read labels.json and create each label
    cat .github/labels.json | jq -r '.[] | "\(.name)|\(.color)|\(.description // "")"' | while IFS='|' read -r name color description; do
        if [ -z "$description" ]; then
            gh label create "$name" --color "$color" --force 2>/dev/null || echo "Label '$name' already exists or could not be created"
        else
            gh label create "$name" --color "$color" --description "$description" --force 2>/dev/null || echo "Label '$name' already exists or could not be created"
        fi
    done
    
    echo -e "${GREEN}Labels setup complete!${NC}"
else
    echo "Error: .github/labels.json not found"
    exit 1
fi



