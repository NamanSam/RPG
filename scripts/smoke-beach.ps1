# PowerShell 7. Creates a synthetic account in the dedicated CodeQuest database.
param([string]$BaseUrl = 'http://127.0.0.1:8085')
$ErrorActionPreference = 'Stop'
function Assert($Condition, [string]$Message) { if (-not $Condition) { throw $Message } }
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
function PostJson([string]$Path, $Body) {
    $token = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
    $headers = @{}; $headers[$token.headerName] = $token.token
    Invoke-RestMethod "$BaseUrl/api$Path" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $session -Body ($Body | ConvertTo-Json)
}
$email = "beach-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
$account = PostJson '/auth/register' @{email=$email;password=$password;displayName='Beach Smoke Explorer'}
Assert ($account.totalXp -eq 0) 'New player must start at 0 XP.'
$state = Invoke-RestMethod "$BaseUrl/api/me/progress" -WebSession $session
Assert ($state.quests[0].status -eq 'AVAILABLE') 'Quest 1 must be available.'
Assert ($state.quests[1].status -eq 'LOCKED' -and $state.quests[2].status -eq 'LOCKED') 'Quests 2 and 3 must start locked.'
$token = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
$headers = @{}; $headers[$token.headerName] = $token.token
foreach ($locked in @(@{id='choose-the-type';answer='boolean'},@{id='operator-training';answer='2'})) {
    $response = Invoke-WebRequest "$BaseUrl/api/quests/$($locked.id)/submit" -Method Post -ContentType 'application/json' -Headers $headers -WebSession $session -Body (@{answer=$locked.answer}|ConvertTo-Json) -SkipHttpErrorCheck
    Assert ($response.StatusCode -eq 403) "Locked quest $($locked.id) was not rejected."
}
$wrong = PostJson '/quests/first-variable/submit' @{answer='4'}
Assert (-not $wrong.correct -and $wrong.awardedXp -eq 0 -and $wrong.progress.totalXp -eq 0) 'Incorrect answer changed XP.'
$first = PostJson '/quests/first-variable/submit' @{answer='5'}
Assert ($first.awardedXp -eq 50 -and $first.progress.totalXp -eq 50) 'Quest 1 reward is incorrect.'
Assert ($first.progress.quests[1].status -eq 'AVAILABLE') 'Quest 2 did not unlock.'
$replay = PostJson '/quests/first-variable/submit' @{answer='5'}
Assert ($replay.awardedXp -eq 0 -and $replay.progress.totalXp -eq 50) 'Replay duplicated XP.'
$second = PostJson '/quests/choose-the-type/submit' @{answer='boolean'}
Assert ($second.awardedXp -eq 50 -and $second.progress.totalXp -eq 100) 'Quest 2 reward is incorrect.'

# Two independent HTTP requests using the same authenticated identity and CSRF token.
$token = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
$parallelHeaders = @{Cookie=$session.Cookies.GetCookieHeader([Uri]$BaseUrl)}
$parallelHeaders[$token.headerName] = $token.token
$results = 1..2 | ForEach-Object -Parallel {
    Invoke-RestMethod "$using:BaseUrl/api/quests/operator-training/submit" -Method Post -ContentType 'application/json' -Headers $using:parallelHeaders -Body '{"answer":"2"}'
} -ThrottleLimit 2
Assert (($results.awardedXp | Measure-Object -Sum).Sum -eq 75) 'Concurrent completion duplicated or lost XP.'
Assert (@($results | Where-Object beachCompletedNow).Count -eq 1) 'Beach completion event must occur once.'
$state = Invoke-RestMethod "$BaseUrl/api/me/progress" -WebSession $session
Assert ($state.totalXp -eq 175 -and $state.beachCompleted -eq 3) 'Final beach progress is incorrect.'
Assert ($state.worlds[1].unlocked -and -not $state.worlds[2].unlocked) 'Incorrect world unlock state.'
Assert ($state.badges.Count -eq 1 -and $state.badges[0].id -eq 'beginner-beach') 'Badge was missing or duplicated.'
PostJson '/auth/logout' @{} | Out-Null
$login = PostJson '/auth/login' @{email=$email;password=$password}
$restored = Invoke-RestMethod "$BaseUrl/api/me/progress" -WebSession $session
Assert ($login.totalXp -eq 175 -and $restored.beachCompleted -eq 3 -and $restored.worlds[1].unlocked -and $restored.badges.Count -eq 1) 'Progress was lost after login.'
Write-Output 'PASS: all ten requested progression checks, plus concurrent final completion, against real HTTP/MySQL.'
Write-Output 'Created one synthetic Beach Smoke Explorer account in CodeQuest only.'
