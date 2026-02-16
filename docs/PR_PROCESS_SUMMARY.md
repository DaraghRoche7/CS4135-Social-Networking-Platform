# Pull Request Process - Quick Reference

## Quick PR Process

1. **Create Branch**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/frontend/user-auth  # or fix/backend/...
   ```

2. **Make Changes & Commit**
   ```bash
   git add .
   git commit -m "feat(frontend): add user authentication"
   git push -u origin feature/frontend/user-auth
   ```

3. **Create PR on GitHub**
   - Go to repository → "New Pull Request"
   - Select your branch
   - Fill out the PR template
   - Request review from 1 team member

4. **Address Feedback**
   - Make requested changes
   - Push updates to same branch
   - Re-request review if needed

5. **Merge**
   - Once approved, merge via GitHub
   - Branch auto-deletes after merge

## PR Title Format

```
<type>(<area>): <description>
```

**Examples:**
- `feat(frontend): add user authentication page`
- `fix(backend/core-service): resolve JWT expiration`
- `docs: update API documentation`

**Types:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

## PR Checklist

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Tests pass locally
- [ ] Documentation updated (if needed)
- [ ] Branch is up to date with main
- [ ] At least 1 approval

## Why PR Checks Run

**PR checks run on every PR** - this is normal! They:
- Run when PR is **opened**
- Run when PR is **updated** (new commits pushed)
- Run when PR is **reopened**

**Even after merging**, you might see them in the PR history - that's just showing what ran during the PR's lifetime.

**Important:** PR checks don't prevent you from merging if they fail (unless branch protection requires them). They're informational.

## Full Details

See [BRANCHING_STRATEGY.md](../BRANCHING_STRATEGY.md) for complete PR guidelines.

