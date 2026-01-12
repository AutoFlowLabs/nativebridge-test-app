#!/bin/bash

################################################################################
# NativeBridge Release Script
#
# Simple automated release process:
# 1. Bumps version in package.json and Android build.gradle
# 2. Commits version changes
# 3. Creates and pushes git tag
# 4. Triggers CI/CD pipeline
#
# Usage:
#   ./scripts/release.sh <version> [options]
#
# Examples:
#   ./scripts/release.sh 1.0.0
#   ./scripts/release.sh 1.2.3
#   ./scripts/release.sh 2.0.0-beta
#
# Author: NativeBridge Team
# License: MIT
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Files to update
PACKAGE_JSON="$PROJECT_ROOT/package.json"
ANDROID_BUILD_GRADLE="$PROJECT_ROOT/android/app/build.gradle"

################################################################################
# Helper Functions
################################################################################

print_header() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║           NativeBridge Release Automation                 ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_step() {
    echo -e "\n${BLUE}▶ $1${NC}\n"
}

show_usage() {
    cat << EOF
Usage: $0 <version> [options]

Arguments:
  version       Version number (e.g., 1.0.0, 2.0.0)

Options:
  --dry-run              Show what would happen without making changes
  --force                Skip confirmations
  --beta                 Build BOTH production and beta variants
  --start-session        Start NativeBridge session after app upload (production)
  --device-id <id>       Production device ID (default: 67a642531a4aa535498192f8)
  --beta-device-id <id>  Beta device ID (requires --beta, default: same as production)
  --session-validity <s> Session duration in seconds, 30-300 (default: 120)
  -h, --help             Show this help message

Examples:
  # Production only
  $0 1.0.0
  $0 1.2.3 --start-session
  $0 1.0.0 --start-session --device-id abc123 --session-validity 180

  # Production + Beta (both uploaded, both sessions)
  $0 1.2.3 --beta --start-session
  $0 1.2.3 --beta --start-session --device-id prod123 --beta-device-id beta456
  $0 1.2.3 --beta --start-session --session-validity 240

  # Other
  $0 1.0.1 --dry-run
  $0 1.0.0 --force

Beta Build Behavior:
  When --beta is specified:
  - Production: Version X.Y.Z uploaded to NativeBridge
  - Beta: Version X.Y.Z-beta uploaded to NativeBridge
  - Both get separate magic links
  - Sessions start on different devices (if --start-session)
  - Both sessions notified in Slack (if configured)

NativeBridge Session Options:
  --start-session              Enable automatic session start after upload
  --device-id <device-id>      Production device ID (requires --start-session)
  --beta-device-id <device-id> Beta device ID (requires --beta and --start-session)
  --session-validity <seconds> Session duration for both builds (30-300 seconds)

EOF
}

################################################################################
# Validation Functions
################################################################################

check_dependencies() {
    print_step "Checking dependencies"

    local missing_deps=()

    # Check git
    if ! command -v git &> /dev/null; then
        missing_deps+=("git")
    fi

    # Check node
    if ! command -v node &> /dev/null; then
        missing_deps+=("node")
    fi

    # Check jq (for JSON manipulation)
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed. Will use sed for JSON manipulation."
        print_info "Install jq for better reliability: brew install jq (macOS) or apt-get install jq (Linux)"
        USE_JQ=false
    else
        USE_JQ=true
        print_success "jq is available"
    fi

    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing required dependencies: ${missing_deps[*]}"
        exit 1
    fi

    print_success "All required dependencies found"
}

validate_version() {
    local version=$1

    # Regex for semantic versioning
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.]+)?$ ]]; then
        print_error "Invalid version format: $version"
        print_info "Version must follow semantic versioning (e.g., 1.0.0, 1.2.3-beta)"
        return 1
    fi

    return 0
}

check_git_status() {
    print_step "Checking git status"

    # Check if in git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi

    # Check for uncommitted changes
    if [[ -n $(git status -s) ]]; then
        print_warning "You have uncommitted changes:"
        git status -s

        if [[ "$FORCE" != true ]]; then
            echo ""
            read -p "Continue anyway? (y/n) " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                print_error "Aborted by user"
                exit 1
            fi
        fi
    else
        print_success "Working directory is clean"
    fi

    # Check current branch
    CURRENT_BRANCH=$(git branch --show-current)
    print_info "Current branch: $CURRENT_BRANCH"

    if [[ "$CURRENT_BRANCH" != "main" && "$CURRENT_BRANCH" != "master" ]]; then
        print_warning "Not on main/master branch"

        if [[ "$FORCE" != true ]]; then
            read -p "Continue anyway? (y/n) " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                print_error "Aborted by user"
                exit 1
            fi
        fi
    fi
}

