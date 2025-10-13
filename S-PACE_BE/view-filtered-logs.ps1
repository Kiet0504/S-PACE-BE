# PowerShell script to view filtered Spring Boot logs
param(
    [string]$LogFile = "logs/space-app.log",
    [string]$Filter = "",
    [switch]$ErrorsOnly,
    [switch]$AvatarOnly
)

Write-Host "=== Spring Boot Logs Viewer (Filtered) ===" -ForegroundColor Green
Write-Host "Log file: $LogFile" -ForegroundColor Yellow

if ($ErrorsOnly) {
    Write-Host "Filter: ERROR and Exception messages only" -ForegroundColor Red
    $Filter = "ERROR|Exception|Failed"
} elseif ($AvatarOnly) {
    Write-Host "Filter: Avatar and upload related messages" -ForegroundColor Cyan
    $Filter = "avatar|upload|file|AWS|S3"
} elseif ($Filter) {
    Write-Host "Filter: $Filter" -ForegroundColor Cyan
}

Write-Host "Press Ctrl+C to stop viewing logs" -ForegroundColor Cyan
Write-Host ""

# Check if log file exists
if (-not (Test-Path $LogFile)) {
    Write-Host "Log file not found: $LogFile" -ForegroundColor Red
    exit 1
}

# Get initial file size
$lastSize = (Get-Item $LogFile).Length

# Show last filtered lines first
Write-Host "=== Recent Filtered Logs ===" -ForegroundColor Cyan
$recentLogs = Get-Content $LogFile -Tail 50
if ($Filter) {
    $recentLogs = $recentLogs | Where-Object { $_ -match $Filter }
}

$recentLogs | ForEach-Object {
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
Write-Host "=== Live Filtered Logs (Real-time) ===" -ForegroundColor Cyan

# Monitor for new logs
while ($true) {
    try {
        # Check if file size changed
        $currentSize = (Get-Item $LogFile).Length
        
        if ($currentSize -gt $lastSize) {
            # Read new content
            $newContent = Get-Content $LogFile -Tail 50 | Select-Object -Skip ($lastSize -gt 0 ? 1 : 0)
            
            # Apply filter if specified
            if ($Filter) {
                $newContent = $newContent | Where-Object { $_ -match $Filter }
            }
            
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
