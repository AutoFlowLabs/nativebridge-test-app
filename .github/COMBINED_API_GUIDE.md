# NativeBridge Combined API Guide

Upload your app AND start a testing session in **one single API call**.

## Table of Contents
1. [Quick Start](#quick-start)
2. [API Reference](#api-reference)
3. [CI/CD Integration](#cicd-integration)
   - [GitHub Actions](#github-actions)
   - [GitLab CI](#gitlab-ci)
   - [Jenkins](#jenkins)
   - [Circle CI](#circle-ci)
   - [Bitrise](#bitrise)
   - [Generic Script](#generic-script)
4. [Response Handling](#response-handling)
5. [Error Handling](#error-handling)

---

## Quick Start

### One Command

```bash
curl -X POST "https://api.nativebridge.io/v1/application/session" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -F "deviceId=YOUR_DEVICE_ID" \
  -F "deviceType=android" \
  -F "file=@./app-release.apk"
```

### What You Get Back

```json
{
  "data": {
    "sessionId": "Qk2d",
    "sessionUrl": "https://nativebridge.io/session/Qk2d",
    "appId": "HgWp",
    "magicLink": "https://nativebridge.io/app/HgWp"
  }
}
```

Click the `sessionUrl` and your app is running in the cloud!

---

## API Reference

### Endpoint

```
POST https://api.nativebridge.io/v1/application/session
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| `X-Api-Key` | Your NativeBridge API key | Yes |

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `deviceId` | string | Yes | - | NativeBridge device ID |
| `deviceType` | string | Yes | - | `android` or `ios` |
| `file` | file | Yes* | - | App file (.apk, .aab, .ipa, .zip) |
| `apkUrl` | string | Yes* | - | OR public URL to download the app |
| `region` | string | No | `ind` | `ind` or `us` |
| `accessType` | string | No | `public` | `public` or `private` |
| `versionAction` | string | No | `create_new_version` | How to handle existing apps |
| `executionValidity` | integer | No | Plan default | Session duration in seconds |
| `allowedUsers` | string[] | No | `[]` | Emails for private access |
| `networkInterception` | boolean | No | `false` | Enable network interception (Android) |

*Either `file` or `apkUrl` is required

### Examples

#### Android (File Upload)

```bash
curl -X POST "https://api.nativebridge.io/v1/application/session" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -F "deviceId=67a642531a4aa535498192f8" \
  -F "deviceType=android" \
  -F "file=@./app-release.apk" \
  -F "region=ind" \
  -F "executionValidity=120"
```

#### Android (URL)

```bash
curl -X POST "https://api.nativebridge.io/v1/application/session" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -F "deviceId=67a642531a4aa535498192f8" \
  -F "deviceType=android" \
  -F "apkUrl=https://example.com/app.apk"
```

#### iOS

```bash
curl -X POST "https://api.nativebridge.io/v1/application/session" \
  -H "X-Api-Key: YOUR_API_KEY" \
  -F "deviceId=67af6a3a908d996442ec49e7" \
  -F "deviceType=ios" \
  -F "file=@./app.ipa"
```

---

## CI/CD Integration

### GitHub Actions

Add to your workflow file:

```yaml
name: Deploy to NativeBridge

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:
    inputs:
      device_id:
        description: 'NativeBridge Device ID'
        required: true
        default: '67a642531a4aa535498192f8'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Upload & Start Session
        env:
          NATIVEBRIDGE_API_KEY: ${{ secrets.NATIVEBRIDGE_API_KEY }}
        run: |
          DEVICE_ID="${{ github.event.inputs.device_id || '67a642531a4aa535498192f8' }}"
          APK_PATH="path/to/your/app.apk"

          RESPONSE=$(curl -X POST "https://api.nativebridge.io/v1/application/session" \
            -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
            -F "deviceId=$DEVICE_ID" \
            -F "deviceType=android" \
            -F "file=@$APK_PATH" \
            -F "region=ind" \
            -F "versionAction=create_new_version" \
            -w "\n%{http_code}" \
            -s)

          HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
          BODY=$(echo "$RESPONSE" | sed '$d')

          if [ "$HTTP_CODE" -eq 200 ]; then
            SESSION_URL=$(echo "$BODY" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')
            APP_ID=$(echo "$BODY" | grep -o '"appId":"[^"]*"' | sed 's/"appId":"//;s/"//')

            echo "Session started successfully!"
            echo "Session URL: $SESSION_URL"
            echo "App ID: $APP_ID"

            echo "SESSION_URL=$SESSION_URL" >> $GITHUB_ENV
            echo "APP_ID=$APP_ID" >> $GITHUB_ENV
          else
            echo "Failed (HTTP $HTTP_CODE): $BODY"
            exit 1
          fi

      - name: Add to Summary
        run: |
          echo "### NativeBridge Session Started" >> $GITHUB_STEP_SUMMARY
          echo "**Session URL:** [${{ env.SESSION_URL }}](${{ env.SESSION_URL }})" >> $GITHUB_STEP_SUMMARY
```

**Required Secret:** Add `NATIVEBRIDGE_API_KEY` in repo Settings → Secrets → Actions

---

### GitLab CI

Add to `.gitlab-ci.yml`:

```yaml
deploy_nativebridge:
  stage: deploy
  image: curlimages/curl:latest
  variables:
    DEVICE_ID: "67a642531a4aa535498192f8"
  script:
    - |
      RESPONSE=$(curl -X POST "https://api.nativebridge.io/v1/application/session" \
        -H "X-Api-Key: ${NATIVEBRIDGE_API_KEY}" \
        -F "deviceId=${DEVICE_ID}" \
        -F "deviceType=android" \
        -F "file=@./app.apk" \
        -F "region=ind" \
        -w "\n%{http_code}" \
        -s)

      HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
      BODY=$(echo "$RESPONSE" | sed '$d')

      if [ "$HTTP_CODE" -eq 200 ]; then
        echo "Session started!"
        echo "$BODY" | grep -o '"sessionUrl":"[^"]*"'
      else
        echo "Failed: $BODY"
        exit 1
      fi
  only:
    - tags
```

**Required Variable:** Add `NATIVEBRIDGE_API_KEY` in Settings → CI/CD → Variables (masked)

---

### Jenkins

Add to `Jenkinsfile`:

```groovy
pipeline {
    agent any

    environment {
        NATIVEBRIDGE_API_KEY = credentials('nativebridge-api-key')
        DEVICE_ID = '67a642531a4aa535498192f8'
    }

    stages {
        stage('Deploy to NativeBridge') {
            steps {
                script {
                    def response = sh(
                        script: """
                            curl -X POST "https://api.nativebridge.io/v1/application/session" \
                              -H "X-Api-Key: ${NATIVEBRIDGE_API_KEY}" \
                              -F "deviceId=${DEVICE_ID}" \
                              -F "deviceType=android" \
                              -F "file=@./app.apk" \
                              -F "region=ind" \
                              -w "\\n%{http_code}" \
                              -s
                        """,
                        returnStdout: true
                    ).trim()

                    def lines = response.split('\n')
                    def httpCode = lines[-1]
                    def body = lines[0..-2].join('\n')

                    if (httpCode == '200') {
                        echo "Session started successfully!"
                        echo body
                    } else {
                        error "Failed (HTTP ${httpCode}): ${body}"
                    }
                }
            }
        }
    }
}
```

**Required Credential:** Add `nativebridge-api-key` in Manage Jenkins → Credentials

---

### Circle CI

Add to `.circleci/config.yml`:

```yaml
version: 2.1

jobs:
  deploy-nativebridge:
    docker:
      - image: cimg/base:stable
    steps:
      - checkout
      - run:
          name: Upload & Start Session
          command: |
            RESPONSE=$(curl -X POST "https://api.nativebridge.io/v1/application/session" \
              -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
              -F "deviceId=67a642531a4aa535498192f8" \
              -F "deviceType=android" \
              -F "file=@./app.apk" \
              -F "region=ind" \
              -w "\n%{http_code}" \
              -s)

            HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
            BODY=$(echo "$RESPONSE" | sed '$d')

            if [ "$HTTP_CODE" -eq 200 ]; then
              echo "Session started!"
              echo "$BODY" | grep -o '"sessionUrl":"[^"]*"'
            else
              echo "Failed: $BODY"
              exit 1
            fi

workflows:
  deploy:
    jobs:
      - deploy-nativebridge:
          filters:
            tags:
              only: /^v.*/
```

**Required Variable:** Add `NATIVEBRIDGE_API_KEY` in Project Settings → Environment Variables

---

### Bitrise

Add script step to `bitrise.yml`:

```yaml
- script@1:
    title: Deploy to NativeBridge
    inputs:
      - content: |
          #!/bin/bash
          set -e

          RESPONSE=$(curl -X POST "https://api.nativebridge.io/v1/application/session" \
            -H "X-Api-Key: $NATIVEBRIDGE_API_KEY" \
            -F "deviceId=67a642531a4aa535498192f8" \
            -F "deviceType=android" \
            -F "file=@$BITRISE_APK_PATH" \
            -F "region=ind" \
            -w "\n%{http_code}" \
            -s)

          HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
          BODY=$(echo "$RESPONSE" | sed '$d')

          if [ "$HTTP_CODE" -eq 200 ]; then
            SESSION_URL=$(echo "$BODY" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')
            echo "Session URL: $SESSION_URL"
            envman add --key NATIVEBRIDGE_SESSION_URL --value "$SESSION_URL"
          else
            echo "Failed: $BODY"
            exit 1
          fi
```

**Required Secret:** Add `NATIVEBRIDGE_API_KEY` in Workflow → Secrets

---

### Generic Script

Use this script in any CI/CD:

```bash
#!/bin/bash
set -e

# Configuration
API_KEY="${NATIVEBRIDGE_API_KEY}"
DEVICE_ID="${DEVICE_ID:-67a642531a4aa535498192f8}"
APK_PATH="${APK_PATH:-./app.apk}"
REGION="${REGION:-ind}"

# Validate
if [ -z "$API_KEY" ]; then
  echo "Error: NATIVEBRIDGE_API_KEY not set"
  exit 1
fi

if [ ! -f "$APK_PATH" ]; then
  echo "Error: APK not found at $APK_PATH"
  exit 1
fi

echo "Uploading to NativeBridge..."
echo "  APK: $APK_PATH"
echo "  Device: $DEVICE_ID"
echo "  Region: $REGION"

# Call Combined API
RESPONSE=$(curl -X POST "https://api.nativebridge.io/v1/application/session" \
  -H "X-Api-Key: $API_KEY" \
  -F "deviceId=$DEVICE_ID" \
  -F "deviceType=android" \
  -F "file=@$APK_PATH" \
  -F "region=$REGION" \
  -F "versionAction=create_new_version" \
  -F "accessType=public" \
  -w "\n%{http_code}" \
  -s)

# Parse response
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
  SESSION_ID=$(echo "$BODY" | grep -o '"sessionId":"[^"]*"' | sed 's/"sessionId":"//;s/"//')
  SESSION_URL=$(echo "$BODY" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')
  APP_ID=$(echo "$BODY" | grep -o '"appId":"[^"]*"' | sed 's/"appId":"//;s/"//')
  MAGIC_LINK=$(echo "$BODY" | grep -o '"magicLink":"[^"]*"' | sed 's/"magicLink":"//;s/"//')

  echo ""
  echo "SUCCESS!"
  echo "  Session ID:  $SESSION_ID"
  echo "  Session URL: $SESSION_URL"
  echo "  App ID:      $APP_ID"
  echo "  Magic Link:  $MAGIC_LINK"
  echo ""
  echo "Open this URL to test: $SESSION_URL"
else
  echo ""
  echo "FAILED (HTTP $HTTP_CODE)"
  echo "$BODY"
  exit 1
fi
```

**Usage:**

```bash
export NATIVEBRIDGE_API_KEY="your-api-key"
export DEVICE_ID="67a642531a4aa535498192f8"
export APK_PATH="./app-release.apk"

./deploy-nativebridge.sh
```

---

## Response Handling

### Success Response (200)

```json
{
  "data": {
    "sessionId": "Qk2d",
    "sessionUrl": "https://nativebridge.io/session/Qk2d",
    "appId": "HgWp",
    "magicLink": "https://nativebridge.io/app/HgWp",
    "message": "Application uploaded and session started successfully"
  }
}
```

| Field | Description |
|-------|-------------|
| `sessionId` | Unique session identifier |
| `sessionUrl` | URL to open the live session |
| `appId` | Your app's ID on NativeBridge |
| `magicLink` | Permanent link to your app |

### Extracting Values (Bash)

```bash
SESSION_URL=$(echo "$RESPONSE" | grep -o '"sessionUrl":"[^"]*"' | sed 's/"sessionUrl":"//;s/"//')
APP_ID=$(echo "$RESPONSE" | grep -o '"appId":"[^"]*"' | sed 's/"appId":"//;s/"//')
```

### Extracting Values (jq)

```bash
SESSION_URL=$(echo "$RESPONSE" | jq -r '.data.sessionUrl')
APP_ID=$(echo "$RESPONSE" | jq -r '.data.appId')
```

---

## Error Handling

### Error Responses

| HTTP Code | Error | Solution |
|-----------|-------|----------|
| 400 | Missing file or URL | Provide `file` or `apkUrl` |
| 400 | Invalid device type | Use `android` or `ios` |
| 400 | Invalid file extension | Use .apk, .aab, .ipa, or .zip |
| 401 | Invalid API key | Check your API key |
| 403 | Monthly quota exceeded | Upgrade your plan |
| 404 | Device not found | Check device ID |
| 409 | Application already exists | Use `versionAction=create_new_version` |
| 422 | Missing required fields | Check `deviceId` and `deviceType` |
| 429 | Rate limit exceeded | Wait 60 seconds (10 req/min limit) |

### Error Handling Example

```bash
case $HTTP_CODE in
  200)
    echo "Success!"
    ;;
  401)
    echo "Invalid API key - check NATIVEBRIDGE_API_KEY"
    exit 1
    ;;
  404)
    echo "Device not found - check DEVICE_ID"
    exit 1
    ;;
  429)
    echo "Rate limited - waiting 60s..."
    sleep 60
    # Retry
    ;;
  *)
    echo "Error ($HTTP_CODE): $BODY"
    exit 1
    ;;
esac
```

---

## Getting Started

1. **Get API Key:** [NativeBridge Dashboard](https://nativebridge.io/dashboard/api-keys)
2. **Get Device ID:** From NativeBridge dashboard or contact support
3. **Add to CI/CD secrets:** Store `NATIVEBRIDGE_API_KEY` securely
4. **Copy the snippet** for your CI/CD platform above
5. **Run your pipeline!**

---


