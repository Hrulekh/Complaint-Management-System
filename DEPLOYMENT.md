# Deployment Guide

## Local Development (Docker Compose)

### Prerequisites
- Docker and Docker Compose installed
- Git repository cloned

### Quick Start

```bash
cp .env.example .env
docker compose up --build
```

The system will be available at:
- **Frontend:** http://localhost
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html

### Default Credentials
- **Email:** admin@cms.local
- **Password:** admin123

### Environment Variables

Edit `.env` before running:

```env
DB_HOST=mysql
DB_PORT=3306
DB_NAME=cms
DB_USER=cms_user
DB_PASSWORD=cms_password
MYSQL_ROOT_PASSWORD=root_password

JWT_SECRET=change-this-to-a-strong-random-string
ADMIN_PASSWORD=admin123

CORS_ORIGINS=http://localhost:5173,http://localhost

UPLOAD_DIR=/app/uploads

SPRING_PROFILES_ACTIVE=dev
```

### Development Without Docker

**Backend:**
```bash
cd backend
mvn clean install
export JWT_SECRET="dev-secret"
export DB_HOST=localhost DB_USER=cms_user DB_PASSWORD=cms_password
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Access at http://localhost:5173

## Cloud Deployment

### Option 1: Render + Vercel + Aiven (Recommended)

#### 1. Database (Aiven MySQL)
```bash
# Create account at https://aiven.io
# Create free MySQL instance
# Get connection string: mysql://user:pass@host:3306/db
```

#### 2. Backend (Render)
1. Push code to GitHub
2. Create new Web Service on Render.com
3. Connect GitHub repo
4. Set build command: `cd backend && mvn clean package -DskipTests`
5. Set start command: `java -jar target/*.jar`
6. Add environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=mysql://user:pass@host:3306/db`
   - `JWT_SECRET=<strong-random-string>`
   - `CORS_ORIGINS=https://your-frontend.vercel.app`
7. Deploy

#### 3. Frontend (Vercel)
1. Create project on Vercel
2. Connect GitHub repo (frontend directory)
3. Set build command: `npm run build`
4. Set output directory: `dist`
5. Add environment variable:
   - `VITE_API_BASE_URL=https://your-backend.onrender.com`
6. Deploy

#### 4. Post-Deployment
Update `CORS_ORIGINS` on backend with frontend URL
```bash
# Set via Render environment variables and redeploy
```

### Option 2: Single VPS (AWS EC2 / DigitalOcean)

#### Prerequisites
- Ubuntu 22.04 LTS server
- SSH access
- Domain name (optional, for SSL)

#### Setup Steps

```bash
# 1. Install Docker
sudo apt update
sudo apt install docker.io docker-compose

# 2. Clone repository
git clone <repo-url>
cd complaint-management-system

# 3. Configure environment
cp .env.example .env
# Edit .env with production values

# 4. Setup SSL (Caddy)
sudo apt install caddy

# Create Caddyfile
sudo tee /etc/caddy/Caddyfile > /dev/null <<EOF
your-domain.com {
  reverse_proxy localhost:80
}
EOF

# 5. Start services
docker compose up -d

# 6. Start Caddy
sudo systemctl restart caddy
```

Access at https://your-domain.com

## Production Checklist

Before going live:

- [ ] **Security**
  - [ ] Change JWT_SECRET to strong random string (32+ chars)
  - [ ] Change ADMIN_PASSWORD in database
  - [ ] Enable HTTPS (SSL certificates installed)
  - [ ] Set CORS_ORIGINS to exact frontend URL
  - [ ] Verify no debug mode enabled

- [ ] **Database**
  - [ ] ddl-auto=validate (never `create` or `update`)
  - [ ] Backups configured
  - [ ] Connection pooling optimized
  - [ ] Indexes applied

- [ ] **Backend**
  - [ ] SPRING_PROFILES_ACTIVE=prod
  - [ ] Swagger disabled (springdoc.swagger-ui.enabled=false)
  - [ ] Health check endpoint responding
  - [ ] Logs aggregated (Datadog, Splunk, etc.)
  - [ ] Error tracking enabled (Sentry, etc.)

- [ ] **Frontend**
  - [ ] Build optimized (npm run build)
  - [ ] No console errors
  - [ ] Analytics configured
  - [ ] CDN configured for static assets

- [ ] **Deployment**
  - [ ] DNS configured
  - [ ] SSL certificate valid
  - [ ] HSTS headers enabled
  - [ ] Rate limiting configured
  - [ ] DDoS protection enabled

- [ ] **Monitoring**
  - [ ] Application health checks
  - [ ] Database connection pool monitoring
  - [ ] Disk space alerts
  - [ ] Memory/CPU alerts
  - [ ] API response time monitoring

## Database Backups

### Automated Backup Script

```bash
#!/bin/bash
# backup.sh
BACKUP_DIR="/backups/cms"
DB_HOST="mysql.example.com"
DB_USER="cms_user"
DB_PASSWORD="secure_password"
DB_NAME="cms"
RETENTION_DAYS=30

mkdir -p $BACKUP_DIR

# Create backup
BACKUP_FILE="$BACKUP_DIR/cms_backup_$(date +%Y%m%d_%H%M%S).sql.gz"
mysqldump -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME | gzip > $BACKUP_FILE

# Upload to S3
aws s3 cp $BACKUP_FILE s3://cms-backups/

# Clean old backups
find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: $BACKUP_FILE"
```

### Schedule with Cron

```bash
# Run daily at 2 AM
0 2 * * * /path/to/backup.sh
```

### Restore from Backup

```bash
# Decompress backup
gunzip cms_backup_20261006_020000.sql.gz

# Restore to database
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME < cms_backup_20261006_020000.sql
```

## Scaling Considerations

### Horizontal Scaling
- Use load balancer (AWS ELB, Nginx) for multiple backend instances
- Share uploads volume via NFS or S3
- Use managed database (RDS, Aiven)

### Performance Optimization
- Enable Redis caching for frequently accessed data
- Use CDN for static assets
- Optimize database queries with additional indexes
- Implement pagination limits

### Monitoring at Scale
- Set up centralized logging (ELK stack, CloudWatch)
- Monitor queue depths if async processing added
- Track distributed tracing (Jaeger)
- Set up alerts for anomalies

## Troubleshooting

### Backend won't start
```bash
docker logs cms-backend
# Check: DB connection, JWT_SECRET, ports
```

### Database connection failed
```bash
# Verify credentials in .env
# Check MySQL service is running
docker logs cms-mysql
```

### Frontend not connecting to backend
```bash
# Check CORS_ORIGINS matches frontend URL
# Verify VITE_API_BASE_URL in frontend
# Check browser console for errors
```

### Out of disk space
```bash
# Check upload directory
du -sh /app/uploads

# Clean old uploads or increase volume size
```

### High memory usage
```bash
# Check Java heap settings in docker-compose
# Increase JAVA_OPTS if needed
```

## Support

For deployment issues:
1. Check docker-compose logs: `docker compose logs -f`
2. Verify all environment variables are set
3. Check firewall and security groups
4. Review error logs in application monitoring tools
