# Lab-10: Jenkins Pipeline – Test Stage
## Selenium + JUnit 5 Login Test Suite

---

## Project Structure

```
SeleniumLoginTest/
├── pom.xml                                          ← Maven build file
├── Jenkinsfile                                      ← Jenkins pipeline
└── src/
    └── test/
        └── java/
            └── com/lab10/
                └── LoginTest.java                   ← 5 Selenium test cases
```

---

## Step 1 – Update Credentials (if needed)

Open `src/test/java/com/lab10/LoginTest.java` and update the constants at the top:

```java
private static final String VALID_EMAIL    = "admin@example.com"; // ← real account
private static final String VALID_PASSWORD = "admin123";          // ← real password
```

TC-05 (valid login) uses these. The other four tests use wrong/empty values intentionally.

---

## Step 2 – Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit – Lab-10 Selenium test suite"
git remote add origin https://github.com/YOUR_USERNAME/SeleniumLoginTest.git
git push -u origin main
```

---

## Step 3 – Add GitHub Webhook

1. Go to your GitHub repository → **Settings → Webhooks → Add webhook**
2. Payload URL: `http://<YOUR_JENKINS_IP>:8080/github-webhook/`
3. Content type: `application/json`
4. Events: **Just the push event**
5. Click **Add webhook**

---

## Step 4 – Configure Jenkins Pipeline

1. **Jenkins → New Item → Pipeline** → name it `Lab10-SeleniumTest`
2. Under **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/YOUR_USERNAME/SeleniumLoginTest.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
3. Tick **GitHub hook trigger for GITScm polling**
4. Click **Save**

---

## Step 5 – Configure Email (Email Ext Plugin)

In **Jenkins → Manage Jenkins → Configure System → Extended E-mail Notification**:

| Field | Value |
|---|---|
| SMTP Server | `smtp.gmail.com` (or your provider) |
| SMTP Port | `465` (SSL) or `587` (TLS) |
| Credentials | Add Jenkins credential with Gmail app password |
| Default recipients | your email |

> **Gmail tip:** Enable 2FA then create an **App Password** at myaccount.google.com/apppasswords.

---

## Step 6 – Run the Pipeline

- Trigger manually: **Build Now**
- Or push a commit – the webhook fires Jenkins automatically
- After the build, check **Test Results** on the build page
- The committer receives an email summary automatically

---

## Test Cases Summary

| # | Test | Expected Result |
|---|------|----------------|
| TC-01 | Page loads | Title non-empty, email field visible |
| TC-02 | Wrong credentials | Error message shown |
| TC-03 | Empty email | Stays on login page |
| TC-04 | Empty password | Stays on login page |
| TC-05 | Valid credentials | Redirected away from login page |

---

## Docker Image Used

`markhobson/maven-chrome` — bundles **Maven + Chrome + ChromeDriver** in a single image, perfect for running headless Selenium tests inside Jenkins without installing anything on the host.

---

## Add Collaborator

Add `qasimalik@gmail.com` as a collaborator:  
**GitHub repo → Settings → Collaborators → Add people**
