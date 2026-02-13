# Contributing Guide

## Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform.git
   cd CS4135-Social-Networking-Platform
   ```

2. **Set up Git**
   ```bash
   git config user.name "Your Name"
   git config user.email "your.email@example.com"
   ```

3. **Create Feature Branch**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/<service>/<description>
   ```

## Code Standards

### General Guidelines
- Write clear, self-documenting code
- Add comments for complex logic
- Follow existing code style
- Keep functions small and focused
- Write tests for new features

### Naming Conventions
- **Variables/Functions**: `camelCase`
- **Classes/Components**: `PascalCase`
- **Constants**: `UPPER_SNAKE_CASE`
- **Files**: `kebab-case`

### Code Review Checklist
Before submitting a PR, ensure:
- [ ] Code follows project style
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] No console.log or debug code
- [ ] No hardcoded values
- [ ] Error handling implemented
- [ ] Security considerations addressed

## Testing

### Writing Tests
- Write unit tests for individual functions
- Write integration tests for service interactions
- Aim for >80% code coverage
- Test edge cases and error scenarios

### Running Tests
```bash
# All tests
npm test

# Specific service
npm test -- user-service

# Watch mode
npm test -- --watch

# Coverage
npm run test:coverage
```

## Pull Request Process

1. **Update Documentation**
   - Update README if needed
   - Add/update API documentation
   - Update CHANGELOG if applicable

2. **Create Pull Request**
   - Use the PR template
   - Link related issues
   - Request review from team members
   - Ensure CI checks pass

3. **Address Feedback**
   - Respond to all comments
   - Make requested changes
   - Re-request review when ready

4. **After Merge**
   - Delete your feature branch
   - Update your local main branch
   - Celebrate!

## Communication

### Project Management
- **Microsoft Planner** - Primary tool for task tracking and project coordination
  - Check assigned tasks regularly
  - Update task status as you progress
  - Use comments to communicate blockers or questions
  - Link PRs to Planner tasks when applicable

### Asking Questions
- Use **Microsoft Planner** for project-related questions and coordination
- Use GitHub Discussions for technical questions and discussions
- Use PR comments for code-specific questions
- Tag team members when needed

### Reporting Issues
- Use the bug report template on GitHub
- Provide clear steps to reproduce
- Include relevant logs and screenshots
- Consider creating a task in Microsoft Planner for tracking

## Service-Specific Guidelines

### User Service
- Handle authentication securely
- Hash passwords properly
- Validate all user inputs
- Implement rate limiting

### Post Service
- Validate post content
- Handle file uploads securely
- Implement content moderation hooks

### Feed Service
- Optimize for performance
- Use Redis caching effectively
- Handle fan-out efficiently

### Notification Service
- Ensure reliable delivery
- Handle failures gracefully
- Implement retry logic

## Getting Help

- Check existing documentation
- Check **Microsoft Planner** for assigned tasks and project updates
- Search closed issues/PRs
- Ask in GitHub Discussions
- Contact team members via Microsoft Planner or GitHub

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

Thank you for contributing! 🚀