check_tag_exists() {
    local version=$1
    local tag="v$version"

    if git rev-parse "$tag" >/dev/null 2>&1; then
        print_error "Tag $tag already exists"
        print_info "Delete it with: git tag -d $tag && git push origin :refs/tags/$tag"
        exit 1
    fi
}

validate_session_validity() {
    local validity=$1

    # Check if it's a number
    if ! [[ "$validity" =~ ^[0-9]+$ ]]; then
        print_error "Session validity must be a number: $validity"
        return 1
    fi

    # Check range
    if [ "$validity" -lt 30 ]; then
        print_warning "Session validity too low ($validity seconds), using minimum: 30 seconds"
        SESSION_VALIDITY=30
    elif [ "$validity" -gt 300 ]; then
        print_warning "Session validity too high ($validity seconds), using maximum: 300 seconds"
        SESSION_VALIDITY=300
    else
        SESSION_VALIDITY=$validity
    fi

    return 0
}

################################################################################
# Version Bump Functions
################################################################################

update_package_json() {
    local version=$1

    print_step "Updating package.json"

    if [[ "$USE_JQ" == true ]]; then
        # Use jq for clean JSON manipulation
        local temp_file=$(mktemp)
        jq --arg version "$version" '.version = $version' "$PACKAGE_JSON" > "$temp_file"
        mv "$temp_file" "$PACKAGE_JSON"
        print_success "Updated package.json to version $version"
    else
        # Fallback to sed (less robust but works without jq)
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            sed -i '' "s/\"version\": \".*\"/\"version\": \"$version\"/" "$PACKAGE_JSON"
        else
            # Linux
            sed -i "s/\"version\": \".*\"/\"version\": \"$version\"/" "$PACKAGE_JSON"
        fi
        print_success "Updated package.json to version $version"
    fi

    # Show the change
    print_info "New version in package.json:"
    grep '"version"' "$PACKAGE_JSON"
}

update_android_version() {
    local version=$1

    print_step "Updating Android build.gradle"

    # Generate versionCode from version number
    # 1.2.3 -> 10203
    # 1.0.0 -> 10000
    local version_code=$(echo "$version" | sed 's/-.*//' | awk -F. '{printf "%d%02d%02d", $1, $2, $3}')

    print_info "Version: $version"
    print_info "Version Code: $version_code"

    # Update versionCode
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/versionCode [0-9]*/versionCode $version_code/" "$ANDROID_BUILD_GRADLE"
        sed -i '' "s/versionName \".*\"/versionName \"$version\"/" "$ANDROID_BUILD_GRADLE"
    else
        sed -i "s/versionCode [0-9]*/versionCode $version_code/" "$ANDROID_BUILD_GRADLE"
        sed -i "s/versionName \".*\"/versionName \"$version\"/" "$ANDROID_BUILD_GRADLE"
    fi

    print_success "Updated Android build.gradle"
    print_info "versionCode: $version_code"
    print_info "versionName: $version"
}

################################################################################
# Git Functions
################################################################################

commit_version_changes() {
    local version=$1

    print_step "Committing version changes"

    cd "$PROJECT_ROOT"

    # Add changed files
    git add "$PACKAGE_JSON" "$ANDROID_BUILD_GRADLE"

    # Check if there are changes to commit
    if git diff --cached --quiet; then
        print_error "No changes to commit!"
        print_warning "The version $version is already set in package.json and build.gradle"
        print_info "Current version in package.json: $(grep '"version"' "$PACKAGE_JSON" | sed 's/.*"version": "\(.*\)".*/\1/')"
        echo ""
        print_info "Try a different version, for example:"
        print_info "  ./scripts/release.sh 1.0.1"
        print_info "  ./scripts/release.sh 1.1.0"
        print_info "  ./scripts/release.sh 2.0.0"
        exit 1
    fi

    # Commit
    git commit -m "chore: bump version to $version

- Updated package.json version
- Updated Android versionCode and versionName
- Preparing for release v$version"

    print_success "Changes committed"
}

