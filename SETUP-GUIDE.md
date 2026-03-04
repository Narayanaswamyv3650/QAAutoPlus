# 🚀 QA Auto Plus — Complete Setup Guide

## Table of Contents
1. [Neon PostgreSQL Database Setup](#1-neon-postgresql-database-setup)
2. [Local Development Setup](#2-local-development-setup)
3. [Test CRUD Operations](#3-test-crud-operations)
4. [Deploy to Netlify](#4-deploy-to-netlify)
5. [Troubleshooting](#5-troubleshooting)

---

## 1. Neon PostgreSQL Database Setup

### Step 1: Create a Neon Account
1. Go to **[https://console.neon.tech](https://console.neon.tech)**
2. Click **"Sign Up"** (use GitHub, Google, or email)
3. Verify your email if needed

### Step 2: Create a New Project
1. After login, click **"New Project"**
2. Fill in:
   - **Project Name**: `qaautoplus`
   - **Postgres Version**: `16` (latest)
   - **Region**: Choose the closest to you (e.g., `US East (Ohio)` or `EU (Frankfurt)`)
3. Click **"Create Project"**

### Step 3: Get Your Connection Details
After creating the project, Neon shows your connection details. **Copy and save these**:

| Field | Example Value |
|-------|---------------|
| **Host** | `ep-cool-name-123456.us-east-2.aws.neon.tech` |
| **Database** | `neondb` (default) |
| **User** | `neondb_owner` |
| **Password** | `your-generated-password` |

### Step 4: Build Your Connection String
Your `DATABASE_URL` follows this format:
```
jdbc:postgresql://<HOST>/<DATABASE>?sslmode=require
```

**Example:**
```
jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
```

> 💡 **Where to find it:** In the Neon Console → Your Project → **Dashboard** → **Connection Details** → Select **Java (JDBC)** from the dropdown.

### Step 5: Verify in Neon Console
1. Go to **"SQL Editor"** in the left sidebar
2. Run this query to confirm the database is ready:
   ```sql
   SELECT version();
   ```
3. You should see the PostgreSQL version — your database is ready!

---

## 2. Local Development Setup

### Prerequisites
- **Java 20+** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -version`)
- **Git** installed

### Step 1: Set Environment Variables (Windows PowerShell)

Open **PowerShell** and set these before running the app:

```powershell
# Replace with YOUR Neon connection details from Step 1
$env:DATABASE_URL = "jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require"
$env:DATABASE_USER = "neondb_owner"
$env:DATABASE_PASSWORD = "your-neon-password-here"
```

> ⚠️ **Important:** These environment variables only last for the current PowerShell session. To make them permanent, see [Make Env Vars Permanent](#make-environment-variables-permanent-windows) below.

### Step 2: Build the Project

```powershell
cd C:\Users\njonnala\Automation\WaterProject
mvn clean package -DskipTests
```

### Step 3: Start the Server

```powershell
java -jar target\qaautoplus.jar
```

You should see output like:
```
  Database tables ready.
  Database connected: jdbc:postgresql://ep-cool-name-123456...
============================================
  QA Auto Plus server started successfully!
============================================
  Port       : 8088  (default)
  Home page  : http://localhost:8088/home
  Blog page  : http://localhost:8088/blog/ai-automation-blog.html
  API status : http://localhost:8088/api/status
============================================
```

### Step 4: Open in Browser
- **Blog page:** [http://localhost:8088/blog/ai-automation-blog.html](http://localhost:8088/blog/ai-automation-blog.html)
- **API status:** [http://localhost:8088/api/status](http://localhost:8088/api/status)

### Make Environment Variables Permanent (Windows)

**Option A — System Environment Variables (recommended):**
1. Press `Win + R`, type `sysdm.cpl`, press Enter
2. Go to **Advanced** → **Environment Variables**
3. Under **User Variables**, click **New** for each:
   - Variable: `DATABASE_URL`, Value: `jdbc:postgresql://ep-...neon.tech/neondb?sslmode=require`
   - Variable: `DATABASE_USER`, Value: `neondb_owner`
   - Variable: `DATABASE_PASSWORD`, Value: `your-password`
4. Click **OK** → **OK**
5. **Restart** any open terminals/IDE

**Option B — PowerShell Profile (auto-loads on each session):**
```powershell
# Open your PowerShell profile
notepad $PROFILE

# Add these lines at the bottom:
$env:DATABASE_URL = "jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require"
$env:DATABASE_USER = "neondb_owner"
$env:DATABASE_PASSWORD = "your-neon-password-here"

# Save and close — restart PowerShell
```

### Quick Start Script

You can also use the included `start.bat` — but first create a `.env.bat` file for your credentials:

Create file **`.env.bat`** in the project root:
```bat
@echo off
set DATABASE_URL=jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
set DATABASE_USER=neondb_owner
set DATABASE_PASSWORD=your-neon-password-here
```

Then run:
```powershell
.\.env.bat
mvn clean package -DskipTests
java -jar target\qaautoplus.jar
```

---

## 3. Test CRUD Operations

Once the server is running at `http://localhost:8088`, you can test all CRUD operations:

### Create an Article (POST)
Using **PowerShell**:
```powershell
Invoke-RestMethod -Method POST -Uri "http://localhost:8088/api/articles" `
  -ContentType "application/json" `
  -Body '{"title":"My First AI Article","excerpt":"Testing CRUD operations","content":"This is the full content of the article about AI automation.","author":"Test User","category":"ai","featured":true}'
```

Or using **curl** (if installed):
```bash
curl -X POST http://localhost:8088/api/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"My First AI Article","excerpt":"Testing CRUD","content":"Full content here...","author":"Test User","category":"ai","featured":true}'
```

### Read All Articles (GET)
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/articles"
```

### Read Articles by Category (GET)
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/articles?category=ai"
```

### Read Single Article (GET)
```powershell
# Replace 1 with the actual article ID
Invoke-RestMethod -Uri "http://localhost:8088/api/articles/1"
```

### Update an Article (PUT)
```powershell
# Replace 1 with the actual article ID
Invoke-RestMethod -Method PUT -Uri "http://localhost:8088/api/articles/1" `
  -ContentType "application/json" `
  -Body '{"title":"Updated Title","excerpt":"Updated summary","content":"Updated full content of the article.","author":"Test User","category":"ai","featured":false}'
```

### Delete an Article (DELETE)
```powershell
# Replace 1 with the actual article ID
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8088/api/articles/1"
```

### Using the Web UI
1. Open [http://localhost:8088/blog/ai-automation-blog.html](http://localhost:8088/blog/ai-automation-blog.html)
2. Scroll down to **"Publish Your Insight"** section
3. Fill in: **Your Name**, **Article Title**, **Excerpt**, **Content**, select a **Category**
4. Click **"Publish Article →"**
5. The article appears in the articles grid above!

### Verify in Neon Console
1. Go to [https://console.neon.tech](https://console.neon.tech)
2. Select your project → **SQL Editor**
3. Run:
   ```sql
   SELECT * FROM articles ORDER BY created_at DESC;
   ```
4. You'll see all your created articles!

---

## 4. Deploy to Netlify

> **Important Note:** Netlify is a **static site hosting** platform. It can host only the front-end (HTML/CSS/JS). The Java backend (Jetty + API) needs to run separately on a platform like **Render**, **Railway**, **Fly.io**, or **Azure**. Below are instructions for both parts.

### Part A: Deploy the Static Frontend to Netlify

#### Step 1: Push Code to GitHub
```powershell
cd C:\Users\njonnala\Automation\WaterProject
git init
git add .
git commit -m "Initial commit - QA Auto Plus"
git remote add origin https://github.com/YOUR-USERNAME/WaterProject.git
git push -u origin main
```

#### Step 2: Connect to Netlify
1. Go to **[https://app.netlify.com](https://app.netlify.com)**
2. Click **"Add new site"** → **"Import an existing project"**
3. Choose **GitHub** and authorize Netlify
4. Select your **WaterProject** repository

#### Step 3: Configure Build Settings
Netlify will auto-detect settings from `netlify.toml`, but verify:

| Setting | Value |
|---------|-------|
| **Base directory** | `src/main/webapp` |
| **Build command** | _(leave empty)_ |
| **Publish directory** | `.` |

5. Click **"Deploy site"**

#### Step 4: Your Site is Live!
- Netlify gives you a URL like: `https://your-site-name.netlify.app`
- The root URL redirects to `/blog/ai-automation-blog.html` (configured in `netlify.toml`)

#### Step 5: Custom Domain (Optional)
1. In Netlify Dashboard → **Domain Management**
2. Click **"Add custom domain"**
3. Follow the DNS setup instructions

### Part B: Deploy the Java Backend (API Server)

Since Netlify only hosts static files, you need a separate platform for the Java API. Here are the best free options:

#### Option 1: Render.com (Recommended — Free Tier)

1. Go to **[https://render.com](https://render.com)** → Sign up
2. Click **"New"** → **"Web Service"**
3. Connect your GitHub repo
4. Configure:
   - **Name**: `qaautoplus-api`
   - **Environment**: `Docker`
   - **Region**: Choose closest to you
5. Add **Environment Variables:**
   | Key | Value |
   |-----|-------|
   | `DATABASE_URL` | `jdbc:postgresql://ep-...neon.tech/neondb?sslmode=require` |
   | `DATABASE_USER` | `neondb_owner` |
   | `DATABASE_PASSWORD` | `your-neon-password` |
   | `PORT` | `8088` |
6. Click **"Create Web Service"**
7. Your API will be at: `https://qaautoplus-api.onrender.com`

#### Option 2: Railway.app

1. Go to **[https://railway.app](https://railway.app)** → Sign up
2. Click **"New Project"** → **"Deploy from GitHub Repo"**
3. Select your repo
4. Add environment variables (same as above)
5. Railway auto-detects the Dockerfile

### Part C: Connect Frontend to Backend API

After deploying the backend, update the frontend to point to your API URL:

1. Edit `src/main/webapp/blog/js/ai-automation-blog.js`
2. Change line 3 from:
   ```javascript
   const API = '/api';
   ```
   to:
   ```javascript
   const API = 'https://qaautoplus-api.onrender.com/api';
   ```
3. Commit and push — Netlify auto-deploys!

### Netlify Environment Variables (for reference)
Even though Netlify doesn't run Java, you may want to store the API URL:
1. Go to **Netlify Dashboard** → **Site Settings** → **Environment Variables**
2. Add:
   - `API_URL` = `https://qaautoplus-api.onrender.com`

---

## 5. Troubleshooting

### "DATABASE_URL not set — running without database"
**Cause:** Environment variables are not set in the current session.
**Fix:** Set the environment variables before starting the server:
```powershell
$env:DATABASE_URL = "jdbc:postgresql://ep-...neon.tech/neondb?sslmode=require"
$env:DATABASE_USER = "neondb_owner"
$env:DATABASE_PASSWORD = "your-password"
java -jar target\qaautoplus.jar
```

### "Could not connect to database"
**Cause:** Wrong credentials or network issue.
**Fix:**
1. Verify your credentials in the [Neon Console](https://console.neon.tech)
2. Check that the connection string starts with `jdbc:postgresql://`
3. Ensure `?sslmode=require` is at the end of the URL
4. Check your internet connection

### "Failed to create article" (500 error)
**Cause:** Database tables might not exist.
**Fix:** The app auto-creates tables on startup. Restart the server:
```powershell
mvn clean package -DskipTests
java -jar target\qaautoplus.jar
```

### Background is black instead of white
**Fix:** Clear browser cache or open in incognito. The default theme is white. Toggle with the ◐ button in the navbar.

### Articles not showing after publishing
**Cause:** The page auto-reloads articles from the API. If using Netlify (static only), the API is not available.
**Fix:** Deploy the backend separately (see [Part B](#part-b-deploy-the-java-backend-api-server)).

### Port already in use
```powershell
# Find what's using port 8088
netstat -ano | findstr :8088
# Kill the process (replace PID)
taskkill /PID <PID> /F
```

---

## Architecture Overview

```
┌──────────────────────────────────┐
│         Netlify (Static)         │
│  HTML / CSS / JS (Frontend)      │
│  ai-automation-blog.html         │
└────────────┬─────────────────────┘
             │  fetch('/api/articles')
             ▼
┌──────────────────────────────────┐
│  Render / Railway (Backend)      │
│  Java 20 + Jetty (Embedded)      │
│  API Endpoints: /api/*           │
└────────────┬─────────────────────┘
             │  JDBC (SSL)
             ▼
┌──────────────────────────────────┐
│     Neon PostgreSQL (Cloud DB)   │
│  Tables: articles, categories    │
│  Free tier: 0.5 GB              │
└──────────────────────────────────┘
```

---

## Quick Reference — API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/status` | Health check |
| `GET` | `/api/articles` | List all articles |
| `GET` | `/api/articles?category=ai` | Filter by category |
| `GET` | `/api/articles/{id}` | Get single article |
| `POST` | `/api/articles` | Create new article |
| `PUT` | `/api/articles/{id}` | Update an article |
| `DELETE` | `/api/articles/{id}` | Delete article |
| `GET` | `/api/categories` | List categories |

### POST Body (Create Article):
```json
{
  "title": "Article Title",
  "excerpt": "Short summary",
  "content": "Full article content...",
  "author": "Author Name",
  "category": "ai",
  "featured": true
}
```

**Categories:** `ai`, `rpa`, `llm`, `devops`, `ml`

