# NativeBridge API Integration for CI/CD

A comprehensive guide for integrating NativeBridge Application Upload API into any CI/CD platform (GitHub Actions, Jenkins, GitLab CI, Circle CI, Bitrise, etc.).

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [API Overview](#api-overview)
3. [Session API](#session-api)
4. [GitHub Actions](#github-actions)
5. [GitLab CI/CD](#gitlab-cicd)
6. [Jenkins](#jenkins)
7. [Circle CI](#circleci)
8. [Bitrise](#bitrise)
9. [Azure Pipelines](#azure-pipelines)
10. [Travis CI](#travis-ci)
11. [Generic Shell Script](#generic-shell-script)
12. [Best Practices](#best-practices)
13. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### 1. Get Your API Key

1. Log in to [NativeBridge](https://nativebridge.io)
2. Go to the **API Keys** section: https://nativebridge.io/dashboard/api-keys
3. Click **Generate New API Key** or **Create API Key**
4. Copy and **keep it secure** - never commit to your repository!

### 2. What is NativeBridge?

**NativeBridge** is a cloud platform that lets you **run Android apps instantly in your browser** without any installation:

- ☁️ **Cloud Emulators** - Test on virtual Android devices instantly
- 📱 **Real Physical Devices** - Run on actual Android hardware in the cloud
- 🚀 **One-Click Launch** - No setup, no downloads, just click and test
- 🌐 **Universal Access** - Works on any platform (Windows, Mac, Linux, mobile)
- 🔗 **Shareable Magic Links** - Send links to anyone for instant testing

The API integration automatically uploads your APK and generates a **magic link** that lets anyone run your app in the cloud with a single click.

### 3. Understanding the API

**Base URL:** `https://api.nativebridge.io`

**Endpoint:** `POST /v1/application`

**Authentication:** `X-Api-Key` header

**Content-Type:** `multipart/form-data`

**Rate Limit:** 10 requests per minute

---

## API Overview

### Required Setup

All CI/CD integrations need to:

1. **Add API key to secrets/environment variables**
   - Secret name: `NATIVEBRIDGE_API_KEY`
   - Value: Your API key (e.g., `sk_live_xxxxx`)

2. **Upload APK after build**
   - Use `curl` or HTTP client
   - Send `multipart/form-data` request
   - Include `X-Api-Key` header

3. **Parse response** (optional but recommended)
   - Extract magic link
   - Save for later steps
   - Add to build artifacts/logs

### Default Parameters

For most use cases, use these parameters:

| Parameter | Value | Description |
|-----------|-------|-------------|
| `file` | APK file path | Your built APK |
| `apkUrl` | `None` | Not using URL upload |
| `accessType` | `public` | Public access (or `private` for restricted) |
| `allowedUsers` | `None` | Not needed for public access |
| `versionAction` | `create_new_version` | Auto-increment versions |
| `sendNotification` | `true` | Email notifications enabled |
| `notificationEmails` | `None` | Use uploader's email (default) |

---

## Session API

### Overview

After uploading your app, you can automatically start a test session on a NativeBridge device using the Session API.

**Endpoint:** `POST /v1/device/session`

**Authentication:** `X-Api-Key` header

**Content-Type:** `application/json`

### Request Parameters

| Parameter | Type | Description | Required | Valid Values |
|-----------|------|-------------|----------|--------------|
| `deviceType` | string | Type of device | Yes | `"android"` or `"ios"` |
| `deviceId` | string | NativeBridge device ID | Yes | Valid device ID |
| `appId` | string | App ID from upload response | Yes | e.g., `"HgWp"` |
| `region` | string | Server region | Yes | `"ind"`, `"us"`, etc. |
| `executionValidity` | number | Session duration in seconds | Yes | 30-300 |

### Example Request

```bash
curl -X POST https://api.nativebridge.io/v1/device/session \
  -H "accept: application/json" \
  -H "X-Api-Key: Nb-c3uo.a1233f7f-b484-4962-a531-3f7a13bab4ae" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceType": "android",
    "deviceId": "67a642531a4aa535498192f8",
    "appId": "HgWp",
    "region": "ind",
    "executionValidity": 120
  }'
```

### Example Response

```json
{
  "data": {
    "sessionId": "OXDQ",
    "message": "Session created successfully",
    "sessionUrl": "https://nativebridge.io/session/OXDQ"
  }
}
```

### Session Validity Constraints

- **Minimum:** 30 seconds
- **Maximum:** 300 seconds (5 minutes)
- **Recommended:** 120 seconds (2 minutes) for standard testing

Values outside this range will be rejected by the API.

### Complete Upload + Session Workflow

Here's a complete example that uploads an app and starts a session:

```bash
#!/bin/bash
set -e

API_KEY="${NATIVEBRIDGE_API_KEY}"
APK_PATH="path/to/app-release.apk"
DEVICE_ID="67a642531a4aa535498192f8"
SESSION_VALIDITY=120

# Step 1: Upload APK
echo "📤 Uploading APK to NativeBridge..."

UPLOAD_RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
  -H "X-Api-Key: $API_KEY" \
  -F "file=@$APK_PATH" \
  -F "accessType=public" \
  -F "versionAction=create_new_version" \
  -F "sendNotification=true" \
  -w "\n%{http_code}" \
  -s)

HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -n1)
BODY=$(echo "$UPLOAD_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -ne 200 ]; then
  echo "❌ Upload failed (HTTP $HTTP_CODE)"
  echo "$BODY"
  exit 1
fi

# Extract App ID
APP_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//')
MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')

echo "✅ Upload successful!"
echo "   App ID: $APP_ID"
echo "   Magic Link: $MAGIC_LINK"

# Step 2: Start Session
echo ""
echo "🚀 Starting NativeBridge session..."

SESSION_RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/device/session \
  -H "accept: application/json" \
  -H "X-Api-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{
    \"deviceType\": \"android\",
    \"deviceId\": \"$DEVICE_ID\",
    \"appId\": \"$APP_ID\",
    \"region\": \"ind\",
    \"executionValidity\": $SESSION_VALIDITY
  }" \
  -w "\n%{http_code}" \
  -s)

HTTP_CODE=$(echo "$SESSION_RESPONSE" | tail -n1)
BODY=$(echo "$SESSION_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
  SESSION_ID=$(echo "$BODY" | grep -o '"sessionId":"[^"]*"' | sed 's/"sessionId":"//;s/"//')
  SESSION_URL=$(echo "$BODY" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')

  echo "✅ Session started successfully!"
  echo "   Session ID: $SESSION_ID"
  echo "   Session URL: $SESSION_URL"
  echo "   Validity: $SESSION_VALIDITY seconds"
else
  echo "⚠️  Session start failed (HTTP $HTTP_CODE)"
  echo "$BODY"
  echo "   App is still available via magic link"
fi

echo ""
echo "🎉 Deployment complete!"
```

### Validation Logic

Always validate session validity before making the API call:

```bash
validate_session_validity() {
  local validity=$1

  if [ "$validity" -lt 30 ]; then
    echo "⚠️ Session validity too low ($validity), using minimum: 30"
    validity=30
  elif [ "$validity" -gt 300 ]; then
    echo "⚠️ Session validity too high ($validity), using maximum: 300"
    validity=300
  fi

  echo "$validity"
}

# Usage
SESSION_VALIDITY=$(validate_session_validity $SESSION_VALIDITY)
```

### Error Handling

Handle session start errors gracefully:

```bash
if [ "$HTTP_CODE" -eq 200 ]; then
  # Success - session started
  echo "✅ Session active"
elif [ "$HTTP_CODE" -eq 401 ]; then
  echo "❌ Invalid API key"
  exit 1
elif [ "$HTTP_CODE" -eq 404 ]; then
  echo "❌ Device or app not found"
  exit 1
elif [ "$HTTP_CODE" -eq 400 ]; then
  echo "❌ Invalid parameters (check validity range)"
  exit 1
else
  echo "⚠️ Session start failed - app still available via magic link"
  # Don't exit - app upload was successful
fi
```

### Common Use Cases

#### 1. Automated Testing After Upload
```bash
# Upload app → Start session → Run automated tests
# The session URL can be used for Appium or other testing frameworks
```

#### 2. QA Team Notification
```bash
# After starting session, send notification with session URL
# QA team can immediately start testing
```

#### 3. Quick Smoke Tests
```bash
# Start short 30-second session for quick verification
executionValidity=30
```

#### 4. Extended Manual Testing
```bash
# Start 5-minute session for thorough manual testing
executionValidity=300
```

### Regional Considerations

Available regions:
- `"ind"` - India
- `"us"` - United States
- Additional regions may be available - check NativeBridge dashboard

Choose the region closest to your testing team for best performance.

---

## GitHub Actions

### Step 1: Add Secret

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `NATIVEBRIDGE_API_KEY`
4. Value: Your API key
5. Click **Add secret**

### Step 2: Add Upload Step

Add to your workflow file (`.github/workflows/deploy.yml`):

```yaml
name: Build and Deploy

on:
  push:
    tags:
      - 'v*'

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build APK
        run: |
          cd android
          ./gradlew assembleRelease

      - name: Upload to NativeBridge
        env:
          NATIVEBRIDGE_API_KEY: ${{ secrets.NATIVEBRIDGE_API_KEY }}
        run: |
          APK_PATH="android/app/build/outputs/apk/release/app-release.apk"

          # Upload to NativeBridge
          RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
            -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
            -F "file=@$APK_PATH" \
            -F "accessType=public" \
            -F "versionAction=create_new_version" \
            -F "sendNotification=true" \
            -w "\n%{http_code}" \
            -s)

          # Parse response
          HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
          BODY=$(echo "$RESPONSE" | sed '$d')

          if [ "$HTTP_CODE" -eq 200 ]; then
            echo "✅ Successfully uploaded to NativeBridge!"
            MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')
            echo "🔗 Magic Link: $MAGIC_LINK"
            echo "MAGIC_LINK=$MAGIC_LINK" >> $GITHUB_ENV
          else
            echo "❌ Upload failed (HTTP $HTTP_CODE)"
            echo "$BODY"
            exit 1
          fi

      - name: Add to Summary
        if: success()
        run: |
          echo "### 📱 NativeBridge Upload ✅" >> $GITHUB_STEP_SUMMARY
          echo "**Magic Link:** ${{ env.MAGIC_LINK }}" >> $GITHUB_STEP_SUMMARY
```

### Advanced: Upload with Auto-Session Start

Complete example with app upload + session start + Slack notification:

```yaml
- name: Upload to NativeBridge and Start Session
  env:
    NATIVEBRIDGE_API_KEY: ${{ secrets.NATIVEBRIDGE_API_KEY }}
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
  run: |
    APK_PATH="android/app/build/outputs/apk/release/app-release.apk"
    DEVICE_ID="67a642531a4aa535498192f8"
    SESSION_VALIDITY=120

    # Upload APK
    echo "📤 Uploading to NativeBridge..."
    UPLOAD_RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
      -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
      -F "file=@$APK_PATH" \
      -F "accessType=public" \
      -F "versionAction=create_new_version" \
      -F "sendNotification=true" \
      -w "\n%{http_code}" \
      -s)

    HTTP_CODE=$(echo "$UPLOAD_RESPONSE" | tail -n1)
    BODY=$(echo "$UPLOAD_RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -ne 200 ]; then
      echo "❌ Upload failed"
      exit 1
    fi

    # Extract app details
    APP_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//')
    MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')

    echo "✅ Upload successful! App ID: $APP_ID"
    echo "APP_ID=$APP_ID" >> $GITHUB_ENV
    echo "MAGIC_LINK=$MAGIC_LINK" >> $GITHUB_ENV

    # Start session
    echo "🚀 Starting session..."
    SESSION_RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/device/session \
      -H "accept: application/json" \
      -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
      -H "Content-Type: application/json" \
      -d "{
        \"deviceType\": \"android\",
        \"deviceId\": \"$DEVICE_ID\",
        \"appId\": \"$APP_ID\",
        \"region\": \"ind\",
        \"executionValidity\": $SESSION_VALIDITY
      }" \
      -w "\n%{http_code}" \
      -s)

    HTTP_CODE=$(echo "$SESSION_RESPONSE" | tail -n1)
    BODY=$(echo "$SESSION_RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
      SESSION_URL=$(echo "$BODY" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')
      echo "✅ Session started: $SESSION_URL"
      echo "SESSION_URL=$SESSION_URL" >> $GITHUB_ENV

      # Send Slack notification
      if [ -n "$SLACK_WEBHOOK_URL" ]; then
        curl -X POST "$SLACK_WEBHOOK_URL" \
          -H "Content-Type: application/json" \
          -d "{
            \"text\": \"🚀 NativeBridge session ready!\",
            \"blocks\": [{
              \"type\": \"section\",
              \"text\": {
                \"type\": \"mrkdwn\",
                \"text\": \"*Session URL:* <$SESSION_URL|Launch Session>\"
              }
            }]
          }"
      fi
    fi

- name: Add to Release Notes
  if: success() && env.SESSION_URL != ''
  run: |
    echo "### 🎮 Test Session" >> $GITHUB_STEP_SUMMARY
    echo "**Session URL:** [${{ env.SESSION_URL }}](${{ env.SESSION_URL }})" >> $GITHUB_STEP_SUMMARY
```

### Advanced: Private Access with Allowed Users

```yaml
- name: Upload to NativeBridge (Private)
  run: |
    curl -X POST https://api.nativebridge.io/v1/application \
      -H "X-Api-Key: ${{ secrets.NATIVEBRIDGE_API_KEY }}" \
      -F "file=@$APK_PATH" \
      -F "accessType=private" \
      -F "allowedUsers=dev@company.com" \
      -F "allowedUsers=qa@company.com" \
      -F "sendNotification=true" \
      -F "notificationEmails=qa-team@company.com"
```

---

## GitLab CI/CD

### Step 1: Add Variable

1. Go to **Settings** → **CI/CD** → **Variables**
2. Click **Add variable**
3. Key: `NATIVEBRIDGE_API_KEY`
4. Value: Your API key
5. **Check:** Protected, Masked
6. Click **Add variable**

### Step 2: Add to `.gitlab-ci.yml`

```yaml
stages:
  - build
  - deploy

variables:
  ANDROID_HOME: "/opt/android-sdk"

build:
  stage: build
  image: gradle:jdk17
  script:
    - cd android
    - ./gradlew assembleRelease
  artifacts:
    paths:
      - android/app/build/outputs/apk/release/app-release.apk
    expire_in: 1 week

deploy_nativebridge:
  stage: deploy
  image: curlimages/curl:latest
  script:
    - |
      RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
        -H "X-Api-Key: ${NATIVEBRIDGE_API_KEY}" \
        -F "file=@android/app/build/outputs/apk/release/app-release.apk" \
        -F "accessType=public" \
        -F "versionAction=create_new_version" \
        -F "sendNotification=true" \
        -w "\n%{http_code}" \
        -s)

      HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
      BODY=$(echo "$RESPONSE" | sed '$d')

      if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ Successfully uploaded to NativeBridge!"
        echo "$BODY" | grep -o '"magicLink":"[^"]*"'
      else
        echo "❌ Upload failed (HTTP $HTTP_CODE)"
        echo "$BODY"
        exit 1
      fi
  dependencies:
    - build
  only:
    - tags
    - main
```

---

## Jenkins

### Step 1: Add Credential

1. Go to **Manage Jenkins** → **Credentials**
2. Click your domain → **Add Credentials**
3. Kind: **Secret text**
4. Secret: Your API key
5. ID: `nativebridge-api-key`
6. Description: NativeBridge API Key
7. Click **OK**

### Step 2: Add to Jenkinsfile

```groovy
pipeline {
    agent any

    environment {
        NATIVEBRIDGE_API_KEY = credentials('nativebridge-api-key')
    }

    stages {
        stage('Build') {
            steps {
                sh '''
                    cd android
                    chmod +x gradlew
                    ./gradlew assembleRelease
                '''
            }
        }

        stage('Upload to NativeBridge') {
            steps {
                script {
                    def apkPath = 'android/app/build/outputs/apk/release/app-release.apk'

                    sh """
                        RESPONSE=\$(curl -X POST https://api.nativebridge.io/v1/application \\
                            -H "X-Api-Key: ${NATIVEBRIDGE_API_KEY}" \\
                            -F "file=@${apkPath}" \\
                            -F "accessType=public" \\
                            -F "versionAction=create_new_version" \\
                            -F "sendNotification=true" \\
                            -w "\\n%{http_code}" \\
                            -s)

                        HTTP_CODE=\$(echo "\$RESPONSE" | tail -n1)
                        BODY=\$(echo "\$RESPONSE" | sed '\$d')

                        if [ "\$HTTP_CODE" -eq 200 ]; then
                            echo "✅ Successfully uploaded to NativeBridge!"
                            echo "\$BODY" | grep -o '"magicLink":"[^"]*"'
                        else
                            echo "❌ Upload failed (HTTP \$HTTP_CODE)"
                            echo "\$BODY"
                            exit 1
                        fi
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Build and upload successful!'
        }
        failure {
            echo 'Build or upload failed!'
        }
    }
}
```

---

## Circle CI

### Step 1: Add Environment Variable

1. Go to **Project Settings** → **Environment Variables**
2. Click **Add Environment Variable**
3. Name: `NATIVEBRIDGE_API_KEY`
4. Value: Your API key
5. Click **Add Variable**

### Step 2: Add to `.circleci/config.yml`

```yaml
version: 2.1

jobs:
  build-and-deploy:
    docker:
      - image: cimg/android:2023.09

    steps:
      - checkout

      - run:
          name: Build APK
          command: |
            cd android
            chmod +x gradlew
            ./gradlew assembleRelease

      - run:
          name: Upload to NativeBridge
          command: |
            APK_PATH="android/app/build/outputs/apk/release/app-release.apk"

            RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
              -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
              -F "file=@$APK_PATH" \
              -F "accessType=public" \
              -F "versionAction=create_new_version" \
              -F "sendNotification=true" \
              -w "\n%{http_code}" \
              -s)

            HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
            BODY=$(echo "$RESPONSE" | sed '$d')

            if [ "$HTTP_CODE" -eq 200 ]; then
              echo "✅ Successfully uploaded to NativeBridge!"
              echo "$BODY" | grep -o '"magicLink":"[^"]*"'
            else
              echo "❌ Upload failed (HTTP $HTTP_CODE)"
              echo "$BODY"
              exit 1
            fi

      - store_artifacts:
          path: android/app/build/outputs/apk/release/app-release.apk
          destination: app-release.apk

workflows:
  build-deploy:
    jobs:
      - build-and-deploy:
          filters:
            tags:
              only: /^v.*/
```

---

## Bitrise

### Step 1: Add Secret

1. Go to **Workflow** → **Secrets**
2. Click **Add new**
3. Key: `NATIVEBRIDGE_API_KEY`
4. Value: Your API key
5. Check **Protected**
6. Click **Save**

### Step 2: Add Script Step

Add to your `bitrise.yml`:

```yaml
workflows:
  deploy:
    steps:
      - activate-ssh-key: {}
      - git-clone: {}

      - gradle-runner@2:
          inputs:
            - gradle_task: assembleRelease
            - gradle_file: android/build.gradle

      - script@1:
          title: Upload to NativeBridge
          inputs:
            - content: |
                #!/bin/bash
                set -e

                APK_PATH="$BITRISE_APK_PATH"

                echo "📤 Uploading to NativeBridge..."

                RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
                  -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
                  -F "file=@$APK_PATH" \
                  -F "accessType=public" \
                  -F "versionAction=create_new_version" \
                  -F "sendNotification=true" \
                  -w "\n%{http_code}" \
                  -s)

                HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
                BODY=$(echo "$RESPONSE" | sed '$d')

                if [ "$HTTP_CODE" -eq 200 ]; then
                  echo "✅ Successfully uploaded to NativeBridge!"
                  MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')
                  echo "🔗 Magic Link: $MAGIC_LINK"
                  envman add --key NATIVEBRIDGE_LINK --value "$MAGIC_LINK"
                else
                  echo "❌ Upload failed (HTTP $HTTP_CODE)"
                  echo "$BODY"
                  exit 1
                fi

      - deploy-to-bitrise-io: {}
```

---

## Azure Pipelines

### Step 1: Add Variable

1. Go to **Pipelines** → **Library** → **Variable groups**
2. Create new variable group or select existing
3. Add variable:
   - Name: `NATIVEBRIDGE_API_KEY`
   - Value: Your API key
   - **Check:** Keep this value secret
4. Save

### Step 2: Add to `azure-pipelines.yml`

```yaml
trigger:
  tags:
    include:
      - v*

pool:
  vmImage: 'ubuntu-latest'

variables:
  - group: nativebridge-vars  # Your variable group name

steps:
  - task: Gradle@2
    displayName: 'Build APK'
    inputs:
      workingDirectory: 'android'
      gradleWrapperFile: 'android/gradlew'
      tasks: 'assembleRelease'
      publishJUnitResults: false

  - bash: |
      APK_PATH="android/app/build/outputs/apk/release/app-release.apk"

      echo "📤 Uploading to NativeBridge..."

      RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
        -H "X-Api-Key: $(NATIVEBRIDGE_API_KEY)" \
        -F "file=@$APK_PATH" \
        -F "accessType=public" \
        -F "versionAction=create_new_version" \
        -F "sendNotification=true" \
        -w "\n%{http_code}" \
        -s)

      HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
      BODY=$(echo "$RESPONSE" | sed '$d')

      if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ Successfully uploaded to NativeBridge!"
        MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')
        echo "🔗 Magic Link: $MAGIC_LINK"
        echo "##vso[task.setvariable variable=MagicLink]$MAGIC_LINK"
      else
        echo "❌ Upload failed (HTTP $HTTP_CODE)"
        echo "$BODY"
        exit 1
      fi
    displayName: 'Upload to NativeBridge'

  - task: PublishBuildArtifacts@1
    displayName: 'Publish APK'
    inputs:
      PathtoPublish: 'android/app/build/outputs/apk/release/app-release.apk'
      ArtifactName: 'apk'
```

---

## Travis CI

### Step 1: Add Encrypted Variable

Using Travis CLI:
```bash
travis encrypt NATIVEBRIDGE_API_KEY="your_api_key" --add
```

Or via web UI:
1. Go to **Settings** → **Environment Variables**
2. Name: `NATIVEBRIDGE_API_KEY`
3. Value: Your API key
4. **Check:** Display value in build log (OFF)
5. Click **Add**

### Step 2: Add to `.travis.yml`

```yaml
language: android

android:
  components:
    - tools
    - platform-tools
    - build-tools-34.0.0
    - android-34

before_install:
  - chmod +x android/gradlew

script:
  - cd android && ./gradlew assembleRelease

after_success:
  - |
    APK_PATH="app/build/outputs/apk/release/app-release.apk"

    echo "📤 Uploading to NativeBridge..."

    RESPONSE=$(curl -X POST https://api.nativebridge.io/v1/application \
      -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
      -F "file=@$APK_PATH" \
      -F "accessType=public" \
      -F "versionAction=create_new_version" \
      -F "sendNotification=true" \
      -w "\n%{http_code}" \
      -s)

    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
      echo "✅ Successfully uploaded to NativeBridge!"
      echo "$BODY" | grep -o '"magicLink":"[^"]*"'
    else
      echo "❌ Upload failed (HTTP $HTTP_CODE)"
      echo "$BODY"
    fi

deploy:
  provider: releases
  skip_cleanup: true
  on:
    tags: true
```

---

## Generic Shell Script

For any CI/CD platform or manual uploads:

```bash
#!/bin/bash
set -e

# Configuration
API_KEY="${NATIVEBRIDGE_API_KEY}"
APK_PATH="path/to/your/app-release.apk"
BASE_URL="https://api.nativebridge.io"

# Validate API key
if [ -z "$API_KEY" ]; then
    echo "❌ Error: NATIVEBRIDGE_API_KEY environment variable not set"
    exit 1
fi

# Validate APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: APK not found at $APK_PATH"
    exit 1
fi

echo "📤 Uploading APK to NativeBridge..."
echo "   APK: $APK_PATH"
echo "   Size: $(du -h "$APK_PATH" | cut -f1)"

# Upload to NativeBridge
RESPONSE=$(curl -X POST "${BASE_URL}/v1/application" \
    -H "X-Api-Key: $API_KEY" \
    -F "file=@$APK_PATH" \
    -F "accessType=public" \
    -F "versionAction=create_new_version" \
    -F "sendNotification=true" \
    -w "\n%{http_code}" \
    -s)

# Parse response
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

# Handle response
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ Successfully uploaded to NativeBridge!"
    echo ""

    # Extract data
    MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')
    VERSIONED_LINK=$(echo "$BODY" | grep -o '"versionedMagicLink":"[^"]*"' | sed 's/"versionedMagicLink":"//;s/"//')
    APP_ID=$(echo "$BODY" | grep -o '"id":"[^"]*"' | sed 's/"id":"//;s/"//')
    VERSION=$(echo "$BODY" | grep -o '"version":"[^"]*"' | sed 's/"version":"//;s/"//')

    echo "📱 App ID: $APP_ID"
    echo "🔢 Version: $VERSION"
    echo "🔗 Magic Link: $MAGIC_LINK"
    echo "🔗 Versioned Link: $VERSIONED_LINK"
    echo ""
    echo "✅ Upload complete!"
elif [ "$HTTP_CODE" -eq 401 ]; then
    echo "❌ Authentication failed (HTTP $HTTP_CODE)"
    echo "   Check your API key is correct"
    exit 1
elif [ "$HTTP_CODE" -eq 429 ]; then
    echo "❌ Rate limit exceeded (HTTP $HTTP_CODE)"
    echo "   Wait 60 seconds before retrying"
    exit 1
else
    echo "❌ Upload failed (HTTP $HTTP_CODE)"
    echo "$BODY"
    exit 1
fi
```

Save as `upload-to-nativebridge.sh` and use:

```bash
# Make executable
chmod +x upload-to-nativebridge.sh

# Run
export NATIVEBRIDGE_API_KEY="your_api_key"
./upload-to-nativebridge.sh
```

---

## Best Practices

### 1. API Key Security

✅ **DO:**
- Store in CI/CD secrets/variables
- Mark as "protected" and "masked"
- Use different keys for staging/production
- Rotate keys periodically

❌ **DON'T:**
- Hardcode in scripts or config files
- Commit to version control
- Share in logs or public places
- Reuse across different projects

### 2. Error Handling

Always check HTTP status codes:

```bash
if [ "$HTTP_CODE" -eq 200 ]; then
    # Success
elif [ "$HTTP_CODE" -eq 401 ]; then
    # Invalid API key
elif [ "$HTTP_CODE" -eq 429 ]; then
    # Rate limit - retry after delay
else
    # Other error - log and exit
fi
```

### 3. Retry Logic

Implement exponential backoff for transient failures:

```bash
MAX_RETRIES=3
RETRY_DELAY=5

for i in $(seq 1 $MAX_RETRIES); do
    RESPONSE=$(curl ...)
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

    if [ "$HTTP_CODE" -eq 200 ]; then
        break
    elif [ "$HTTP_CODE" -eq 429 ]; then
        echo "Rate limited, waiting ${RETRY_DELAY}s..."
        sleep $RETRY_DELAY
        RETRY_DELAY=$((RETRY_DELAY * 2))
    else
        exit 1
    fi
done
```

### 4. Notifications

Configure email notifications for your team:

```bash
-F "sendNotification=true" \
-F "notificationEmails=qa@company.com" \
-F "notificationEmails=dev@company.com"
```

### 5. Private Access

For internal testing:

```bash
-F "accessType=private" \
-F "allowedUsers=internal-team@company.com"
```

### 6. Version Management

Use `create_new_version` for regular releases:

```bash
-F "versionAction=create_new_version"
```

Use `create_new_app` for separate environments:

```bash
# Staging build
-F "versionAction=create_new_app"
```

---

## Troubleshooting

### Common Issues

#### API Key Not Set
```
❌ Error: NATIVEBRIDGE_API_KEY environment variable not set
```
**Solution:** Add API key to CI/CD secrets/variables

#### Invalid API Key (401)
```
❌ Authentication failed (HTTP 401)
{"detail": "Invalid API Key"}
```
**Solution:**
- Verify API key is correct
- Check for extra spaces/newlines
- Regenerate key if needed

#### Rate Limit (429)
```
❌ Rate limit exceeded (HTTP 429)
```
**Solution:**
- Wait 60 seconds before retrying
- Implement exponential backoff
- Contact support for higher limits

#### APK Not Found
```
❌ Error: APK not found at path/to/app.apk
```
**Solution:**
- Verify build step completed successfully
- Check APK path is correct
- Ensure build artifacts are preserved

#### Upload Timeout
**Solution:**
- Increase curl timeout: `--max-time 300`
- Check network connectivity
- Verify APK size is reasonable

---

## Support

- **API Documentation:** [application_upload_api.md](application_upload_api.md)
- **Project CI/CD Guide:** [NATIVEBRIDGE_CICD.md](NATIVEBRIDGE_CICD.md)
- **API Support:** api-support@nativebridge.io
- **Dashboard:** https://dashboard.nativebridge.io

---

**Last Updated:** 2025-11-29
**API Version:** v1
**Base URL:** `https://api.nativebridge.io`
