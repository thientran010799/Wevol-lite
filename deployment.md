# Wevol-lite — Deployment Guide

## Architecture

| Layer | Platform | URL pattern |
|---|---|---|
| Frontend | Vercel | `https://wevol-lite.vercel.app` |
| Backend | Render | `https://wevol-backend.onrender.com` |
| Database | Neon (free PostgreSQL) | `ep-xxx.us-east-2.aws.neon.tech` |
| Media | Cloudflare R2 | `https://pub-xxx.r2.dev` |

> **Render free tier note:** The backend spins down after 15 minutes of inactivity. The first request after a sleep takes ~30–50 seconds. This is normal for a private couple's app — just open the app and wait a moment if it's been unused.

---

## Environment Variables

Set these in Render's dashboard (Environment tab) and Vercel's dashboard.

| Variable | Where | Value |
|---|---|---|
| `DATABASE_URL` | Render | `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require` |
| `DB_USER` | Render | Neon username |
| `DB_PASS` | Render | Neon password |
| `R2_ACCOUNT_ID` | Render | Cloudflare account ID |
| `R2_ACCESS_KEY` | Render | R2 access key |
| `R2_SECRET_KEY` | Render | R2 secret key |
| `R2_BUCKET` | Render | `wevol-media` |
| `R2_PUBLIC_URL` | Render | `https://pub-xxx.r2.dev` |
| `FRONTEND_URL` | Render | Vercel URL (for R2 CORS config) |
| `VITE_API_URL` | Vercel | Render service URL |

---

## First Deploy

### 1. Set up Neon database
1. Sign up at https://neon.tech (free, no card required)
2. Create new project → copy the connection string
3. Format for `DATABASE_URL`: `jdbc:postgresql://ep-xxx.region.aws.neon.tech/neondb?sslmode=require`
4. Note the username and password separately for `DB_USER` / `DB_PASS`

### 2. Push to GitHub
```bash
cd /Users/tranvanthien010799/Thien/project/Wevol-lite
git init
git add .
git commit -m "feat: initial Wevol-lite"
git remote add origin https://github.com/YOUR_USER/wevol-lite.git
git push -u origin main
```

### 3. Deploy backend to Render
1. Go to https://render.com → Sign in with GitHub (no card required)
2. Click **New → Web Service**
3. Connect your `wevol-lite` GitHub repo
4. Set **Root Directory** → `backend`
5. Set **Runtime** → `Docker`
6. Set **Instance Type** → **Free**
7. Add all environment variables from the table above
8. Click **Create Web Service** → wait ~5-10 minutes for first build
9. Save the Render URL (e.g. `https://wevol-backend.onrender.com`)

### 4. Deploy frontend to Vercel
1. Go to https://vercel.com → Add New Project → import `wevol-lite` repo
2. Set **Root Directory** → `frontend`
3. Add environment variable: `VITE_API_URL` = your Render URL from step 3
4. Click **Deploy** → save your Vercel URL

### 5. Update FRONTEND_URL on Render
After Vercel deploy, go back to Render → your service → **Environment** tab:
- Update `FRONTEND_URL` to your Vercel URL (e.g. `https://wevol-lite.vercel.app`)
- Click **Save Changes** → Render will redeploy automatically

---

## Redeployment (after code changes)

Both platforms auto-deploy on `git push main` once connected to GitHub.

```bash
# Commit and push changes
git add .
git commit -m "fix: ..."
git push
```

- **Frontend** — Vercel redeploys automatically within ~1 minute
- **Backend** — Render rebuilds the Docker image, takes ~5 minutes

### Manual redeploy (without code changes)
- Render dashboard → your service → **Manual Deploy** button
- Vercel dashboard → your project → **Deployments** → **Redeploy**

---

## Local Development

```bash
# Copy and fill in credentials
cp .env.example .env

# Start Postgres + backend (Docker required)
docker compose up -d

# Start frontend dev server
cd frontend
npm install
npm run dev        # → http://localhost:5173
```

Vite proxies all `/api/*` requests to `http://localhost:8080` automatically.

---

## Monitor and Logs

- **Render logs:** Dashboard → your service → **Logs** tab (live streaming)
- **Render metrics:** Dashboard → your service → **Metrics** tab
- **Vercel logs:** Dashboard → your project → **Deployments** → select deployment → **Functions** tab

Free tier limits: Render gives 750 hours/month (enough for 24/7 uptime if you upgrade, or ~25 days of always-on). For a private couple's app the sleep behavior means it uses far fewer hours.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| First request takes 30-50s | Render free tier spin-up | Normal — wait and retry |
| `Connection refused` on API | Backend still starting | Wait 60s after deploy, check Render logs |
| Photos not loading | R2 public URL mismatch | Check `R2_PUBLIC_URL` env var, must match your R2 bucket public domain |
| DB migration fails | Wrong `DATABASE_URL` format | Must include `?sslmode=require` for Neon |
| CORS error in browser | `FRONTEND_URL` not set | Update `FRONTEND_URL` env var on Render to your Vercel URL |
