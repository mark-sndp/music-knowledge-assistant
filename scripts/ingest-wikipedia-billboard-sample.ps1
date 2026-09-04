param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$FixturePath = "$PSScriptRoot/../data/wikipedia-billboard-sample.json"
)

$ErrorActionPreference = "Stop"
$apiBase = "$BaseUrl/api/v1/knowledge-sources"
$sources = Get-Content -Raw -Path $FixturePath | ConvertFrom-Json
$existing = Invoke-RestMethod -Method Get -Uri $apiBase

foreach ($source in $sources) {
    $match = $existing | Where-Object { $_.uri -eq $source.uri } | Select-Object -First 1
    if ($null -eq $match) {
        $body = $source | ConvertTo-Json -Depth 5
        $match = Invoke-RestMethod -Method Post -Uri $apiBase -ContentType "application/json" -Body $body
        Write-Host "Created source $($match.id): $($match.title)"
    } else {
        Write-Host "Source already exists $($match.id): $($match.title)"
    }

    Invoke-RestMethod -Method Post -Uri "$apiBase/$($match.id)/chunks" | Out-Null
    Write-Host "Prepared chunks for source $($match.id)"
}
