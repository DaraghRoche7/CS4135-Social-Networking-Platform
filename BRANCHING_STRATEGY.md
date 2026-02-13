# Trunk-Based Development Strategy

## Overview
This project uses **Trunk-Based Development** - a branching model where all developers work on short-lived feature branches that are frequently merged into the `main` branch (the "trunk").

## Why Trunk-Based Development?
- Reduces merge conflicts through frequent integration
- Faster feedback loops
- Simpler workflow for our team
- Encourages small, incremental changes
- Better code quality through continuous integration

## Branch Structure

### Main Branch (`main`)
- **Protected branch** - requires pull request approval
- Always in a deployable state
- All feature branches merge here
- No direct commits (except hotfixes in emergencies)

### Feature Branches
- **Naming convention**: `feature/<area>/<brief-description>`
  - Examples:
    - `feature/frontend/user-authentication`
    - `feature/backend/core-service/jwt-implementation`
    - `feature/backend/support-service/feed-generation`
    - `feature/frontend/post-creation-form`

### Bug Fix Branches
- **Naming convention**: `fix/<area>/<brief-description>` or `bugfix/<area>/<brief-description>`
  - Examples:
    - `fix/frontend/login-redirect-issue`
    - `fix/backend/core-service/jwt-expiration-handling`
    - `bugfix/frontend/form-validation-error`
    - `fix/backend/support-service/feed-cache-invalidation`
- **Use case**: Non-critical bugs found during development
- **Lifespan**: Hours to 1-2 days maximum
- **Size**: Small, focused bug fixes
- **Merge**: Via Pull Request with at least 1 approval

### Hotfix Branches (Emergency Only)
- **Naming convention**: `hotfix/<brief-description>`
  - Examples:
    - `hotfix/critical-auth-bypass`
    - `hotfix/data-loss-prevention`
    - `hotfix/security-vulnerability`
- **Use case**: Critical production bugs that need immediate fix
- **Process**: 
  1. Create from `main`
  2. Fix the issue
  3. Create PR with urgency label
  4. Get expedited review and approval
  5. Merge and deploy immediately

## Workflow

### Daily Development Flow

1. **Start of Day**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Create Feature or Fix Branch**
   ```bash
   # For new features
   git checkout -b feature/frontend/user-auth
   git checkout -b feature/backend/core-service/jwt
   
   # For bug fixes
   git checkout -b fix/frontend/login-redirect-issue
   git checkout -b bugfix/backend/core-service/jwt-expiration
   ```

3. **Work on Feature/Fix**
   - Make small, frequent commits
   - Write clear commit messages
   - Keep branch up to date with main:
     ```bash
     git checkout main
     git pull origin main
     git checkout feature/frontend/user-auth  # or fix/backend/core-service/...
     git merge main  # or rebase if preferred
     ```

4. **Push and Create PR**
   ```bash
   git push origin feature/frontend/user-auth
   # or
   git push origin fix/backend/core-service/jwt-expiration
   ```
   - Create Pull Request on GitHub
   - Request review from at least 1 team member
   - Address feedback

5. **Merge to Main**
   - Once approved, merge via GitHub
   - Delete feature branch after merge
   - Pull latest main:
     ```bash
     git checkout main
     git pull origin main
     ```

## Commit Message Guidelines

Follow conventional commits format:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(frontend): add user authentication page
feat(backend/core-service): implement JWT authentication
fix(frontend): resolve login redirect issue
fix(backend/support-service): resolve feed caching issue
bugfix(backend/core-service): handle JWT expiration properly
docs: update API documentation
refactor(frontend): optimize component structure
```

## Pull Request Guidelines

### PR Title Format
```
<type>(<area>): <brief description>
```

**Examples:**
- `feat(frontend): add user authentication page`
- `fix(backend/core-service): resolve JWT expiration handling`
- `bugfix(frontend): fix login redirect issue`
- `docs: update API documentation`

### PR Description Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Documentation update
- [ ] Refactoring
- [ ] Other (please describe)

## Area(s) Affected
- [ ] Frontend (React)
- [ ] Backend - API Gateway
- [ ] Backend - Core Service
- [ ] Backend - Support Service

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated (if needed)
- [ ] No new warnings generated
- [ ] Tests pass locally
```

## Code Review Process

1. **Self-Review First**
   - Review your own PR before requesting review
   - Check for typos, unused code, and obvious issues

2. **Request Review**
   - Assign at least 1 team member as reviewer
   - Use `@mentions` for specific questions

3. **Review Guidelines**
   - Be constructive and respectful
   - Focus on code quality, not personal preferences
   - Approve if changes look good
   - Request changes if issues found

4. **Addressing Feedback**
   - Make requested changes
   - Push updates to the same branch
   - Re-request review if needed

## Branch Protection Rules

The `main` branch is protected with:
- ✅ Require pull request reviews (1 approval minimum)
- ✅ Require status checks to pass
- ✅ Require branches to be up to date before merging
- ✅ No force pushes
- ✅ No deletion of main branch

## Conflict Resolution

If your branch has conflicts with main:
1. Update your branch:
   ```bash
   git checkout main
   git pull origin main
   git checkout feature/frontend/your-branch  # or fix/backend/...
   git merge main
   ```
2. Resolve conflicts in your editor
3. Test your changes
4. Commit the merge:
   ```bash
   git add .
   git commit -m "merge: resolve conflicts with main"
   git push origin feature/frontend/your-branch
   ```

## Best Practices

1. **Keep Branches Small**
   - One feature or bug fix per branch
   - If a feature is large, break it into smaller PRs

2. **Commit Frequently**
   - Small, logical commits
   - Clear commit messages
   - Don't wait until the end of the day

3. **Stay Up to Date**
   - Regularly merge/rebase main into your feature branch
   - Reduces merge conflicts

4. **Test Before PR**
   - Run tests locally
   - Test your changes manually
   - Fix issues before requesting review

5. **Communicate**
   - Update team on what you're working on
   - Ask for help early if stuck
   - Coordinate on shared components

## Team Coordination

### Project Management
- **Microsoft Planner** - Primary tool for task tracking and coordination
  - Check your assigned tasks regularly
  - Update task status as you progress
  - Use task comments to communicate blockers or questions
  - Link Pull Requests to Planner tasks when applicable

### Weekly Standup (maybe more often)
- What did you work on so far?
- What are you working on next?
- Any blockers?
- Update Microsoft Planner task status accordingly

## Emergency Procedures

### Critical Bug in Production
1. Create hotfix branch from main
2. Fix the issue
3. Create urgent PR
4. Get expedited review
5. Merge and deploy immediately

### Broken Main Branch
1. Immediately notify team
2. Identify the problematic commit
3. Create hotfix to revert or fix
4. Deploy fix ASAP
---


