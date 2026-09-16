param([string]$BaseUrl = 'http://127.0.0.1:8085')
$ErrorActionPreference='Stop'
if($BaseUrl -ne 'http://127.0.0.1:8085') { throw 'This fixture smoke test is restricted to the local CodeQuest API.' }
$email="arrays-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
function Assert($condition,$message) { if(-not $condition) { throw $message } }
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers { $csrf=Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session; $h=@{}; $h[$csrf.headerName]=$csrf.token; return $h }
function Post($path,$body) { Invoke-RestMethod "$BaseUrl/api$path" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body ($body|ConvertTo-Json -Depth 8) }
function Get($path) { Invoke-RestMethod "$BaseUrl/api$path" -WebSession $session }
function Locked($id) {
    $r=Invoke-WebRequest "$BaseUrl/api/questions/$id/submit" -Method Post -ContentType 'application/json' -WebSession $session -Headers (Headers) -Body '{"answer":"8"}' -SkipHttpErrorCheck
    Assert ($r.StatusCode -eq 403) "Submission bypassed lock: $id"
    $r=Invoke-WebRequest "$BaseUrl/api/questions/$id" -WebSession $session -SkipHttpErrorCheck
    Assert ($r.StatusCode -eq 403) "Detail bypassed lock: $id"
}
$new=Post '/auth/register' @{email=$email;password=$password;displayName='Array Smoke Explorer'}
Assert ($new.totalXp -eq 0) 'Starting XP is not zero.'
Locked 'arrays-01'
& "$PSScriptRoot/prepare-arrays-demo.ps1" -Email $email -Password $password | Out-Null
$pack=Get-Content -Raw "$PSScriptRoot/../content/dsa/arrays/v1.json" | ConvertFrom-Json
$state=Get '/topics/arrays/progress'
Assert ($state.completed -eq 0 -and $state.unlockedTrail -eq 1) 'Initial trail state is incorrect.'
$total=0
foreach($q in $pack.questions) {
    if($q.order -in @(1,11,21,31)) { Locked ('arrays-'+($q.order+10).ToString('00')) }
    $detail=Get "/questions/$($q.id)"
    Assert (-not $detail.PSObject.Properties['expectedAnswer'] -and -not $detail.PSObject.Properties['validator']) 'Private validation exposed.'
    $bad=Post "/questions/$($q.id)/submit" @{answer='not-an-answer'}
    Assert (-not $bad.correct -and $bad.awardedXp -eq 0 -and $bad.progress.totalXp -eq $total) 'Wrong answer changed XP.'
    $total+=$q.xpReward
    if($q.order -eq 50) {
        $h=Headers; $h.Cookie=$session.Cookies.GetCookieHeader([Uri]$BaseUrl)
        $results=1..2 | ForEach-Object -Parallel { Invoke-RestMethod "$using:BaseUrl/api/questions/arrays-50/submit" -Method Post -ContentType 'application/json' -Headers $using:h -Body '{"answer":"7"}' } -ThrottleLimit 2
        Assert (($results.awardedXp|Measure-Object -Sum).Sum -eq 30) 'Concurrent submissions duplicated XP.'
        Assert (@($results|Where-Object topicCompletedNow).Count -eq 1) 'Concurrent completion duplicated badge event.'
    } else {
        $r=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer}
        Assert ($r.correct -and $r.awardedXp -eq $q.xpReward -and $r.progress.totalXp -eq $total) "Wrong reward: $($q.id)"
        Assert ($r.trailCompletedNow -eq ($q.order%10 -eq 0)) 'Wrong trail completion event.'
    }
    $again=Post "/questions/$($q.id)/submit" @{answer=$q.expectedAnswer}
    Assert ($again.awardedXp -eq 0 -and $again.progress.totalXp -eq $total) 'Replay duplicated XP.'
}
$state=Get '/topics/arrays/progress'
Assert ($state.completed -eq 50 -and $state.totalXp -eq 900 -and $state.badge -eq 'Array Isles Pathfinder' -and $state.successorEligible) 'Final state incorrect.'
Post '/auth/logout' @{} | Out-Null
Post '/auth/login' @{email=$email;password=$password} | Out-Null
$saved=Get '/topics/arrays/progress'
Assert ($saved.completed -eq 50 -and $saved.totalXp -eq 900 -and $saved.badge -eq $state.badge) 'Progress did not survive login.'
Write-Output 'PASS: 50 correct/incorrect/replay checks, campaign/trail locks, concurrent final completion, badge and MySQL login persistence.'
