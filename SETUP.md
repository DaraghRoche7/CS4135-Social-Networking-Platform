# Git Setup for Team Members

This guide will help you set up Git for working on this project.

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform.git
cd CS4135-Social-Networking-Platform
```

### 2. Configure Git

Set your name and email (use your actual name and email):

```bash
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

**Note:** If you want to set this globally for all repositories on your machine, add the `--global` flag:
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

### 3. Verify Setup

Make sure you're on the main branch and up to date:

```bash
git checkout main
git pull origin main
```

## Daily Workflow

### Starting Work

Always start by updating your local main branch:

```bash
git checkout main
git pull origin main
```

### Creating a Feature Branch

Create a new branch for your work:

```bash
# For frontend work
git checkout -b feature/frontend/user-authentication

# For backend work
git checkout -b feature/backend/core-service/jwt-implementation
# or
git checkout -b fix/backend/support-service/feed-cache-issue
```

See [BRANCHING_STRATEGY.md](./BRANCHING_STRATEGY.md) for branch naming conventions.

### Making Changes

1. **Make your changes** to the code
2. **Stage your changes:**
   ```bash
   git add .
   # or add specific files
   git add path/to/file
   ```
3. **Commit with a clear message:**
   ```bash
   git commit -m "feat(frontend): add user authentication page"
   ```
   See [BRANCHING_STRATEGY.md](./BRANCHING_STRATEGY.md) for commit message guidelines.

### Pushing Your Work

Push your branch to GitHub:

```bash
git push -u origin feature/frontend/user-authentication
```

The `-u` flag sets up tracking so future pushes can just use `git push`.

### Creating a Pull Request

1. Go to the repository on GitHub
2. Click "New Pull Request"
3. Select your branch
4. Fill out the PR template
5. Request review from a team member

## Keeping Your Branch Up to Date

If main has new changes while you're working:

```bash
# Switch to main and update
git checkout main
git pull origin main

# Switch back to your branch
git checkout feature/frontend/user-authentication

# Merge main into your branch
git merge main
# or rebase (if preferred)
git rebase main
```

## Troubleshooting

### Permission Denied
- Check that you have write access to the repository
- Verify your GitHub authentication (SSH keys or personal access token)

### Branch Protection Issues
- You cannot push directly to `main`
- Always create a feature branch and use Pull Requests

### Merge Conflicts
- Resolve conflicts in your editor
- After resolving, stage the files: `git add .`
- Complete the merge: `git commit`

### Authentication Issues
- **SSH:** Make sure your SSH key is added to GitHub
- **HTTPS:** You may need a personal access token instead of a password
- See [GitHub authentication guide](https://docs.github.com/en/authentication)

## Useful Git Commands

```bash
# Check current status
git status

# View commit history
git log

# View changes in your working directory
git diff

# Discard local changes (be careful!)
git checkout -- filename

# View all branches
git branch -a

# Delete a local branch
git branch -d branch-name

# Delete a remote branch (after PR is merged)
git push origin --delete branch-name
```

## Next Steps

1. Read [BRANCHING_STRATEGY.md](./BRANCHING_STRATEGY.md) for detailed workflow guidelines
2. Read [CONTRIBUTING.md](./CONTRIBUTING.md) for coding standards
3. Check **Microsoft Planner** for assigned tasks
4. Start coding!

---

**Questions?** Ask in the group chat or check Microsoft Planner for updates.
