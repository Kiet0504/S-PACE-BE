# PowerShell script to view Spring Boot logs in real-time (like IntelliJ IDEA)
param(
    [string]$LogFile = "logs/space-app.log"
)

Write-Host "=== Spring Boot Logs Viewer ===" -ForegroundColor Green
Write-Host "Log file: $LogFile" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop viewing logs" -ForegroundColor Cyan
Write-Host ""

# Check if log file exists
if (-not (Test-Path $LogFile)) {
    Write-Host "Log file not found: $LogFile" -ForegroundColor Red
    exit 1
}

# Get initial file size
$lastSize = (Get-Item $LogFile).Length

# Show last 20 lines first
Write-Host "=== Recent Logs ===" -ForegroundColor Cyan
Get-Content $LogFile -Tail 20 | ForEach-Object {
    # Color code different log levels
    if ($_ -match "ERROR") {
        Write-Host $_ -ForegroundColor Red
    } elseif ($_ -match "WARN") {
        Write-Host $_ -ForegroundColor Yellow
    } elseif ($_ -match "INFO") {
        Write-Host $_ -ForegroundColor Green
    } elseif ($_ -match "DEBUG") {
        Write-Host $_ -ForegroundColor Gray
    } else {
        Write-Host $_ -ForegroundColor White
    }
}

Write-Host ""
Write-Host "=== Live Logs (Real-time) ===" -ForegroundColor Cyan

# Monitor for new logs
while ($true) {
    try {
        # Check if file size changed
        $currentSize = (Get-Item $LogFile).Length
        
        if ($currentSize -gt $lastSize) {
            # Read new content
            $newContent = Get-Content $LogFile -Tail 50 | Select-Object -Skip ($lastSize -gt 0 ? 1 : 0)
            
            foreach ($line in $newContent) {
                # Color code different log levels
                if ($line -match "ERROR") {
                    Write-Host $line -ForegroundColor Red
                } elseif ($line -match "WARN") {
                    Write-Host $line -ForegroundColor Yellow
                } elseif ($line -match "INFO") {
                    Write-Host $line -ForegroundColor Green
                } elseif ($line -match "DEBUG") {
                    Write-Host $line -ForegroundColor Gray
                } else {
                    Write-Host $line -ForegroundColor White
                }
            }
            
            $lastSize = $currentSize
        }
        
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "Error monitoring logs: $($_.Exception.Message)" -ForegroundColor Red
        break
    }
}