create_and_push_tag() {
    local version=$1
    local tag="v$version"

    print_step "Creating git tag"

    cd "$PROJECT_ROOT"

    # Build tag message
    local tag_message="Release $tag

Version: $version
Date: $(date '+%Y-%m-%d %H:%M:%S')
Branch: $(git branch --show-current)
Commit: $(git rev-parse --short HEAD)

This release includes:"

    # Add production build info
    if [[ "$BUILD_BETA" == true ]]; then
        tag_message="${tag_message}
- Production APK: NativeBridge-v${version}.apk
- Beta APK: NativeBridge-v${version}-beta.apk
- Production iOS: NativeBridge-iOS-v${version}.app.zip
- Beta iOS: NativeBridge-iOS-v${version}-beta.app.zip"
    else
        tag_message="${tag_message}
- Android APK: NativeBridge-v${version}.apk
- iOS .app: NativeBridge-iOS-v${version}.app.zip"
    fi

    tag_message="${tag_message}

Automated build via GitHub Actions CI/CD pipeline."

    # Add beta build marker if enabled
    if [[ "$BUILD_BETA" == true ]]; then
        tag_message="${tag_message}

Beta Build Configuration:
- Beta builds enabled: YES
- Beta version suffix: -beta
- Beta device ID: ${BETA_DEVICE_ID}

[NB_BETA_ENABLED]
[NB_BETA_DEVICE_ID:${BETA_DEVICE_ID}]"
    fi

    # Add NativeBridge session info if enabled
    if [[ "$START_SESSION" == true ]]; then
        tag_message="${tag_message}

NativeBridge Session Configuration:
- Auto-start session: enabled
- Production device ID: ${DEVICE_ID}
- Session validity: ${SESSION_VALIDITY} seconds

[NB_SESSION_ENABLED]
[NB_DEVICE_ID:${DEVICE_ID}]
[NB_SESSION_VALIDITY:${SESSION_VALIDITY}]"
    fi

    # Create annotated tag
    git tag -a "$tag" -m "$tag_message"

    print_success "Tag $tag created"

    if [[ "$BUILD_BETA" == true ]]; then
        print_info "Beta build enabled:"
        print_info "  Production version: $version"
        print_info "  Beta version: $version-beta"
        if [[ "$START_SESSION" == true ]]; then
            print_info "  Both variants will have separate sessions"
        fi
    fi

    if [[ "$START_SESSION" == true ]]; then
        print_info "Session will auto-start with:"
        print_info "  Production Device ID: $DEVICE_ID"
        if [[ "$BUILD_BETA" == true ]]; then
            print_info "  Beta Device ID: $BETA_DEVICE_ID"
        fi
        print_info "  Validity: $SESSION_VALIDITY seconds"
    fi

    # Push commit and tag
    print_step "Pushing to remote"

    git push origin "$CURRENT_BRANCH"
    print_success "Pushed commit to $CURRENT_BRANCH"

    git push origin "$tag"
    print_success "Pushed tag $tag"

    if [[ "$BUILD_BETA" == true ]]; then
        print_info "GitHub Actions will now build BOTH production and beta variants"
    else
        print_info "GitHub Actions will now build the release"
    fi
}

################################################################################
# Main Release Process
################################################################################

perform_release() {
    local version=$1

    print_header

    echo -e "${GREEN}Release Version: $version${NC}"
    echo -e "${GREEN}Tag: v$version${NC}"
    echo ""

    # Validate version format
    if ! validate_version "$version"; then
        exit 1
    fi

    # Check dependencies
    check_dependencies

    # Check git status
    check_git_status

    # Check if tag already exists
    check_tag_exists "$version"

    # Show summary and confirm
    if [[ "$DRY_RUN" != true && "$FORCE" != true ]]; then
        echo -e "${YELLOW}═══════════════════════════════════════════════════════════${NC}"
        echo -e "${YELLOW}Release Summary:${NC}"
        echo -e "${YELLOW}  Version:      $version${NC}"
        echo -e "${YELLOW}  Tag:          v$version${NC}"
        echo -e "${YELLOW}  Branch:       $CURRENT_BRANCH${NC}"
        if [[ "$BUILD_BETA" == true ]]; then
            echo -e "${YELLOW}  Beta Build:   enabled${NC}"
            echo -e "${YELLOW}  Beta Version: $version-beta${NC}"
        fi
        if [[ "$START_SESSION" == true ]]; then
            echo -e "${YELLOW}  Session:      enabled${NC}"
            echo -e "${YELLOW}  Prod Device:  $DEVICE_ID${NC}"
            if [[ "$BUILD_BETA" == true ]]; then
                echo -e "${YELLOW}  Beta Device:  $BETA_DEVICE_ID${NC}"
            fi
            echo -e "${YELLOW}  Validity:     $SESSION_VALIDITY seconds${NC}"
        fi
        echo -e "${YELLOW}═══════════════════════════════════════════════════════════${NC}"
        echo ""
        read -p "Continue with release? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Release cancelled by user"
            exit 1
        fi
    fi

    if [[ "$DRY_RUN" == true ]]; then
        print_warning "DRY RUN MODE - No changes will be made"
        echo ""
        print_info "Would update package.json to version $version"
        print_info "Would update Android build.gradle"
        print_info "Would commit changes"
        print_info "Would create and push tag v$version"
        if [[ "$BUILD_BETA" == true ]]; then
            print_info "Would build beta variant:"
            print_info "  - Production version: $version"
            print_info "  - Beta version: $version-beta"
            print_info "  - Both uploaded to NativeBridge"
        fi
        if [[ "$START_SESSION" == true ]]; then
            print_info "Would enable NativeBridge session:"
            print_info "  - Production Device ID: $DEVICE_ID"
            if [[ "$BUILD_BETA" == true ]]; then
                print_info "  - Beta Device ID: $BETA_DEVICE_ID"
            fi
            print_info "  - Session validity: $SESSION_VALIDITY seconds"
        fi
        print_info "Would trigger CI/CD pipeline"
        echo ""
        print_success "Dry run completed"
        exit 0
    fi

    # Update versions
    update_package_json "$version"
    update_android_version "$version"

    # Commit changes
    commit_version_changes "$version"

    # Create and push tag
    create_and_push_tag "$version"

    # Success message
    echo ""
    print_success "═══════════════════════════════════════════════════════════"
    print_success "Release $version completed successfully! 🎉"
    print_success "═══════════════════════════════════════════════════════════"
    echo ""
    print_info "Next steps:"
    print_info "1. Monitor the build at: https://github.com/$(git config --get remote.origin.url | sed 's/.*://; s/.git$//')/actions"
    print_info "2. Download artifacts from: https://github.com/$(git config --get remote.origin.url | sed 's/.*://; s/.git$//')/releases/tag/v$version"
    print_info "3. Test the built APK and .app"
    echo ""
}

