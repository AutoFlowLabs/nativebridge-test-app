# Session Configuration Testing Guide

Quick guide to test the session configuration parsing **without running the full 8-10 minute build**.

---

## 🎯 What This Tests

The test workflow verifies:
- ✅ Git tag annotations are properly fetched
- ✅ Session markers are correctly parsed from tag message
- ✅ Session condition logic works as expected
- ✅ Values are properly extracted (device ID, validity)
- ✅ Steps are triggered/skipped correctly

**Time:** ~30 seconds instead of 8-10 minutes!

---

## 🚀 Quick Test

### Test 1: Tag WITH Session (should trigger session steps)

```bash
./scripts/test-session.sh --with-session
```

**Expected Result:**
- ✅ Session Enabled: true
- ✅ "Session start condition MET!" message
- ✅ Device ID and validity extracted
- ✅ Session steps would execute

### Test 2: Tag WITHOUT Session (should skip session steps)

```bash
./scripts/test-session.sh
```

**Expected Result:**
- ❌ Session Enabled: false
- ❌ "Session start condition NOT met" message
- ❌ Session steps skipped

---

## 📊 Check Results

### Method 1: GitHub Actions UI

1. Go to your repository on GitHub
2. Click **Actions** tab
3. Look for workflow: **"Test Session Parsing"**
4. Click on the latest run
5. Review the logs and summary

### Method 2: Direct Link

```
https://github.com/YOUR_USERNAME/YOUR_REPO/actions
```

---

## 🔍 What to Look For

### In Workflow Logs

**If session is enabled, you should see:**
```
✅ SESSION_ENABLED=true
✅ Found Device ID: 67a642531a4aa535498192f8
✅ Found Session Validity: 120 seconds
🚀 Session start condition MET!
```

**If session is disabled, you should see:**
```
❌ SESSION_ENABLED=false (no markers found)
❌ Session start condition NOT met
```

### In Summary Tab

The workflow creates a summary showing:
- Tag name
- Session enabled status
- Parsed configuration (if session enabled)
- What would happen in production

---

## 🧪 Testing Different Scenarios

### Test with existing tag

You can also test with an existing tag (like v1.1.8 that has session config):

1. Go to **Actions** tab
2. Click **"Test Session Parsing"** workflow
3. Click **"Run workflow"** button
4. Select branch: `main`
5. Click **"Run workflow"**

This will test the current HEAD, but you can also:

```bash
# Create a test tag pointing to an existing commit with session
git tag -f test-existing v1.1.8
git push -f origin test-existing
```

---

## 🧹 Cleanup Test Tags

After testing, clean up the test tags:

### List test tags
```bash
git tag | grep "test-"
```

### Delete locally and remotely
```bash
# Replace TAG_NAME with your test tag
git tag -d test-session-1234567890
git push origin :refs/tags/test-session-1234567890
```

### Delete all test tags at once
```bash
# List all test tags
git tag | grep "^test-" | while read tag; do
    git tag -d "$tag"
    git push origin ":refs/tags/$tag"
done
```

---

## ✅ Verification Checklist

After running the test, verify:

- [ ] Test workflow triggered and completed
- [ ] Tag message is displayed in logs
- [ ] Session markers are found (if using --with-session)
- [ ] SESSION_ENABLED output is correct
- [ ] Device ID is extracted correctly
- [ ] Session validity is extracted correctly
- [ ] "Session start condition" step runs/skips correctly
- [ ] Summary shows expected results

---

## 🔧 Troubleshooting

### Issue: Tag message is empty

**Cause:** `fetch-depth: 0` might not be working

**Solution:** Check the "Checkout code" step has:
```yaml
- uses: actions/checkout@v4
  with:
    fetch-depth: 0
```

### Issue: Session markers not found

**Cause:** Tag might not have markers

**Solution:**
1. Verify tag locally:
   ```bash
   git tag -l --format='%(contents)' test-session-XXXXX
   ```
2. Look for `[NB_SESSION_ENABLED]` in output
3. Recreate tag if missing

### Issue: Workflow doesn't trigger

**Cause:** Test tags might not be configured in workflow

**Solution:** The workflow triggers on:
- Manual trigger (workflow_dispatch)
- Any tag starting with `test-*`

---

## 📋 Expected Timeline

| Action | Time |
|--------|------|
| Run test script | 5 seconds |
| Workflow starts | 10 seconds |
| Checkout & parse | 10 seconds |
| Complete test | 15 seconds |
| **Total** | **~30-40 seconds** |

Compare to full build: **8-10 minutes** ⚡

---

## 🎓 Understanding the Test

### What the test does:

1. **Fetch tag** with full history (`fetch-depth: 0`)
2. **Read tag message** using `git tag -l --format='%(contents)'`
3. **Search for markers** using `grep` for `[NB_SESSION_ENABLED]`
4. **Extract values** using `grep` and `sed` for device ID and validity
5. **Test conditions** using GitHub Actions `if` expressions
6. **Display results** in logs and summary

### This validates:

- The `fetch-depth: 0` fix works
- Tag annotations are accessible in workflow
- Parsing logic is correct
- Conditional steps work as expected

---

## 🚀 Next Steps

Once test passes:

1. ✅ Verify session parsing works
2. ✅ Create actual release with session:
   ```bash
   ./scripts/release.sh 1.2.0 --start-session
   ```
3. ✅ Monitor the full workflow
4. ✅ Check session URL in release notes

---

## 📝 Test Script Options

```bash
# Show help
./scripts/test-session.sh --help

# Test without session
./scripts/test-session.sh

# Test with session (recommended for validation)
./scripts/test-session.sh --with-session
```

---

**Ready to test? Run:**

```bash
./scripts/test-session.sh --with-session
```

Then check: https://github.com/YOUR_REPO/actions 🎉
