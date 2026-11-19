# fix-java-version.ps1
# Run this after every "npx cap sync android" to fix the Java 21 → 17 issue.
# Your system has JDK 17 but Capacitor plugins require JDK 21.

Write-Host "Fixing Java VERSION_21 -> VERSION_17 in all Capacitor build files..." -ForegroundColor Cyan

$files = @(
    "android\app\capacitor.build.gradle",
    "node_modules\@capacitor\android\capacitor\build.gradle",
    "node_modules\@capacitor\app\android\build.gradle"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "VERSION_21") {
            $updated = $content -replace "VERSION_21", "VERSION_17"
            Set-Content -Path $file -Value $updated -NoNewline
            Write-Host "  Fixed: $file" -ForegroundColor Green
        } else {
            Write-Host "  OK (no change needed): $file" -ForegroundColor Gray
        }
    } else {
        Write-Host "  Skipped (not found): $file" -ForegroundColor Yellow
    }
}

# Also scan for any other capacitor plugins
$extraFiles = Get-ChildItem -Path "node_modules\@capacitor" -Recurse -Filter "build.gradle" -ErrorAction SilentlyContinue |
    Where-Object { (Get-Content $_.FullName -Raw) -match "VERSION_21" }

foreach ($f in $extraFiles) {
    $content = Get-Content $f.FullName -Raw
    $updated = $content -replace "VERSION_21", "VERSION_17"
    Set-Content -Path $f.FullName -Value $updated -NoNewline
    Write-Host "  Fixed extra: $($f.FullName)" -ForegroundColor Green
}

Write-Host "`nDone! All Java VERSION_21 references fixed to VERSION_17." -ForegroundColor Cyan
Write-Host "You can now run: .\gradlew.bat assembleDebug" -ForegroundColor White
