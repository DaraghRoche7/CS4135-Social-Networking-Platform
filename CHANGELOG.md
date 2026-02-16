# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Services
- User Service (to be implemented)
- Post Service (to be implemented)
- Feed Service (to be implemented)
- Notification Service (to be implemented)

### Infrastructure
- Redis caching setup (to be implemented)
- PostgreSQL database setup (to be implemented)
- RabbitMQ message queue setup (to be implemented)

---

## [0.1.0] - 16-02-2026

### Added
- **Project Documentation**
  - `BRANCHING_STRATEGY.md` - Complete trunk-based development workflow guide
  - `CONTRIBUTING.md` - Contribution guidelines and coding standards
  - `SETUP.md` - Git setup instructions for team members
  - `CHANGELOG.md` - Project changelog tracking
  - `docs/PR_PROCESS_SUMMARY.md` - Quick reference for PR process
  - `docs/LABELS_SETUP.md` - Guide for setting up GitHub labels

- **Wiki Documentation Structure**
  - Home page with navigation
  - Architecture (Planned) - Microservices, gateway, messaging architecture
  - Services (Planned) - Service responsibilities and structure
  - API Contracts (Planned) - API documentation structure
  - Authentication & Security (Planned) - JWT, RBAC, security implementation
  - Events & Messaging (Planned) - Event-driven architecture and messaging
  - Deployment (Planned) - Deployment strategy and hosting

- **GitHub Automation**
  - CI Pipeline workflow - Runs tests and builds (skips when directories don't exist)
  - PR Checks workflow - Validates PR titles and branch names (optional validation)
  - Branch Cleanup workflow - Auto-deletes merged feature branches
  - Issue templates - Bug report and feature request templates
  - Pull request template - Standardized PR format

- **Project Structure**
  - Directory structure for frontend (React) and backend (Spring Boot)
  - Service directories: `services/`, `shared/`, `docs/`, `tests/`, `wiki/`
  - `.gitignore` configured for common files and dependencies

- **Development Workflow**
  - Trunk-based development strategy
  - Branch naming conventions: `feature/`, `fix/`, `bugfix/`, `hotfix/`
  - PR process with review requirements
  - Microsoft Planner integration for task tracking

### Changed
- Made PR title validation optional to prevent blocking merges
- CI pipeline configured to skip gracefully when code directories don't exist

### Removed
- Dependabot configuration - Removed due to overhead for small team (can be added back if needed)

### Infrastructure
- Docker Compose configuration prepared for local development
- Project structure ready for React frontend and Spring Boot microservices

[Unreleased]: https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/DaraghRoche7/CS4135-Social-Networking-Platform/releases/tag/v0.1.0
