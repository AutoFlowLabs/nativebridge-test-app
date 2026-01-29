# Troubleshooting: Permissions Error

## Error Message

```
RequestError [HttpError]: Resource not accessible by integration
status: 403
message: 'Resource not accessible by integration'
x-accepted-github-permissions: 'actions=write'
```

## Problem

The DEV Pipeline cannot trigger the QA Pipeline due to insufficient permissions.

## Root Cause

GitHub Actions workflows need explicit permission to trigger other workflows. The default `GITHUB_TOKEN` has limited permissions.

## Solution (Already Fixed ✅)

The DEV Pipeline workflow file now includes the required permissions:

```yaml
# In dev-pipeline.yml
permissions:
  contents: read
  actions: write
```

This grants the workflow permission to:
- Read repository contents
- Write/trigger GitHub Actions workflows

## Verification Steps

### 1. Check Workflow File Has Permissions

Open `.github/workflows/dev-pipeline.yml` and verify these lines exist near the top:

```yaml
permissions:
  contents: read
  actions: write
```

✅ **Status:** Already added to the workflow file.

### 2. (Optional) Check Repository Settings

If you still encounter issues, verify repository-wide workflow permissions:

**Steps:**
1. Go to: **Settings** → **Actions** → **General**
2. Scroll to: **"Workflow permissions"**
3. Check current setting:
   - ✅ **"Read and write permissions"** (Recommended)
   - ❌ **"Read repository contents and packages permissions"** (Limited)

**If it's set to "Read repository contents":**
1. Select: **"Read and write permissions"**
2. Check: ✅ **"Allow GitHub Actions to create and approve pull requests"** (optional)
3. Click: **"Save"**

## How It Works

### Before Fix (Failed)

```
DEV Pipeline (Job: trigger-qa-pipeline)
  ├─ Uses: actions/github-script@v7
  ├─ Token: GITHUB_TOKEN (default permissions)
  │   └─ Permissions: contents=read, actions=none ❌
  ├─ Attempts to: Trigger QA Pipeline workflow
  └─ Result: 403 Forbidden - "Resource not accessible"
```

### After Fix (Works)

```
DEV Pipeline (Job: trigger-qa-pipeline)
  ├─ Workflow Level: permissions.actions=write ✅
  ├─ Uses: actions/github-script@v7
  ├─ Token: GITHUB_TOKEN (enhanced permissions)
  │   └─ Permissions: contents=read, actions=write ✅
  ├─ Attempts to: Trigger QA Pipeline workflow
  └─ Result: 200 OK - QA Pipeline triggered successfully ✅
```

## Testing the Fix

### Test 1: Manual DEV Pipeline Run

1. Go to: **Actions** tab
2. Select: **"DEV Pipeline - Build and Upload to NativeBridge"**
3. Click: **"Run workflow"**
4. Use defaults and run
5. Wait for "Trigger QA Pipeline" job

**Expected Result:**
- ✅ "Trigger QA Pipeline" job succeeds
- ✅ QA Pipeline automatically starts
- ✅ No 403 error

### Test 2: Git Push Trigger

```bash
git commit -m "Test trigger for nativebridge"
git push
```

**Expected Result:**
- ✅ DEV Pipeline triggers automatically
- ✅ QA Pipeline triggers after DEV completes
- ✅ Both pipelines complete successfully

## Alternative: Using Personal Access Token (PAT)

If the workflow-level permissions don't work (rare cases with strict organization settings), you can use a Personal Access Token:

### Step 1: Create PAT

1. GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. Select scopes:
   - ✅ `repo` (Full control of private repositories)
   - ✅ `workflow` (Update GitHub Action workflows)
4. Copy the token

### Step 2: Add as Secret

1. Repository → Settings → Secrets and variables → Actions
2. New repository secret
3. Name: `WORKFLOW_TRIGGER_TOKEN`
4. Value: (paste the PAT)
5. Add secret

### Step 3: Update DEV Pipeline

Modify the "Trigger QA Pipeline" job in `dev-pipeline.yml`:

```yaml
- name: Trigger QA Pipeline
  uses: actions/github-script@v7
  with:
    github-token: ${{ secrets.WORKFLOW_TRIGGER_TOKEN }}  # Use PAT instead
    script: |
      # ... rest of script
```

**Note:** This is only needed if the workflow-level permissions don't work.

## Comparison

| Method | Pros | Cons | Recommended |
|--------|------|------|-------------|
| **Workflow Permissions** | ✅ Simple<br>✅ No extra secrets<br>✅ Automatic | ❌ May be limited by org settings | ✅ **Yes** (default) |
| **PAT Token** | ✅ Always works<br>✅ More control | ❌ Requires secret management<br>❌ Can expire<br>❌ Security risk if leaked | Only if workflow permissions fail |

## FAQ

### Q: Why do workflows need explicit permissions?

**A:** GitHub follows the principle of least privilege. By default, workflows can only read repository contents. To trigger other workflows (which could potentially run arbitrary code), explicit permission is required.

### Q: Is it safe to give `actions: write` permission?

**A:** Yes, when scoped to a specific workflow. The permission only allows that workflow to trigger other workflows in the same repository. It cannot:
- Access other repositories
- Modify repository settings
- Access secrets from other workflows

### Q: What if I get this error in an organization repository?

**A:** Some organizations have strict security policies that override workflow-level permissions. In that case:
1. Contact your GitHub org admin
2. Request `actions: write` permission for the repository
3. Or use the PAT method as a workaround

### Q: Will this permission expire?

**A:** No, workflow-level permissions don't expire. They're defined in the YAML file and persist as long as the file exists.

### Q: Can I revoke this permission later?

**A:** Yes, simply remove the `permissions` section from the workflow file. Note that this will break the auto-triggering of QA Pipeline.

## Related Documentation

- [GitHub Actions Permissions](https://docs.github.com/en/actions/security-guides/automatic-token-authentication#permissions-for-the-github_token)
- [Workflow Dispatch API](https://docs.github.com/rest/actions/workflows#create-a-workflow-dispatch-event)
- [GitHub Script Action](https://github.com/actions/github-script)

## Summary

✅ **Fix Applied:** Added `actions: write` permission to DEV Pipeline workflow

✅ **Should Work Now:** Next run should successfully trigger QA Pipeline

✅ **No Manual Setup Needed:** Permission is in the workflow file

❌ **If Still Failing:** Check repository settings or use PAT method

---

**Last Updated:** 2025-01-29

**Status:** Fixed in dev-pipeline.yml