################################################################################
# Entry Point
################################################################################

main() {
    # Parse arguments
    VERSION=""
    DRY_RUN=false
    FORCE=false
    BUILD_BETA=false
    START_SESSION=false
    DEVICE_ID="67a642531a4aa535498192f8"  # Default production device ID
    BETA_DEVICE_ID=""  # Default: same as production if not specified
    SESSION_VALIDITY=120  # Default 2 minutes

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --force)
                FORCE=true
                shift
                ;;
            --beta)
                BUILD_BETA=true
                shift
                ;;
            --start-session)
                START_SESSION=true
                shift
                ;;
            --device-id)
                if [[ -z "$2" || "$2" == -* ]]; then
                    print_error "--device-id requires a value"
                    exit 1
                fi
                DEVICE_ID="$2"
                shift 2
                ;;
            --beta-device-id)
                if [[ -z "$2" || "$2" == -* ]]; then
                    print_error "--beta-device-id requires a value"
                    exit 1
                fi
                BETA_DEVICE_ID="$2"
                shift 2
                ;;
            --session-validity)
                if [[ -z "$2" || "$2" == -* ]]; then
                    print_error "--session-validity requires a value"
                    exit 1
                fi
                if ! validate_session_validity "$2"; then
                    exit 1
                fi
                shift 2
                ;;
            -*)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
            *)
                if [[ -z "$VERSION" ]]; then
                    VERSION="$1"
                else
                    print_error "Multiple versions specified"
                    show_usage
                    exit 1
                fi
                shift
                ;;
        esac
    done

    # Set beta device ID default (same as production if not specified)
    if [[ -z "$BETA_DEVICE_ID" ]]; then
        BETA_DEVICE_ID="$DEVICE_ID"
    fi

    # Validate beta options
    if [[ "$BETA_DEVICE_ID" != "$DEVICE_ID" ]] && [[ "$BUILD_BETA" != true ]]; then
        print_warning "--beta-device-id requires --beta to be set"
        print_info "Beta device ID will be ignored"
        BETA_DEVICE_ID="$DEVICE_ID"
    fi

    # Validate session options
    if [[ "$DEVICE_ID" != "67a642531a4aa535498192f8" || "$SESSION_VALIDITY" != "120" ]] && [[ "$START_SESSION" != true ]]; then
        print_warning "--device-id and --session-validity require --start-session to be set"
        print_info "Session options will be ignored"
        DEVICE_ID="67a642531a4aa535498192f8"
        SESSION_VALIDITY=120
        BETA_DEVICE_ID="$DEVICE_ID"
    fi

    # Check if version was provided
    if [[ -z "$VERSION" ]]; then
        print_error "Version number required"
        echo ""
        show_usage
        exit 1
    fi

    # Perform the release
    perform_release "$VERSION"
}

# Run main function
main "$@"
