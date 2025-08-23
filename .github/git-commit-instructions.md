# Conventional Commits

## Summary

The Conventional Commits specification is a lightweight convention for commit messages. It provides clear rules for
creating an explicit commit history, making it easier to write automated tools and maintain semantic versioning.

## Commit Message Structure

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

- **fix:** patches a bug in your codebase (correlates with PATCH in Semantic Versioning)
- **feat:** introduces a new feature (correlates with MINOR in Semantic Versioning)
- **BREAKING CHANGE:** a commit with a BREAKING CHANGE footer or a ! after type/scope introduces a breaking API change (
  correlates with MAJOR in Semantic Versioning)
- Other types (e.g., build, chore, ci, docs, style, refactor, perf, test) are allowed and provide additional context

### Scope

A scope may be provided to a commit’s type for additional context, contained within parentheses, e.g.,
`feat(parser): add ability to parse arrays`.

### Footers

Footers such as BREAKING CHANGE: <description> may be provided and follow a convention similar to git trailer format.

## Examples

- **Commit message with description and breaking change footer:**
  ```
  feat: allow provided config object to extend other configs

  BREAKING CHANGE: `extends` key in config file is now used for extending other config files
  ```
- **Commit message with ! to draw attention to breaking change:**
  ```
  feat!: send an email to the customer when a product is shipped
  ```
- **Commit message with scope and ! to draw attention to breaking change:**
  ```
  feat(parser)!: support new array parsing
  ```
