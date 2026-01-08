#!/bin/bash

################################################################################
# Test Session Configuration Script
#
# Creates a test tag to verify session parsing without full build
#
# Usage:
#   ./scripts/test-session.sh [--with-session]
#
# Examples:
#   ./scripts/test-session.sh                 # Test without session
#   ./scripts/test-session.sh --with-session  # Test with session
################################################################################

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║           Session Configuration Test                      ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Parse arguments
WITH_SESSION=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --with-session)
            WITH_SESSION=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--with-session]"
            echo ""
            echo "Creates a test tag to verify session configuration parsing"
            echo ""
            echo "Options:"
            echo "  --with-session    Create tag with session markers"
            echo "  -h, --help        Show this help"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

print_header

# Generate test tag name with timestamp
TIMESTAMP=$(date +%s)
TEST_TAG="test-session-$TIMESTAMP"

print_info "Creating test tag: $TEST_TAG"
echo ""

# Build tag message
TAG_MESSAGE="Test Session Configuration

Timestamp: $(date '+%Y-%m-%d %H:%M:%S')
Test Tag: $TEST_TAG

This is a test tag to verify session configuration parsing.
No actual build will occur."

# Add session markers if requested
if [[ "$WITH_SESSION" == true ]]; then
    TAG_MESSAGE="${TAG_MESSAGE}

Session Configuration:
- Auto-start session: enabled
- Device ID: 67a642531a4aa535498192f8
- Session validity: 120 seconds

[NB_SESSION_ENABLED]
[NB_DEVICE_ID:67a642531a4aa535498192f8]
[NB_SESSION_VALIDITY:120]"

    print_success "Session markers will be included"
else
    print_warning "Session markers will NOT be included"
fi

echo ""
print_info "Tag message preview:"
echo "─────────────────────────────────────────────────────────────"
echo "$TAG_MESSAGE"
echo "─────────────────────────────────────────────────────────────"
echo ""

# Create the tag
git tag -a "$TEST_TAG" -m "$TAG_MESSAGE"
print_success "Tag created locally: $TEST_TAG"

# Push the tag
print_info "Pushing tag to GitHub..."
git push origin "$TEST_TAG"
print_success "Tag pushed to GitHub"

echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Test Tag Created Successfully! 🎉${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════${NC}"
echo ""
print_info "What happens next:"
echo "  1. GitHub Actions will trigger the test workflow"
echo "  2. Check workflow at: https://github.com/$(git config --get remote.origin.url | sed 's/.*://; s/.git$//')/actions"
echo "  3. Look for workflow: 'Test Session Parsing'"
echo "  4. Review the test results in the workflow summary"
echo ""
print_info "To clean up later:"
echo "  git tag -d $TEST_TAG"
echo "  git push origin :refs/tags/$TEST_TAG"
echo ""

if [[ "$WITH_SESSION" == true ]]; then
    print_success "✅ Session markers included - should trigger session steps"
else
    print_warning "❌ No session markers - session steps should be skipped"
fi
