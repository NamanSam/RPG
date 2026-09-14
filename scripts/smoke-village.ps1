# PowerShell 7. Runs all six quests on a fresh synthetic CodeQuest account.
param([string]$BaseUrl = 'http://127.0.0.1:8085')
$ErrorActionPreference = 'Stop'
function Assert($Condition, [string]$Message) { if (-not $Condition) { throw $Message } }
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Headers {
    $token = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
    $result = @{}; $result[$token.headerName] = $token.token
    return $result
}
function PostJson([string]$Path, $Body) {
    Invoke-RestMethod "$BaseUrl/api$Path" -Method Post -ContentType 'application/json' -Headers (Headers) -WebSession $session -Body ($Body | ConvertTo-Json)
}
function State { Invoke-RestMethod "$BaseUrl/api/me/progress" -WebSession $session }
function RejectLocked([string]$Id, [string]$Answer) {
    $response=Invoke-WebRequest "$BaseUrl/api/quests/$Id/submit" -Method Post -ContentType 'application/json' -Headers (Headers) -WebSession $session -Body (@{answer=$Answer}|ConvertTo-Json) -SkipHttpErrorCheck
    Assert ($response.StatusCode -eq 403) "Locked submission accepted for $Id."
    $read=Invoke-WebRequest "$BaseUrl/api/quests/$Id" -WebSession $session -SkipHttpErrorCheck
    Assert ($read.StatusCode -eq 403) "Locked quest detail exposed for $Id."
}
$email="village-smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password=[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
$new=PostJson '/auth/register' @{email=$email;password=$password;displayName='Village Smoke Explorer'}
Assert ($new.totalXp -eq 0) 'New account did not start at zero.'
$state=State
Assert (-not $state.worlds[1].unlocked -and @($state.villageQuests | Where-Object status -ne 'LOCKED').Count -eq 0) 'Village must start fully locked.'
RejectLocked 'the-gatekeeper' 'coins >= 10'
RejectLocked 'the-looping-mill' 'for (int i = 0; i < 3; i++)'
RejectLocked 'the-endless-well' '8'
foreach ($quest in @(@{id='first-variable';answer='5'},@{id='choose-the-type';answer='boolean'},@{id='operator-training';answer='2'})) {
    PostJson "/quests/$($quest.id)/submit" @{answer=$quest.answer} | Out-Null
}
$state=State
Assert ($state.totalXp -eq 175 -and $state.beachCompleted -eq 3 -and $state.worlds[1].unlocked) 'Beach regression: incorrect XP or village unlock.'
Assert ($state.villageQuests[0].status -eq 'AVAILABLE' -and $state.villageQuests[1].status -eq 'LOCKED' -and $state.villageQuests[2].status -eq 'LOCKED') 'Village sequence did not initialize correctly.'
RejectLocked 'the-looping-mill' 'for (int i = 0; i < 3; i++)'
RejectLocked 'the-endless-well' '8'
$wrong=PostJson '/quests/the-gatekeeper/submit' @{answer='coins > 10'}
Assert (-not $wrong.correct -and $wrong.awardedXp -eq 0 -and $wrong.progress.totalXp -eq 175) 'Incorrect gate answer changed XP.'
$gate=PostJson '/quests/the-gatekeeper/submit' @{answer='coins >= 10'}
Assert ($gate.awardedXp -eq 75 -and $gate.progress.totalXp -eq 250 -and $gate.progress.villageQuests[1].status -eq 'AVAILABLE') 'Gate reward or mill unlock failed.'
$replay=PostJson '/quests/the-gatekeeper/submit' @{answer='coins >= 10'}
Assert ($replay.awardedXp -eq 0 -and $replay.progress.totalXp -eq 250) 'Gate replay duplicated XP.'
RejectLocked 'the-endless-well' '8'
$mill=PostJson '/quests/the-looping-mill/submit' @{answer='for (int i = 0; i < 3; i++)'}
Assert ($mill.awardedXp -eq 100 -and $mill.progress.totalXp -eq 350 -and $mill.progress.level -eq 2 -and $mill.progress.villageQuests[2].status -eq 'AVAILABLE') 'Mill reward or well unlock failed.'
Assert (-not $mill.progress.worlds[2].unlocked) 'Forest unlocked before well completion.'
$wrong=PostJson '/quests/the-endless-well/submit' @{answer='7'}
Assert ($wrong.awardedXp -eq 0 -and $wrong.progress.totalXp -eq 350 -and -not $wrong.progress.worlds[2].unlocked) 'Wrong well answer changed progress.'
$parallelHeaders=Headers
$parallelHeaders.Cookie=$session.Cookies.GetCookieHeader([Uri]$BaseUrl)
$results=1..2 | ForEach-Object -Parallel {
    Invoke-RestMethod "$using:BaseUrl/api/quests/the-endless-well/submit" -Method Post -ContentType 'application/json' -Headers $using:parallelHeaders -Body '{"answer":"8"}'
} -ThrottleLimit 2
Assert (($results.awardedXp | Measure-Object -Sum).Sum -eq 125) 'Concurrent well submissions duplicated XP.'
Assert (@($results | Where-Object completedWorld -eq 'loop-village').Count -eq 1) 'Village completion event was duplicated.'
$state=State
Assert ($state.totalXp -eq 475 -and $state.villageCompleted -eq 3 -and $state.beachCompleted -eq 3) 'Final completion/XP totals are incorrect.'
Assert (@($state.badges | Where-Object id -eq 'loop-village').Count -eq 1 -and $state.badges.Count -eq 2) 'Village badge missing or duplicated.'
Assert ($state.worlds[2].unlocked -and -not $state.worlds[3].unlocked -and -not $state.worlds[4].unlocked) 'Final world unlock states are incorrect.'
$again=PostJson '/quests/the-endless-well/submit' @{answer='8'}
Assert ($again.awardedXp -eq 0 -and -not $again.completedWorld -and $again.progress.totalXp -eq 475) 'Final replay duplicated completion or XP.'
$oldReplay=PostJson '/quests/first-variable/submit' @{answer='5'}
Assert ($oldReplay.awardedXp -eq 0 -and $oldReplay.progress.totalXp -eq 475) 'Beach replay regressed after village completion.'
PostJson '/auth/logout' @{} | Out-Null
$login=PostJson '/auth/login' @{email=$email;password=$password}
$restored=State
Assert ($login.totalXp -eq 475 -and $restored.villageCompleted -eq 3 -and $restored.beachCompleted -eq 3 -and $restored.badges.Count -eq 2 -and $restored.worlds[2].unlocked) 'Progress did not persist after login.'
Write-Output 'PASS: all 16 village checks, beach-to-village progression, and concurrent final submissions against real HTTP/MySQL.'
Write-Output 'Created one synthetic Village Smoke Explorer account in CodeQuest only.'
