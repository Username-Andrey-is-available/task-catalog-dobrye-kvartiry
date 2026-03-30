# PowerShell Usage

## Start With PostgreSQL

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "task_catalog"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
gradle bootRun
```

## Start With Sample Data

```powershell
gradle bootRun --args='--spring.profiles.active=seed'
```

## Smoke Test

Create:

```powershell
$task = Invoke-RestMethod `
  -Method POST `
  -Uri "http://localhost:8080/api/tasks" `
  -ContentType "application/json" `
  -Body '{"title":"Prepare report","description":"Monthly financial report"}'
```

List:

```powershell
Invoke-RestMethod "http://localhost:8080/api/tasks?page=0&size=10"
```

Get by id:

```powershell
Invoke-RestMethod "http://localhost:8080/api/tasks/$($task.id)"
```

Update status:

```powershell
Invoke-RestMethod `
  -Method PATCH `
  -Uri "http://localhost:8080/api/tasks/$($task.id)/status" `
  -ContentType "application/json" `
  -Body '{"status":"DONE"}'
```

Delete:

```powershell
Invoke-RestMethod `
  -Method DELETE `
  -Uri "http://localhost:8080/api/tasks/$($task.id)"
```

## pgAdmin Check

```sql
SELECT id, title, description, status, created_at, updated_at
FROM tasks
ORDER BY created_at DESC;
```
